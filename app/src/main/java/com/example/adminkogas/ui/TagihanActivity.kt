package com.example.adminkogas.ui

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.adminkogas.R
import com.example.adminkogas.adapter.TagihanAdapter
import com.example.adminkogas.db.DbQuery
import com.example.adminkogas.model.DewaAkun
import com.example.adminkogas.model.Pembayaran
import com.example.adminkogas.model.Tagihan
import com.example.adminkogas.server.Connect
import com.example.adminkogas.server.ConnectAstro
import com.example.adminkogas.utils.FirebaseQuery
import com.example.adminkogas.utils.FirebaseQuery.myRef
import com.example.adminkogas.utils.SessionLogin
import com.example.adminkogas.utils.Utils
import com.example.adminkogas.utils.Utils.formatRupiah
import com.example.adminkogas.utils.Utils.getCurrentDate
import com.example.adminkogas.utils.Utils.getFormaterDate
import com.example.adminkogas.utils.Utils.showText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_tagihan.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

class TagihanActivity : AppCompatActivity(), View.OnClickListener {
    private var totalDenda: Int = 0
    private var totalReturn: Int = 0
    private var totalBiaya: Int = 0
    private var biayaAdmin: Int = 0
    private var biaya: Int = 0
    private var denda: Int = 0
    private var refund: Int = 0
    private var m1: Int = 0
    private var m2: Int = 0
    private var name = "Tidak Ketemu"
    private var tipeP = ""
    private var alamatP = ""
    private var noAlamat = ""
    private var rt = ""
    private var rw = ""
    private var strId = ""

    private var dbNomor  = ""

    private var tipeDev = 0
    private var tipeAdmin  = 0

