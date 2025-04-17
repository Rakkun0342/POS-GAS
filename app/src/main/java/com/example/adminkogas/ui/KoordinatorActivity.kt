package com.example.adminkogas.ui

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminkogas.R
import com.example.adminkogas.adapter.KoordinatorAdapter
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.model.Koordinator
import com.example.adminkogas.server.Connect
import com.example.adminkogas.utils.Utils
import kotlinx.android.synthetic.main.activity_koordinator.etKoordinator
import kotlinx.android.synthetic.main.activity_koordinator.rvKoordinator
import kotlinx.android.synthetic.main.activity_koordinator.toolbarKoordinator
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

class KoordinatorActivity : AppCompatActivity() {

    var selectedCoord = ""

    private lateinit var connect: Connect
    private lateinit var koordinatorAdapter: KoordinatorAdapter
    private var koordinator: MutableList<Koordinator> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_koordinator)
        setSupportActionBar(toolbarKoordinator)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Koordinator"

        connect = Connect()
        rvKoordinator.layoutManager = LinearLayoutManager(this)
        rvKoordinator.setHasFixedSize(false)

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu..")
        progressDialog.setMessage("Sedang mengambil data..")
        progressDialog.show()

        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO){
                loadListKoord()
            }
            progressDialog.dismiss()
            val adapterTipe = ArrayAdapter<String>(this@KoordinatorActivity, android.R.layout.simple_spinner_item, list)
            adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            etKoordinator.adapter = adapterTipe
            etKoordinator.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    progressDialog.show()
                    selectedCoord = list[position]
                    lifecycleScope.launch {
                        val load = withContext(Dispatchers.IO){
                            loadKoordinator(selectedCoord)
                        }
                        progressDialog.dismiss()
                        load.sortBy { it.namaPelanggan }
                        koordinator = load
                        koordinatorAdapter = KoordinatorAdapter(load)
                        rvKoordinator.adapter = koordinatorAdapter
                    }
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }
    }

    private fun loadListKoord(): MutableList<String>{
        val conn = connect.connection(this)
        val coord = mutableListOf<String>()
        if (conn != null){
            try {
                val query = "SELECT Coord FROM J_CUST WHERE Coord != '' GROUP BY Coord ORDER BY Coord"
                val createState = conn.createStatement()
                val resultSet = createState.executeQuery(query)
                while (resultSet.next()){
                    val cord = resultSet.getString("Coord")
                    coord.add(cord)
                }
            }catch (e:SQLException){
                e.printStackTrace()
            }
        }
        return coord
    }

    private fun loadKoordinator(name: String):MutableList<Koordinator>{
        val conn = connect.connection(this)
        val coord = mutableListOf<Koordinator>()
        if (conn != null){
            try {
                val query = "SELECT\n" +
                        "\tJ_Billing.BillingID,\n" +
                        "\tdbo.UDF_Common_GetPeriodName(J_Billing.[Period]) PeriodName,\n" +
                        "\tC.CustID,\n" +
                        "\tC.RegNo,\n" +
                        "\tC.CName,\n" +
                        "\tC.Addr,\n" +
                        "\tC.AddrNo,\n" +
                        "\tC.AddrRT,\n" +
                        "\tC.AddrRW,\n" +
                        "\tJ_Billing.M1,\n" +
                        "\tJ_Billing.M2,\n" +
                        "\tJ_Billing.Amount,\n" +
                        "\tJ_Billing.TotalTaxRefund,\n" +
                        "\tJ_Billing.TotalFine,\n" +
                        "\tISNULL(EP.TPaid, 0) AmountPaid, \n" +
                        "\t(J_Billing.Amount+J_Billing.TotalFine-J_Billing.TotalTaxRefund)-ISNULL(EP.TPaid, 0) Balance,\n" +
                        "\tC.Coord\n " +
                        " FROM J_Billing\n" +
                        " JOIN J_Cust C ON C.CustID = J_Billing.CustID\n" +
                        "\tLEFT JOIN dbo.VIEW_J_Receipt_EffPaid EP ON EP.ItemType = 'TG' AND EP.ItemID = J_Billing.BillingID\n" +
                        " WHERE EP.TPaid IS NULL AND C.Coord != '' AND C.Coord = ? AND J_Billing.ApprovedDateTime IS NOT NULL AND J_Billing.VoidDateTime IS NULL\n" +
                        " ORDER BY C.Coord"
                val createState = conn.prepareStatement(query)
                createState.setString(1, name)
                val resultSet = createState.executeQuery()
                coord.clear()
                while (resultSet.next()){
                    val koordinator = Koordinator()
                    koordinator.idPelanggan = resultSet.getInt("RegNo")
                    koordinator.namaPelanggan = resultSet.getString("CName")
                    val alamat = resultSet.getString("Addr")
                    val noAlamat = resultSet.getString("AddrNo")
                    val rt = resultSet.getString("AddrRT")
                    val rw = resultSet.getString("AddrRW")
                    koordinator.alamatPelanggan = "$alamat No.$noAlamat Rt:$rt / Rw:$rw"
                    koordinator.periode = resultSet.getString("PeriodName")
                    koordinator.m1 = resultSet.getInt("M1")
                    koordinator.m2 = resultSet.getInt("M2")
                    koordinator.m3 = resultSet.getInt("M2") - resultSet.getInt("M1")
                    koordinator.biaya = resultSet.getInt("Amount")
                    koordinator.denda = resultSet.getInt("TotalFine")
                    koordinator.retur = resultSet.getInt("TotalTaxRefund")
                    koordinator.total = resultSet.getInt("Balance")
                    coord.add(koordinator)
                }
            }catch (e:SQLException){
                e.printStackTrace()
            }
        }
        return coord
    }

    private fun createXlsx(key: String, mDetailPembayaran: MutableList<Koordinator>):String {
        try {
            val root = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Koordinator"
            )
            if (!root.exists()) root.mkdirs()
            val nameFile = key.replace("/", "_")
            val path = File(root,"/$nameFile.xlsx")
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
            val sheet = workbook.createSheet("Koordinator $nameFile")
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
            cell.setCellValue("Periode")
            cell.cellStyle = headerStyle
            cell = row.createCell(5)
            cell.setCellValue("Meter Awal")
            cell.cellStyle = headerStyle
            cell = row.createCell(6)
            cell.setCellValue("Meter AKhir")
            cell.cellStyle = headerStyle
            cell = row.createCell(7)
            cell.setCellValue("Meter Terpakai")
            cell.cellStyle = headerStyle
            cell = row.createCell(8)
            cell.setCellValue("Biaya")
            cell.cellStyle = headerStyle
            cell = row.createCell(9)
            cell.setCellValue("Denda")
            cell.cellStyle = headerStyle
            cell = row.createCell(10)
            cell.setCellValue("Return PPn")
            cell.cellStyle = headerStyle
            cell = row.createCell(11)
            cell.setCellValue("Jumlah")
            cell.cellStyle = headerStyle
            for (i in mDetailPembayaran.indices) {
                row = sheet.createRow(i + 1)
                cell = row.createCell(0)
                cell.setCellValue((i + 1).toString())
                cell = row.createCell(1)
                cell.setCellValue(mDetailPembayaran[i].idPelanggan.toString())
                sheet.setColumnWidth(1, 20 * 200)
                cell = row.createCell(2)
                cell.setCellValue(mDetailPembayaran[i].namaPelanggan)
                sheet.setColumnWidth(2, 30 * 200)
                cell = row.createCell(3)
                cell.setCellValue(mDetailPembayaran[i].alamatPelanggan)
                sheet.setColumnWidth(3, 30 * 350)
                cell = row.createCell(4)
                cell.setCellValue(mDetailPembayaran[i].periode.toString())
                cell = row.createCell(5)
                cell.setCellValue(mDetailPembayaran[i].m1.toString())
                cell = row.createCell(6)
                cell.setCellValue(mDetailPembayaran[i].m2.toString())
                cell = row.createCell(7)
                cell.setCellValue(mDetailPembayaran[i].m3.toString())
                cell = row.createCell(8)
                cell.setCellValue(mDetailPembayaran[i].biaya.toString())
                cell = row.createCell(9)
                cell.setCellValue(mDetailPembayaran[i].denda.toString())
                cell = row.createCell(10)
                cell.setCellValue(mDetailPembayaran[i].retur.toString())
                cell = row.createCell(11)
                cell.setCellValue(((mDetailPembayaran[i].denda!! + mDetailPembayaran[i].biaya!!) - mDetailPembayaran[i].retur!!).toString())
            }
            workbook.write(outputStream)
            outputStream.close()
            return "berhasil"
        } catch (e: IOException) {
            e.printStackTrace()
            return e.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.export, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.export -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Apakah mau export?")
                builder.setPositiveButton("Ya"){_,_ ->
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Export to excel")
                    progressDialog.setMessage("Sedang proses export..")
                    progressDialog.show()
                    lifecycleScope.launch {
                        val async = async(Dispatchers.IO) {
                            createXlsx(selectedCoord, koordinator)
                        }
                        val result = async.await()
                        when(result){
                            "berhasil" -> {
                                progressDialog.dismiss()
                                Utils.showText(this@KoordinatorActivity, "Data berhasil di export")
                            }
                            else -> {
                                progressDialog.dismiss()
                                Utils.showText(this@KoordinatorActivity, "Data gagal di export")
                            }
                        }
                    }
                }

                builder.setNegativeButton("Tidak"){p,_ ->
                    p.dismiss()
                }
                builder.create().show()
            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}