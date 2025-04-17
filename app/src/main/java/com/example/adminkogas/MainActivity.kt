package com.example.adminkogas

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.adminkogas.adapter.KoordinatorAdapter
import com.example.adminkogas.adapter.PagerAdapter
import com.example.adminkogas.db.DbContract
import com.example.adminkogas.db.DbQuery
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.server.ConnectAstro
import com.example.adminkogas.ui.DataActivity
import com.example.adminkogas.ui.ExportActivity
import com.example.adminkogas.ui.HistoryActivity
import com.example.adminkogas.ui.KoordinatorActivity
import com.example.adminkogas.ui.LoginActivity
import com.example.adminkogas.ui.SaldoActivity
import com.example.adminkogas.ui.TagihanActivity
import com.example.adminkogas.utils.FirebaseQuery
import com.example.adminkogas.utils.SessionLogin
import com.example.adminkogas.utils.Utils
import com.example.adminkogas.utils.Utils.formatRupiah
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_koordinator.rlKoordinator
import kotlinx.android.synthetic.main.activity_main.btnRefresh
import kotlinx.android.synthetic.main.activity_main.collapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_main.rlBayar
import kotlinx.android.synthetic.main.activity_main.rlDaftar
import kotlinx.android.synthetic.main.activity_main.rlExport
import kotlinx.android.synthetic.main.activity_main.rlHistory
import kotlinx.android.synthetic.main.activity_main.rlKeluar
import kotlinx.android.synthetic.main.activity_main.rlKeuntungan
import kotlinx.android.synthetic.main.activity_main.rlSaldo
import kotlinx.android.synthetic.main.activity_main.rlTipe
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.tvKeuntungan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var totalTransaksi = 0
    private var detailPembayaran: MutableList<DetailPembayaran> = mutableListOf()
    private lateinit var sessionLogin: SessionLogin
    private lateinit var dbQuery: DbQuery
    private lateinit var connAtro: ConnectAstro

    private val STORAGE_PERMISSION_CODE = 101

    companion object{
        private val TAB_CATEGORY = intArrayOf(
            R.string.harian,
            R.string.bulanan
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        collapsingToolbarLayout.title = "Laporan"
        dbQuery = DbQuery.getInstance(this)
        sessionLogin = SessionLogin(this)
//        sessionLogin.checkLogin()
        connAtro = ConnectAstro()

        if (!sessionLogin.isLoggedIn()){
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finishAffinity()
        }

        viewPager()

        rlDaftar.setOnClickListener(this)
        rlSaldo.setOnClickListener(this)
        rlHistory.setOnClickListener(this)
        rlExport.setOnClickListener(this)
        rlBayar.setOnClickListener(this)
        rlKeluar.setOnClickListener(this)
        rlTipe.setOnClickListener(this)
        rlKoordinator.setOnClickListener(this)

        getTransk()

        btnRefresh.setOnClickListener{
            getTransk()
            viewPager()
        }

        rlKeuntungan.setOnClickListener{
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("his", true)
            startActivity(intent)
        }

    }
    private fun intentClass(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

    private fun getTransk(){
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO){
                getTransaksi()
            }
            tvKeuntungan.text = formatRupiah().format(result).replace(",00", "")
        }
    }

    private fun getTransaksi():Int{
        var keuntungan = 0
        val conn = connAtro.connection(this)
        if (conn != null){
            try {
                val query = "SELECT SUM(Jumlah) AS Jumlah, CAST(tanggal AS DATE) AS Tanggal, CreatedBy FROM JS_HISTPROPER WHERE CONVERT(DATE, tanggal) = CONVERT(DATE, GETDATE()) GROUP BY CAST(Tanggal AS DATE), CreatedBy"
                val preparedStatement = conn.prepareStatement(query)
                val result = preparedStatement.executeQuery()
                while (result.next()){
                    keuntungan = result.getInt("Jumlah")
                }
            }catch (e: SQLException){
                e.printStackTrace()
            }
        }
        return keuntungan
    }

    private fun transaksi(){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = dateFormat.format(Date())
        FirebaseQuery.myRef.child("Pembayaran").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    totalTransaksi = 0
                    for (mSnapshot in snapshot.children) {
                        for (mData in mSnapshot.children) {
                            val piutang = mData.getValue(DetailPembayaran::class.java)
                            val targetDateWithoutTime = piutang?.tanggal!!.split(" ")[0]
                            if (currentDate == targetDateWithoutTime){
                                totalTransaksi += piutang.jumlah!!.toInt()
                            }
                        }
                    }
                }
                tvKeuntungan.text = formatRupiah().format(totalTransaksi).replace(",00", "")
            }

            override fun onCancelled(error: DatabaseError) {
                Utils.showText(
                    this@MainActivity,
                    error.details + " " + error.message
                )
            }
        })
    }

    private fun getPembayaran(){
        FirebaseQuery.myRef.child("Pembayaran").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (mSnapshot in snapshot.children) {
                        for (mData in mSnapshot.children) {
                            val piutang = mData.getValue(DetailPembayaran::class.java)
                            if (piutang != null){
                                val contentValues = ContentValues()
                                contentValues.put(DbContract.NoteColumns.KEY, mData.key)
                                contentValues.put(DbContract.NoteColumns.IDPELANGGAN, piutang.id)
                                contentValues.put(DbContract.NoteColumns.NAMA, piutang.nama)
                                contentValues.put(DbContract.NoteColumns.ALAMAT, piutang.alamat)
                                contentValues.put(DbContract.NoteColumns.METERAWAL, piutang.m1)
                                contentValues.put(DbContract.NoteColumns.METERAKHIR, piutang.m2)
                                contentValues.put(DbContract.NoteColumns.METERTERPAKAI, piutang.m3)
                                contentValues.put(DbContract.NoteColumns.GOLONGAN, piutang.gol)
                                contentValues.put(DbContract.NoteColumns.PERIODE, piutang.periode)
                                contentValues.put(DbContract.NoteColumns.BIAYA, piutang.biaya)
                                contentValues.put(DbContract.NoteColumns.REFUNDPPN, piutang.refund)
                                contentValues.put(DbContract.NoteColumns.DENDA, piutang.denda)
                                contentValues.put(DbContract.NoteColumns.SALES, piutang.sales)
                                contentValues.put(DbContract.NoteColumns.DEV, piutang.dev)
                                contentValues.put(DbContract.NoteColumns.ADMIN, piutang.admin)
                                contentValues.put(DbContract.NoteColumns.JUMLAH, piutang.jumlah)
                                contentValues.put(DbContract.NoteColumns.TIPE, piutang.tipe)
                                contentValues.put(DbContract.NoteColumns.NOTA, piutang.nota)
                                contentValues.put(DbContract.NoteColumns.KOLEK, piutang.kolek)
                                contentValues.put(DbContract.NoteColumns.TANGGAL, piutang.tanggal)
                                contentValues.put(DbContract.NoteColumns.CREATEDBY, piutang.createdBy)
                                val cekKey = dbQuery.queryCekKey(mData.key.toString())
                                if (cekKey == 1){
                                    Log.e("cek key", cekKey.toString())
                                }else{
                                    dbQuery.insert(contentValues)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Utils.showText(
                    this@MainActivity,
                    databaseError.details + " " + databaseError.message
                )
            }
        })
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.rlDaftar -> {
                val intent = Intent(this, DataActivity::class.java)
                intent.putExtra("data", true)
                startActivity(intent)
            }
            R.id.rlSaldo ->{
                intentClass(SaldoActivity())
            }
            R.id.rlHistory ->{
                val intent = Intent(this, HistoryActivity::class.java)
                intent.putExtra("his", false)
                startActivity(intent)
            }
            R.id.rlExport ->{
                intentClass(ExportActivity())
            }
            R.id.rlBayar -> {
                intentClass(TagihanActivity())
            }
            R.id.rlKoordinator -> {
                intentClass(KoordinatorActivity())
            }
            R.id.rlTipe -> {
                val intent = Intent(this, DataActivity::class.java)
                intent.putExtra("data", false)
                startActivity(intent)
            }
            R.id.rlKeluar -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Apakah anda ingin keluar?")
                builder.setCancelable(true)
                builder.setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
                builder.setPositiveButton("Ya") { dialog, which ->
                    sessionLogin.logoutUser()
                    dbQuery.delete()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
    }

    private fun viewPager(){
        val pagerAdapter = PagerAdapter(this as FragmentActivity)
        val viewPager2: ViewPager2 = findViewById(R.id.viewPager)
        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = pagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabsLayout)
        TabLayoutMediator(tabs, viewPager2){tab, position ->
            tab.text = resources.getString(TAB_CATEGORY[position])
        }.attach()
        actionBar?.elevation = 0f
    }
}