    private lateinit var conn: Connect
    private lateinit var connAstro: ConnectAstro
    private lateinit var tagihan: MutableList<Tagihan>
    private lateinit var mTagihan: MutableList<Tagihan>
    private lateinit var tagihanAdapter: TagihanAdapter
    private lateinit var dbQuery: DbQuery
    private lateinit var sessionLogin: SessionLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tagihan)
        conn = Connect()
        dbQuery = DbQuery.getInstance(this)
        sessionLogin = SessionLogin(this)
        connAstro = ConnectAstro()

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH),101);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 102);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 103);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 104)
        }

        myRef.child("Dewa").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mSnapshot = snapshot.getValue(DewaAkun::class.java)
                if (mSnapshot != null){
                    tipeDev = mSnapshot.dev!!.toInt()
                    tipeAdmin = mSnapshot.admin!!.toInt()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showText(this@TagihanActivity, error.details + " " + error.message)
            }
        })

        btnBack.setOnClickListener {
            finish()
        }

        rvTagihan.layoutManager = LinearLayoutManager(this)
        rvTagihan.setHasFixedSize(false)

        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Sedang Mencari Id")

        imageClear.setOnClickListener {
            etSearchID.text.clear()
            tvKosong.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            llTagihan.visibility = View.GONE
            imageClear.visibility = View.GONE
        }

        etSearchID.setOnEditorActionListener { _, actionId, event ->
            strId = etSearchID.text.toString().trim()
            tvKosong.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            llTagihan.visibility = View.GONE
            imageClear.visibility = View.GONE
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                progressDialog.show()
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    val async = async(Dispatchers.IO){
                        loadItem(strId)
                    }
                    when (val result = async.await()){
                        "berhasil" -> {
                            tvKosong.visibility = View.GONE
                            layoutEmpty.visibility = View.GONE
                            llTagihan.visibility = View.VISIBLE
                            imageClear.visibility = View.VISIBLE
                            etSearchID.isEnabled = true
                            hideKeyboard(this@TagihanActivity)
                            progressDialog.dismiss()
                            tagihanAdapter = TagihanAdapter(tagihan)
                            rvTagihan.adapter = tagihanAdapter
                            tvNama.text = name
                            var i = 0
                            repeat(tagihan.size) {
                                i++
                            }
                            biayaAdmin = (tipeDev + tipeAdmin) * i
                            tvTotalBiaya.text = formatRupiah().format(totalBiaya).replace(",00", "")
                            tvTotalDenda.text = formatRupiah().format(totalDenda).replace(",00", "")
                            tvBiayaAdmin.text = formatRupiah().format(biayaAdmin).replace(",00", "")
                            tvReturnPPN.text  = formatRupiah().format(totalReturn).replace(",00", "")
                            tvTotalPayment.text = formatRupiah().format(totalBiaya + totalDenda + biayaAdmin - totalReturn).replace(",00", "")
                        }
                        "gagal" -> {
                            tvKosong.visibility = View.VISIBLE
                            imageClear.visibility = View.VISIBLE
                            tvKosong.text = "Silahkan cek kondisi internet anda.."
                            progressDialog.dismiss()
                        }
                        else -> {
                            tvKosong.visibility = View.VISIBLE
                            imageClear.visibility = View.VISIBLE
                            tvKosong.text = result
                            progressDialog.dismiss()
                        }
                    }
                }
            }
            false
        }
        rlBayar.setOnClickListener(this)
    }

    private fun loadItem(id: String):String{
        tagihan = mutableListOf()
        totalDenda = 0
        totalBiaya = 0
        totalReturn = 0
        val koneksi = conn.connection(this)
        if (koneksi != null) {
            return try {
                val query = "EXEC USP_J_Billing_API @RegNo=?, @Action='QueryHist'"
                val preparedStatement = koneksi.prepareStatement(query)
                preparedStatement.setString(1, id)
                val resultSet = preparedStatement.executeQuery()
                tagihan.clear()
                while (resultSet.next()) {
                    val amountPaid = resultSet.getInt("AmountPaid")
                    Log.e("cek balance", amountPaid.toString())
                    name = resultSet.getString("CName")
                    if (amountPaid == 0){
                        Log.e("TAG", "loadItem: $name", )
                        tipeP = resultSet.getString("CType")
                        alamatP = resultSet.getString("Addr")
                        noAlamat = resultSet.getString("AddrNo")
                        rt = resultSet.getString("AddrRT")
                        rw = resultSet.getString("AddrRW")
                        val _HistTagihan = Tagihan()
                        m1 = resultSet.getInt("M1")
                        m2 = resultSet.getInt("M2")
                        _HistTagihan.m1 = m1
                        _HistTagihan.m2 = m2
                        _HistTagihan.periode = resultSet.getString("PeriodName")
                        _HistTagihan.m3 = m2-m1
                        biaya = resultSet.getInt("Amount")
                        _HistTagihan.biaya = biaya
                        totalBiaya += biaya
                        denda = resultSet.getInt("TotalFine")
                        _HistTagihan.denda = denda
                        totalDenda += denda
                        refund = resultSet.getInt("TotalTaxRefund")
                        _HistTagihan.refund = refund
                        totalReturn += refund
                        tagihan.add(_HistTagihan)
                    }
                }
                "berhasil"
            }catch (e: SQLException){
                e.printStackTrace()
                e.toString()
            }finally {
                koneksi.close()
            }
        }
        return "gagal"
    }

    private fun hideKeyboard(activity: Activity) {
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setPermission(date: String, piutang: MutableList<Tagihan>){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH),101);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 102);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 103);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 104)
        } else {
            setPrintBluetooth(date, piutang)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setPrintBluetooth(date: String, piutang: MutableList<Tagihan>){
        val printer = EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32)
        val staticText = "[C]<b><font size='normal'>JARINGAN GAS RUMAH TANGGA\n" +
                "[C]KOTA DEPOK</font></b>\n" +
                "[C]Jln. Kenari No. 191 Perumnas \n [C]Depok Utara" +
                "[L]\n" +
                "[C]$dbNomor \n" +
                "[C]================================\n" +
                "[C]<qrcode>${etSearchID.text.toString().trim()}</qrcode>\n" +
                "[L]\n" +
                "[L]Id Pelanggan   : [R]${etSearchID.text.toString().trim()}\n" +
                "[L]Nama Pelanggan : [R]${tvNama.text.toString().trim()}\n" +
                "[L]Alamat         : [R]$alamatP\n" +
                "[L]No Alamat      : [R]$noAlamat\n" +
                "[L]RT/RW          : [R]$rt / $rw\n" +
                "[L]Tipe           : [R]$tipeP\n" +
                "[L]Tanggal Cetak  : [R]${getFormaterDate(date, "yyyy-MM-dd")}\n" +
                "[L]Waktu Cetak    : [R]${getFormaterDate(date, "HH:mm")}\n" +
                "[L]\n" +
                "[L]--------------------------------\n" +
                "[R]Periode |[R]M3 |[R]Biaya |[R]Denda\n" +
                "[L]--------------------------------\n"
        var admin = 0

        val dynamicText = piutang.joinToString("\n") { it ->
            admin++
            "[R]${it.periode}|[R]${it.m3}|[R]${it.biaya}|[R]${it.denda}"
        }

        val formattedText = staticText + dynamicText +
                "\n[L]--------------------------------\n" +
                "[L]\n" +
                "[L]Total Biaya      : [R]${tvTotalBiaya.text.toString().trim()}\n" +
                "[L]Total Denda      : [R]${tvTotalDenda.text.toString().trim()}\n" +
                "[L]Biaya Admin      : [R]${tvBiayaAdmin.text.toString().trim()}\n" +
                "[L]Return PPn       : [R]${tvReturnPPN.text.toString().trim()}\n" +
                "[L]Total Pembayaran : [R]${tvTotalPayment.text.toString().trim()}\n" +
                "[L]\n" +
                "[C]<b>Terimakasih Telah Melakukan\n"+
                "[C]Pembayaran</b>"

        printer.printFormattedText(formattedText)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View) {
        when (v.id){
            R.id.rlBayar -> {
                val builder = AlertDialog.Builder(this@TagihanActivity)
                builder.setTitle("Apakah anda ingin membayar?")
                builder.setCancelable(true)
                builder.setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
                builder.setPositiveButton("Ya") { dialog, which ->
                    val date = getCurrentDate("yyyy-MM-dd HH:mm")
                    val currentTimeMillis = System.currentTimeMillis()
                    val timestampString = currentTimeMillis.toString()
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main){
                            tagihan.forEach{
                                val generatedNota = "10000" + timestampString.substring(timestampString.length - 5)
                                val pembayaran = Pembayaran().apply {
                                    id = strId.toInt()
                                    nama = name
                                    alamat = alamatP
                                    m1 = it.m1
                                    m2 = it.m2
                                    m3 = it.m3
                                    gol = tipeP
                                    periode = it.periode
                                    biaya = it.biaya
                                    refund = it.refund
                                    denda = it.denda
                                    kolek = 0
                                    sales = 0
                                    dev = tipeDev
                                    admin = tipeAdmin
                                    jumlah = (it.biaya!! + it.denda!!) - it.refund!!
                                    tipe = "Tunai"
                                    nota = generatedNota.toInt()
                                    tanggal = date
                                    createdBy = "Admin"
                                }
                                updatePembayaran(pembayaran)
                            }
                        }
                        showText(this@TagihanActivity, "Berhasil Membayar")
                        setPermission(date, tagihan)
                    }
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
    }
    private fun updatePembayaran(pembayaran: Pembayaran): String {
        val conAs = connAstro.connection(this)
        if (conAs != null) {
            try {
                val query =
                    "INSERT INTO JS_HISTPROPER (ID_Pelanggan, Nama_Pelanggan, Alamat, Meter_Awal, Meter_Akhir," +
                            "Meter_Terpakai, Gol, Periode, Biaya, Return_PPn, Denda, Kolektif, Sales, Jumlah," +
                            "Admin, Dev, Tipe, Nota, Tanggal, CreatedBy, NetTotal) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);"

                val preparedStatement = conAs.prepareStatement(query)
                preparedStatement.setInt(1, pembayaran.id!!.toInt())
                preparedStatement.setString(2, pembayaran.nama)
                preparedStatement.setString(3, pembayaran.alamat)
                preparedStatement.setInt(4, pembayaran.m1!!.toInt())
                preparedStatement.setInt(5, pembayaran.m2!!.toInt())
                preparedStatement.setInt(6, pembayaran.m3!!.toInt())
                preparedStatement.setString(7, pembayaran.gol)
                preparedStatement.setString(8, pembayaran.periode)
                preparedStatement.setInt(9, pembayaran.biaya!!.toInt())
                preparedStatement.setInt(10, pembayaran.refund!!.toInt())
                preparedStatement.setInt(11, pembayaran.denda!!.toInt())
                preparedStatement.setInt(12, 0)
                preparedStatement.setInt(13, 0)
                preparedStatement.setInt(14, pembayaran.jumlah!!.toInt())
                preparedStatement.setInt(15, tipeAdmin)
                preparedStatement.setInt(16, tipeDev)
                preparedStatement.setString(17, pembayaran.tipe)
                preparedStatement.setInt(18, pembayaran.nota!!.toInt())
                preparedStatement.setString(19, pembayaran.tanggal)
                preparedStatement.setString(20, "Admin")
                preparedStatement.setInt(21, pembayaran.jumlah!!.toInt() + tipeDev + tipeAdmin)
                preparedStatement.execute()
                return "berhasil"
            } catch (e: SQLException) {
                e.printStackTrace()
                return "gagal"
            } finally {
                conAs.close()
            }
        }
        return "gagal"
    }
}