package com.example.adminkogas.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminkogas.R
import com.example.adminkogas.adapter.DataUserAdapter
import com.example.adminkogas.model.Keuntungan
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.server.ConnectAstro
import com.example.adminkogas.utils.FirebaseQuery
import com.example.adminkogas.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_data.rvDataUser
import kotlinx.android.synthetic.main.activity_history.rvHistory
import kotlinx.android.synthetic.main.activity_history.toolbarHistory
import kotlinx.android.synthetic.main.layout_empty.layoutEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use
import java.sql.SQLException

class HistoryActivity : AppCompatActivity() {

    private lateinit var dataUserAdapter: DataUserAdapter
    private lateinit var itemUser: MutableList<UserFirebase>
    private lateinit var connectAstro: ConnectAstro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbarHistory)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "History"
        connectAstro = ConnectAstro()

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.setHasFixedSize(false)

        val his = intent.getBooleanExtra("his", false)
        if (his){
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO){
                    getTanggal()
                }
                result.sortByDescending { it.id }
                dataUserAdapter = DataUserAdapter(result, true)
                dataUserAdapter.harian = true
                rvHistory.adapter = dataUserAdapter
                dataUserAdapter.setOnItemClickCallback(object : DataUserAdapter.OnItemClickCallback{
                    override fun onItemClicked(items: UserFirebase) {
                        val intent = Intent(this@HistoryActivity, DetailActivity::class.java)
                        intent.putExtra("kondisi", "harian")
                        intent.putExtra("nama", items.nama)
                        startActivity(intent)
                    }
                })
                layoutEmpty.visibility = View.GONE
            }
        }else{
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO){
                    getCreated()
                }
                dataUserAdapter = DataUserAdapter(result, false)
                rvHistory.adapter = dataUserAdapter
                dataUserAdapter.setOnItemClickCallback(object : DataUserAdapter.OnItemClickCallback{
                    override fun onItemClicked(items: UserFirebase) {
                        val intent = Intent(this@HistoryActivity, DetailActivity::class.java)
                        intent.putExtra("kondisi", "detail")
                        intent.putExtra("nama", items.nama)
                        startActivity(intent)
                    }
                })
                layoutEmpty.visibility = View.GONE
            }
        }
    }

    private fun getTanggal():MutableList<UserFirebase>{
        val conn = connectAstro.connection(this)
        val item = mutableListOf<UserFirebase>()
        if (conn != null){
            try {
                val query = "SELECT MAX(Nomor) AS Nom, SUM(Jumlah) AS Jumlah, FORMAT(CONVERT(DATE,Tanggal), 'yyyy-MM-dd') AS Tanggal " +
                        "FROM JS_HISTPROPER WHERE VoidByAdmin IS NULL " +
                        "GROUP BY CAST(Tanggal AS DATE)"
                val statemen = conn.createStatement()
                val resultSet = statemen.executeQuery(query)
                while (resultSet.next()){
                    val userFirebase = UserFirebase()
                    userFirebase.nama = resultSet.getString("Tanggal")
                    userFirebase.id = resultSet.getString("Nom")
                    userFirebase.deposit = resultSet.getInt("Jumlah")
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

    private fun getUserFirebase(){
        FirebaseQuery.myRef.child("Pembayaran").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemUser = mutableListOf()
                for (mDataSnapshot in snapshot.children) {
                    Log.d("List", "onDataChange: ${mDataSnapshot.getValue(UserFirebase::class.java)} $")
                    val barang = mDataSnapshot.getValue(UserFirebase::class.java)
                    if (barang != null) {
                        barang.key = mDataSnapshot.key
                        itemUser.add(barang)
                    }

                    dataUserAdapter = DataUserAdapter(itemUser, false)
                    rvHistory.adapter = dataUserAdapter
                    dataUserAdapter.setOnItemClickCallback(object : DataUserAdapter.OnItemClickCallback{
                        override fun onItemClicked(items: UserFirebase) {
                            val intent = Intent(this@HistoryActivity, DetailActivity::class.java)
                            intent.putExtra("nama", items.key)
                            startActivity(intent)
                        }
                    })
                    layoutEmpty.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Utils.showText(
                    this@HistoryActivity,
                    databaseError.details + " " + databaseError.message
                )
            }
        })
    }
}