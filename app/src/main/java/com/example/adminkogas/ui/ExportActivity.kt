package com.example.adminkogas.ui

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminkogas.R
import com.example.adminkogas.adapter.DataUserAdapter
import com.example.adminkogas.adapter.ExportAdapter
import com.example.adminkogas.db.DbContract
import com.example.adminkogas.db.DbQuery
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.model.Keuntungan
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.server.ConnectAstro
import com.example.adminkogas.utils.FirebaseQuery
import com.example.adminkogas.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.android.synthetic.main.layout_empty.layoutEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.sql.SQLException

class ExportActivity : AppCompatActivity() {

    private lateinit var exportAdapter: ExportAdapter
    private lateinit var itemUser: MutableList<UserFirebase>
    private lateinit var dbQuery: DbQuery
    private lateinit var connectAstro: ConnectAstro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        setSupportActionBar(toolbarExport)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Export"
        dbQuery = DbQuery.getInstance(this)
        connectAstro = ConnectAstro()
        rvExport.layoutManager = LinearLayoutManager(this)
        rvExport.setHasFixedSize(false)

        val progressBar = ProgressDialog(this)
        progressBar.setTitle("Mohon Tunggu")
        progressBar.setMessage("Sedang mengambil data..")
        progressBar.show()

        lifecycleScope.launch {
            val async = async(Dispatchers.IO) {
                getBulan()
            }
            val deffren = async.await()
            progressBar.dismiss()
            exportAdapter = ExportAdapter(deffren)
            rvExport.adapter = exportAdapter
            exportAdapter.setOnItemClickCallback(object : ExportAdapter.OnItemClickCallback{
                override fun onItemClicked(items: UserFirebase) {
                    val intent = Intent(this@ExportActivity, DetailActivity::class.java)
                    intent.putExtra("kondisi", "export")
                    intent.putExtra("key", items.nama)
                    startActivity(intent)
                }
            })
        }
        layoutEmpty.visibility = View.GONE
    }

    private fun getBulan():MutableList<UserFirebase>{
        val conn = connectAstro.connection(this)
        val item = mutableListOf<UserFirebase>()
        if (conn != null){
            try {
                val query = "SELECT FORMAT(CONVERT(DATE,Tanggal), 'yyyy-MMM') AS Tanggal, SUM(Jumlah) AS Jumlah " +
                        "FROM JS_HISTPROPER WHERE VoidByAdmin IS NULL " +
                        "GROUP BY FORMAT(CAST(Tanggal AS DATE), 'yyyy-MMM')"
                val statemen = conn.createStatement()
                val resultSet = statemen.executeQuery(query)
                while (resultSet.next()){
                    val userFirebase = UserFirebase()
                    userFirebase.deposit = resultSet.getInt("Jumlah")
                    userFirebase.nama = resultSet.getString("Tanggal")
                    item.add(userFirebase)
                }
            }catch (e: SQLException){
                e.printStackTrace()
            }
        }
        return item
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}