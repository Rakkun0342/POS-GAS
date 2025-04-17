package com.example.adminkogas.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkogas.R
import com.example.adminkogas.adapter.DataUserAdapter
import com.example.adminkogas.adapter.DetailAdapter
import com.example.adminkogas.db.DbQuery
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.server.ConnectAstro
import com.example.adminkogas.utils.Utils
import com.example.adminkogas.utils.Utils.formatRupiah
import com.example.adminkogas.utils.Utils.showText
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_detail.rvDetail
import kotlinx.android.synthetic.main.activity_detail.toolbarDetail
import kotlinx.android.synthetic.main.layout_empty.layoutEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DetailActivity : AppCompatActivity() {

    private lateinit var detailAdapter: DetailAdapter
    private lateinit var detailPembayaran: MutableList<DetailPembayaran>
    private lateinit var dataUserAdapter: DataUserAdapter
    private lateinit var dbQuery: DbQuery
    private lateinit var connectAstro: ConnectAstro
    private var nama: String? = null
    private var tgl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbarDetail)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbQuery = DbQuery.getInstance(this)
        connectAstro = ConnectAstro()
        rvDetail.layoutManager = LinearLayoutManager(this)
        rvDetail.setHasFixedSize(false)

        detailPembayaran = mutableListOf()

        when (intent.getStringExtra("kondisi")){
            "detail" -> {
                nama = intent.getStringExtra("nama")
                bottomShet(nama.toString(), "histori")
            }
            "harian" -> {
                nama = intent.getStringExtra("nama")
                bottomShet(nama.toString(), "harian")
            }
            "export" -> {
                tgl = intent.getStringExtra("key")
                bottomShet(tgl.toString(), "export")
            }
        }

        setUpItemTouchHelper()
    }

    private fun bottomShet(name: String, kondisi: String){
        supportActionBar?.title = name
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                getPembayaran(name, kondisi)
            }
            detailPembayaran.sortByDescending { it.idPrimer }
            detailAdapter = DetailAdapter(detailPembayaran)
            rvDetail.adapter = detailAdapter
            detailAdapter.setOnItemClickCallback(object : DetailAdapter.OnItemClickCallback{
                override fun onItemClicked(items: DetailPembayaran) {
                    val view = layoutInflater.inflate(R.layout.bottom_shet_filter, null)
                    val dNama = view.findViewById<TextView>(R.id.tvNamaDetail)
                    val dPeriode = view.findViewById<TextView>(R.id.tvPeriodeDetail)
                    val dM3      = view.findViewById<TextView>(R.id.tvM3Detail)
                    val dBiaya   = view.findViewById<TextView>(R.id.tvBiayaDetail)
                    val dDenda   = view.findViewById<TextView>(R.id.tvDendaDetail)
                    val dTotalBiaya = view.findViewById<TextView>(R.id.tvTotalBiayaDetail)
                    val dTotalDenda = view.findViewById<TextView>(R.id.tvTotalDendaDetail)
                    val dTotalAdmin = view.findViewById<TextView>(R.id.tvBiayaAdminDetail)
                    val dTotalRefun = view.findViewById<TextView>(R.id.tvReturnPPNDetail)
                    val dTotalPayment = view.findViewById<TextView>(R.id.tvTotalPaymentDetail)
                    dNama.text = items.nama
                    dPeriode.text = items.periode
                    dM3.text = items.m3.toString()
                    dBiaya.text = items.biaya.toString()
                    dDenda.text = items.denda.toString()
                    dTotalBiaya.text = formatRupiah().format(items.biaya).replace(",00", "")
                    dTotalDenda.text = formatRupiah().format(items.denda).replace(",00", "")
                    dTotalAdmin.text = formatRupiah().format(items.admin!!.toInt() + items.sales!!.toInt() + items.dev!!.toInt()).replace(",00", "")
                    dTotalRefun.text = formatRupiah().format(items.refund).replace(",00", "")
                    dTotalPayment.text = formatRupiah().format((items.biaya!!.toInt() + items.denda!!.toInt() + items.admin!!.toInt()) - items.refund!!.toInt()).replace(",00", "")
                    val bottom = BottomSheetDialog(this@DetailActivity)
                    bottom.setContentView(view)
                    bottom.show()
                }
            })
            layoutEmpty.visibility = View.GONE
        }
    }

    private fun getCreated():MutableList<UserFirebase>{
        val items = mutableListOf<UserFirebase>()
        val conn = connectAstro.connection(this)
        if (conn != null){
            try {
                val query = "SELECT CreatedBy AS Nama " +
                        "FROM JS_HISTPROPER WHERE VoidByAdmin IS NULL " +
                        "GROUP BY CreatedBy"
                val statmen = conn.createStatement()
                val resultSet = statmen.executeQuery(query)
                while (resultSet.next()){
                    val userFirebase = UserFirebase()
                    userFirebase.nama = resultSet.getString("Nama")
                    items.add(userFirebase)
                }
            }catch (e: SQLException){
                e.printStackTrace()
            }
        }
        return items
    }

    private fun getPembayaran(nama: String, kondisi: String){
        detailPembayaran = mutableListOf<DetailPembayaran>()
        val conn = connectAstro.connection(this)
        if (conn !=null ){
            try {
                val query = when(kondisi){
                    "histori" -> {
                        "SELECT * FROM JS_HISTPROPER WHERE CreatedBy = ? AND VoidByAdmin IS NULL"
                    }
                    "export" -> {
                        "SELECT * FROM JS_HISTPROPER WHERE FORMAT(CONVERT(DATE,Tanggal), 'yyyy-MMM') = ? AND VoidByAdmin IS NULL"
                    }
                    "harian" -> {
                        "SELECT * FROM JS_HISTPROPER WHERE FORMAT(CONVERT(DATE,Tanggal), 'yyyy-MM-dd') = ? AND VoidByAdmin IS NULL"
                    }
                    else -> {
                        ""
                    }
                }

                val preparedStatement = conn.prepareStatement(query)
                preparedStatement.setString(1, nama)
                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()){
                    val mDetailPembayaran = DetailPembayaran()
                    mDetailPembayaran.idPrimer   = resultSet.getInt("Nomor")
                    mDetailPembayaran.id         = resultSet.getInt("Id_Pelanggan")
                    mDetailPembayaran.nama       = resultSet.getString("Nama_Pelanggan")
                    mDetailPembayaran.alamat     = resultSet.getString("Alamat")
                    mDetailPembayaran.m1         = resultSet.getInt("Meter_Awal")
                    mDetailPembayaran.m2         = resultSet.getInt("Meter_Akhir")
                    mDetailPembayaran.m3         = resultSet.getInt("Meter_Terpakai")
                    mDetailPembayaran.gol        = resultSet.getString("Gol")
                    mDetailPembayaran.periode    = resultSet.getString("Periode")
                    mDetailPembayaran.biaya      = resultSet.getInt("Biaya")
                    mDetailPembayaran.refund     = resultSet.getInt("Return_PPn")
                    mDetailPembayaran.denda      = resultSet.getInt("Denda")
                    mDetailPembayaran.kolek      = resultSet.getInt("Kolektif")
                    mDetailPembayaran.sales      = resultSet.getInt("Sales")
                    mDetailPembayaran.dev        = resultSet.getInt("Dev")
                    mDetailPembayaran.admin      = resultSet.getInt("Admin")
                    mDetailPembayaran.jumlah     = resultSet.getInt("Jumlah")
                    mDetailPembayaran.tipe       = resultSet.getString("Tipe")
                    mDetailPembayaran.nota       = resultSet.getInt("Nota")
                    mDetailPembayaran.tanggal    = resultSet.getString("Tanggal")
                    mDetailPembayaran.createdBy  = resultSet.getString("CreatedBy")
                    detailPembayaran.add(mDetailPembayaran)
                }
            }catch (e: SQLException){
                e.printStackTrace()
            }
        }
    }

    private fun createXlsx(key: String, mDetailPembayaran: MutableList<DetailPembayaran>):String {
        try {
            val strDate: String =
                SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.getDefault()).format(Date())
            val root = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "FilePembayaran"
            )
            if (!root.exists()) root.mkdirs()
            val path = File(root, "/$key.xlsx")
            val workbook = XSSFWorkbook()
            val outputStream = FileOutputStream(path)
            val headerStyle = workbook.createCellStyle()
            headerStyle.setAlignment(HorizontalAlignment.CENTER)
            headerStyle.fillForegroundColor = IndexedColors.BLUE_GREY.getIndex()
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            headerStyle.setBorderTop(BorderStyle.MEDIUM)
            headerStyle.setBorderBottom(BorderStyle.MEDIUM)
            headerStyle.setBorderRight(BorderStyle.MEDIUM)
            headerStyle.setBorderLeft(BorderStyle.MEDIUM)
            val font = workbook.createFont()
            font.fontHeightInPoints = 12.toShort()
            font.color = IndexedColors.WHITE.getIndex()
            font.bold = true
            headerStyle.setFont(font)
            val sheet = workbook.createSheet("Pembayaran Pegas $key")
            var row = sheet.createRow(0)
            var cell = row.createCell(0)
            cell.setCellValue("No")
            cell.cellStyle = headerStyle
            cell = row.createCell(1)
            cell.setCellValue("ID Pelanggan")
            cell.cellStyle = headerStyle
            cell = row.createCell(2)
            cell.setCellValue("Nama Pelanggan")
            cell.cellStyle = headerStyle
            cell = row.createCell(3)
            cell.setCellValue("Alamat")
            cell.cellStyle = headerStyle
            cell = row.createCell(4)
            cell.setCellValue("Meter Awal")
            cell.cellStyle = headerStyle
            cell = row.createCell(5)
            cell.setCellValue("Meter Akhir")
            cell.cellStyle = headerStyle
            cell = row.createCell(6)
            cell.setCellValue("Meter Terpakai")
            cell.cellStyle = headerStyle
            cell = row.createCell(7)
            cell.setCellValue("Gol")
            cell.cellStyle = headerStyle
            cell = row.createCell(8)
            cell.setCellValue("Periode")
            cell.cellStyle = headerStyle
            cell = row.createCell(9)
            cell.setCellValue("Biaya")
            cell.cellStyle = headerStyle
            cell = row.createCell(10)
            cell.setCellValue("Return PPn")
            cell.cellStyle = headerStyle
            cell = row.createCell(11)
            cell.setCellValue("Denda")
            cell.cellStyle = headerStyle
            cell = row.createCell(12)
            cell.setCellValue("Jumlah")
            cell.cellStyle = headerStyle
            cell = row.createCell(13)
            cell.setCellValue("Admin")
            cell.cellStyle = headerStyle
            cell = row.createCell(14)
            cell.setCellValue("Dev")
            cell.cellStyle = headerStyle
            cell = row.createCell(15)
            cell.setCellValue("Tipe")
            cell.cellStyle = headerStyle
            cell = row.createCell(16)
            cell.setCellValue("Nota")
            cell.cellStyle = headerStyle
            cell = row.createCell(17)
            cell.setCellValue("Tanggal")
            cell.cellStyle = headerStyle
            cell = row.createCell(18)
            cell.setCellValue("CreatedBy")
            cell.cellStyle = headerStyle
            cell = row.createCell(19)
            cell.setCellValue("NetTotal")
            cell.cellStyle = headerStyle
            for (i in mDetailPembayaran.indices) {
                row = sheet.createRow(i + 1)
                cell = row.createCell(0)
                cell.setCellValue(i.toString())
                cell = row.createCell(1)
                cell.setCellValue(mDetailPembayaran[i].id.toString())
                cell = row.createCell(2)
                cell.setCellValue(mDetailPembayaran[i].nama)
                sheet.setColumnWidth(2, (i + 30) * 100)
                cell = row.createCell(3)
                cell.setCellValue(mDetailPembayaran[i].alamat)
                sheet.setColumnWidth(3, (i + 30) * 100)
                cell = row.createCell(4)
                cell.setCellValue(mDetailPembayaran[i].m1.toString())
                cell = row.createCell(5)
                cell.setCellValue(mDetailPembayaran[i].m2.toString())
                cell = row.createCell(6)
                cell.setCellValue(mDetailPembayaran[i].m3.toString())
                cell = row.createCell(7)
                cell.setCellValue(mDetailPembayaran[i].gol)
                cell = row.createCell(8)
                cell.setCellValue(mDetailPembayaran[i].periode)
                cell = row.createCell(9)
                cell.setCellValue(mDetailPembayaran[i].biaya.toString())
                cell = row.createCell(10)
                cell.setCellValue(mDetailPembayaran[i].refund.toString())
                cell = row.createCell(11)
                cell.setCellValue(mDetailPembayaran[i].denda.toString())
                cell = row.createCell(12)
                cell.setCellValue(((mDetailPembayaran[i].biaya!! + mDetailPembayaran[i].denda!!) - mDetailPembayaran[i].refund!!).toString())
                cell = row.createCell(13)
                cell.setCellValue(mDetailPembayaran[i].admin.toString())
                cell = row.createCell(14)
                cell.setCellValue(mDetailPembayaran[i].dev.toString())
                cell = row.createCell(15)
                cell.setCellValue(mDetailPembayaran[i].tipe)
                cell = row.createCell(16)
                cell.setCellValue(mDetailPembayaran[i].nota.toString())
                sheet.setColumnWidth(16, (i + 30) * 100)
                cell = row.createCell(17)
                cell.setCellValue(mDetailPembayaran[i].tanggal)
                sheet.setColumnWidth(17, (i + 30) * 100)
                cell = row.createCell(18)
                cell.setCellValue(mDetailPembayaran[i].createdBy.toString())
                cell = row.createCell(19)
                cell.setCellValue(((mDetailPembayaran[i].biaya!! + mDetailPembayaran[i].denda!! + mDetailPembayaran[i].admin!! + mDetailPembayaran[i].dev!!) - mDetailPembayaran[i].refund!!).toString())
            }
            workbook.write(outputStream)
            outputStream.close()
            return "berhasil"
        } catch (e: IOException) {
            e.printStackTrace()
            return e.toString()
        }
    }

    private fun setUpItemTouchHelper() {
        val simpleCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipedPosition = viewHolder.adapterPosition
                val modelDatabase = detailAdapter.setSwipeRemove(swipedPosition)
                val keyP = modelDatabase.idPrimer
                val builder = AlertDialog.Builder(this@DetailActivity)
                builder.setTitle("Apakah anda mau menghapus ${modelDatabase.nama} dari histori?")
                builder.setCancelable(false)
                builder.setPositiveButton("Ya"){_,_->
                    val progressBar = ProgressDialog(this@DetailActivity)
                    progressBar.setTitle("Mohon tunggu..")
                    progressBar.setMessage("Sedang menghapus")
                    progressBar.show()
                    lifecycleScope.launch {
                        val result = withContext(Dispatchers.IO){
                            updateVoid(keyP!!.toString())
                        }
                        if (result == "Ok"){
                            progressBar.dismiss()
                            Toast.makeText(
                                this@DetailActivity,
                                "Nama Pelanggan ${modelDatabase.nama} sudah dihapus",
                                Toast.LENGTH_SHORT
                            ).show()
                            detailAdapter.notifyItemRemoved(swipedPosition)
                        }else{
                            progressBar.dismiss()
                            Toast.makeText(
                                this@DetailActivity,
                                "Nama Pelanggan ${modelDatabase.nama} gagal dihapus",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                builder.setNegativeButton("Tidak"){p,_->
                    p.dismiss()
                    when (intent.getStringExtra("kondisi")){
                        "detail" -> {
                            nama = intent.getStringExtra("nama")
                            bottomShet(nama.toString(), "histori")
                        }
                        "harian" -> {
                            nama = intent.getStringExtra("nama")
                            bottomShet(nama.toString(), "harian")
                        }
                        "export" -> {
                            tgl = intent.getStringExtra("key")
                            bottomShet(tgl.toString(), "export")
                        }
                    }
                }
                builder.create().show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(rvDetail)
    }

    private fun updateVoid(key: String):String{
        var status = "Fail"
        val koneksi = connectAstro.connection(this)
        if (koneksi != null){
            try{
                val query = "DECLARE @Result VARCHAR(255) UPDATE JS_HISTPROPER SET VoidByAdmin = ? WHERE Nomor = ? SET @Result = IIF(@@ROWCOUNT > 0, 'Ok', 'Fail') SELECT @Result AS Result"
                val preparedStatement = koneksi.prepareStatement(query)
                preparedStatement.setString(1, Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss"))
                preparedStatement.setString(2, key)
                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()){
                    status = resultSet.getString("Result")
                }
            }catch (e: SQLException){
                e.printStackTrace()
            }
        }
        return status
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.export, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.export -> {
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                val viewAdapter = View.inflate(this, R.layout.select_export, null)
                val rvSelectedExpor = viewAdapter.findViewById<RecyclerView>(R.id.rvSelectedExport)
                builder.setTitle("Apakah anda mau melakukan export?")
                builder.setPositiveButton("Ya"){_,_->
                    val progressDialog = ProgressDialog(this@DetailActivity)
                    progressDialog.setTitle("Export to excel")
                    progressDialog.setMessage("Sedang proses export..")
                    progressDialog.show()
                    lifecycleScope.launch {
                        val async = async(Dispatchers.IO) {
                            if (tgl == null){
                                createXlsx(nama.toString(), detailPembayaran)
                            }else{
                                createXlsx(tgl.toString(), detailPembayaran)
                            }
                        }
                        when(async.await()){
                            "berhasil" -> {
                                progressDialog.dismiss()
                                showText(this@DetailActivity,"Data berhasil di export")
                            }
                            else -> {
                                progressDialog.dismiss()
                                showText(this@DetailActivity, "Data gagal di export")
                            }
                        }
                    }
                }
                builder.setNegativeButton("Batal"){p,_->
                    p.dismiss()
                }
                lifecycleScope.launch {
                    val result = withContext(Dispatchers.IO){
                        getCreated()
                    }
                    Log.e("hasil nama", result[0].nama.toString())
                    dataUserAdapter = DataUserAdapter(result, false)
                    rvSelectedExpor.adapter = dataUserAdapter
                    dataUserAdapter.setOnItemClickCallback(object : DataUserAdapter.OnItemClickCallback{
                        override fun onItemClicked(items: UserFirebase) {

                        }
                    })
                }
//                builder.setView(viewAdapter)
                builder.create().show()
            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}