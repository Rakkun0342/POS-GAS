package com.example.adminkogas.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.manager.Lifecycle
import com.example.adminkogas.R
import com.example.adminkogas.adapter.DataUserAdapter
import com.example.adminkogas.adapter.TipeAdapter
import com.example.adminkogas.model.TipeAdmin
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.utils.FirebaseQuery.myRef
import com.example.adminkogas.utils.FirebaseQuery.remove
import com.example.adminkogas.utils.FirebaseQuery.submit
import com.example.adminkogas.utils.FirebaseQuery.submitTipe
import com.example.adminkogas.utils.FirebaseQuery.update
import com.example.adminkogas.utils.FirebaseQuery.updateTipe
import com.example.adminkogas.utils.Utils.showText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_data.faTambah
import kotlinx.android.synthetic.main.activity_data.rvDataUser
import kotlinx.android.synthetic.main.activity_data.toolbarData
import kotlinx.android.synthetic.main.input_data.llData
import kotlinx.android.synthetic.main.input_data.llTipe
import kotlinx.android.synthetic.main.layout_empty.layoutEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64

class DataActivity : AppCompatActivity() {

    private lateinit var dataUserAdapter: DataUserAdapter
    private lateinit var tipeAdapter: TipeAdapter
    private lateinit var itemUser: MutableList<UserFirebase>
    private lateinit var itemTipe: MutableList<TipeAdmin>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        setSupportActionBar(toolbarData)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvDataUser.layoutManager = LinearLayoutManager(this)
        rvDataUser.setHasFixedSize(false)

        val builder = AlertDialog.Builder(this)
        var position = 0

        val getInten = intent.getBooleanExtra("data", false)
        if (getInten){
            supportActionBar?.title = "List Pengguna"
            myRef.child("ListUser").addValueEventListener(object: ValueEventListener {
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
                        rvDataUser.adapter = dataUserAdapter
                        dataUserAdapter.setOnItemClickCallback(object : DataUserAdapter.OnItemClickCallback{
                            override fun onItemClicked(items: UserFirebase) {
                                val builder = AlertDialog.Builder(this@DataActivity)
                                builder.setTitle("Update User")
                                val view     = View.inflate(this@DataActivity, R.layout.input_data, null)
                                val tvId     = view.findViewById<EditText>(R.id.etId)
                                val tvNama   = view.findViewById<EditText>(R.id.etNamaUser)
                                val tvNomor  = view.findViewById<EditText>(R.id.etPhone)
                                val tvPass   = view.findViewById<EditText>(R.id.etPassword)
                                val tvAlamat = view.findViewById<EditText>(R.id.etAlamat)
                                val llKolek  = view.findViewById<LinearLayout>(R.id.llKolektif)
                                val tvKolek  = view.findViewById<EditText>(R.id.etKolektif)
                                val llDepo   = view.findViewById<LinearLayout>(R.id.llDepo)
                                val tvDepo   = view.findViewById<EditText>(R.id.etDepo)
                                val tvType   = view.findViewById<AppCompatSpinner>(R.id.etType)
                                val llData   = view.findViewById<LinearLayout>(R.id.llData)
                                val llTipe   = view.findViewById<LinearLayout>(R.id.llTipe)
                                llTipe.visibility = View.GONE
                                llData.visibility = View.VISIBLE
                                val listTipe = mutableListOf<TipeAdmin>()
                                var tipeSelect = ""
                                lifecycleScope.launch {
                                    val async = async(Dispatchers.IO) {
                                        myRef.child("Tipe").addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                listTipe.clear()
                                                for (mSnapshot in snapshot.children) {
                                                    val tipe = mSnapshot.getValue(TipeAdmin::class.java)
                                                    if (tipe != null) {
                                                        tipe.key = mSnapshot.key
                                                        listTipe.add(tipe)
                                                    }
                                                }
                                                val listAdmin = mutableListOf<String>()
                                                listTipe.forEach {
                                                    listAdmin.add(it.nama.toString())
                                                }
                                                val adapterTipe = ArrayAdapter<String>(this@DataActivity, android.R.layout.simple_spinner_item, listAdmin)
                                                adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                                tvType.adapter = adapterTipe
                                                tvType.setSelection(adapterTipe.getPosition(items.tipe))
                                                tvType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                                                        tipeSelect = listAdmin[position]
                                                    }
                                                    override fun onNothingSelected(p: AdapterView<*>?) {}
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                                showText(this@DataActivity, error.details + " " + error.message)
                                            }
                                        })
                                    }
                                    async.await()
                                }

                                llDepo.visibility = View.GONE
                                llKolek.visibility = View.GONE

                                tvId.setText(items.id.toString())
                                tvNama.setText(items.nama.toString())
                                tvNomor.setText(items.nomor.toString())
                                tvPass.setText(items.pass.toString())
                                tvAlamat.setText(items.alamat.toString())

                                builder.setView(view)
                                builder.setCancelable(false)
                                builder.setPositiveButton("Update"){_,_->
                                    val userFirebase = UserFirebase()
                                    userFirebase.id   = tvId.text.toString().trim()
                                    userFirebase.nama = tvNama.text.toString().trim()
                                    userFirebase.nomor = tvNomor.text.toString().trim()
                                    userFirebase.pass = tvPass.text.toString().trim()
                                    userFirebase.alamat = tvAlamat.text.toString().trim()
                                    userFirebase.tipe = tipeSelect
                                    userFirebase.kolektif = items.kolektif
                                    userFirebase.deposit = items.deposit
                                    update(this@DataActivity,items.key.toString(), userFirebase)
                                }
                                builder.setNeutralButton("Batal"){p,_->
                                    p.dismiss()
                                }
                                builder.setNegativeButton("Hapus"){_,_ ->
                                    val buil = AlertDialog.Builder(this@DataActivity)
                                    buil.setTitle("Apakah anda yakin mau menghapus ${items.nama} ?")
                                    buil.setPositiveButton("Ya"){_,_ ->
                                        remove(this@DataActivity,items.key.toString(), "ListUser")
                                    }
                                    buil.setNegativeButton("Tidak"){p,_ ->
                                        p.dismiss()
                                    }
                                    buil.create().show()
                                }
                                builder.create().show()
                            }
                        })
                        layoutEmpty.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showText(this@DataActivity, databaseError.details + " " + databaseError.message)
                }
            })

            faTambah.setOnClickListener{
                builder.setTitle("Tambah User")
                val view     = View.inflate(this, R.layout.input_data, null)
                val tvId     = view.findViewById<EditText>(R.id.etId)
                val tvNama   = view.findViewById<EditText>(R.id.etNamaUser)
                val tvNomor  = view.findViewById<EditText>(R.id.etPhone)
                val tvPass   = view.findViewById<EditText>(R.id.etPassword)
                val tvAlamat = view.findViewById<EditText>(R.id.etAlamat)
                val tvKolek  = view.findViewById<EditText>(R.id.etKolektif)
                val tvDepo   = view.findViewById<EditText>(R.id.etDepo)
                val tvType   = view.findViewById<AppCompatSpinner>(R.id.etType)
                val llData   = view.findViewById<LinearLayout>(R.id.llData)
                val llTipe   = view.findViewById<LinearLayout>(R.id.llTipe)
                val rand = (0..10000).random()
                tvId.setText(rand.toString())
                llTipe.visibility = View.GONE
                llData.visibility = View.VISIBLE
                val listTipe = mutableListOf<TipeAdmin>()
                var tipeSelect = ""
                lifecycleScope.launch {
                    val async = async(Dispatchers.IO) {
                        myRef.child("Tipe").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                listTipe.clear()
                                for (mSnapshot in snapshot.children) {
                                    val tipe = mSnapshot.getValue(TipeAdmin::class.java)
                                    if (tipe != null) {
                                        tipe.key = mSnapshot.key
                                        listTipe.add(tipe)
                                    }
                                }
                                val listAdmin = mutableListOf<String>()
                                listTipe.forEach {
                                    listAdmin.add(it.nama.toString())
                                }
                                val adapterTipe = ArrayAdapter<String>(this@DataActivity, android.R.layout.simple_spinner_item, listAdmin)
                                adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                tvType.adapter = adapterTipe
                                tvType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                                        tipeSelect = listAdmin[position]
                                    }
                                    override fun onNothingSelected(p: AdapterView<*>?) {}
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                showText(this@DataActivity, error.details + " " + error.message)
                            }
                        })
                    }
                    async.await()
                }

                builder.setView(view)
                builder.setCancelable(false)
                builder.setPositiveButton("Tambah"){_,_->
                    if (tvId.text.toString().trim().isEmpty() || tvNama.text.toString().trim().isEmpty() || tvNomor.text.toString().trim().isEmpty() || tvAlamat.text.toString().trim().isEmpty() || tvPass.toString().trim().isEmpty() || tvDepo.text.toString().trim().isEmpty()){
                        showText(this, "Semua Wajib diisi")
                    }else{
                        val userFirebase = UserFirebase()
                        val idUser = tvId.text.toString().trim()
                        val nama = tvNama.text.toString().trim()
                        userFirebase.id = idUser
                        userFirebase.nama = nama
                        userFirebase.nomor = tvNomor.text.toString().trim()
                        val encodedString: String = Base64.getEncoder().encodeToString(tvPass.text.toString().trim().toByteArray())
                        userFirebase.pass = encodedString
                        userFirebase.alamat = tvAlamat.text.toString().trim()
                        userFirebase.kolektif = tvKolek.text.toString().toInt()
                        userFirebase.tipe = tipeSelect
                        val saldo = tvDepo.text.toString().trim()
                        if (saldo.toInt() <= 100000000){
                            userFirebase.deposit = tvDepo.text.toString().toInt()
                            submit(this,nama, userFirebase)
                        }else{
                            showText(this, "Batas maksimal deposit Rp.100.000.000")
                        }
                    }
                }
                builder.setNegativeButton("Batal"){p,_->
                    p.dismiss()
                }
                builder.create().show()
            }
        }else{
            supportActionBar?.title = "List Tipe"
            myRef.child("Tipe").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemTipe = mutableListOf()
                    for (mDataSnapshot in snapshot.children) {
                        position++
                        Log.d("List", "onDataChange: ${mDataSnapshot.getValue(TipeAdmin::class.java)} $")
                        val barang = mDataSnapshot.getValue(TipeAdmin::class.java)
                        if (barang != null) {
                            barang.key = mDataSnapshot.key
                            itemTipe.add(barang)
                        }
                        tipeAdapter = TipeAdapter(itemTipe)
                        rvDataUser.adapter = tipeAdapter
                        tipeAdapter.setOnItemClickCallback(object : TipeAdapter.OnItemClickCallback{
                            override fun onItemClicked(items: TipeAdmin) {
                                val builder = AlertDialog.Builder(this@DataActivity)
                                builder.setTitle("Update Tipe")
                                val view     = View.inflate(this@DataActivity, R.layout.input_data, null)
                                val dev      = view.findViewById<EditText>(R.id.etTipeDev)
                                val tipeNama = view.findViewById<EditText>(R.id.etTipeNama)
                                val tipeSales= view.findViewById<EditText>(R.id.etTipeSales)
                                val tipeAdm  = view.findViewById<EditText>(R.id.etTipeAdmin)
                                val llData   = view.findViewById<LinearLayout>(R.id.llData)
                                val llTipe   = view.findViewById<LinearLayout>(R.id.llTipe)
                                llTipe.visibility = View.VISIBLE
                                llData.visibility = View.GONE
                                dev.setText(items.dev.toString())
                                tipeAdm.setText(items.admin.toString())
                                tipeNama.setText(items.nama.toString())
                                tipeSales.setText(items.sales.toString())

                                builder.setView(view)
                                builder.setCancelable(false)
                                builder.setPositiveButton("Update"){_,_->
                                    val tipe = TipeAdmin()
                                    tipe.nama = tipeNama.text.toString().trim()
                                    tipe.dev = items.dev
                                    tipe.sales = tipeSales.text.toString().toInt()
                                    tipe.admin = tipeAdm.text.toString().toInt()
                                    updateTipe(this@DataActivity,items.key.toString(), tipe)
                                }
                                builder.setNeutralButton("Batal"){p,_->
                                    p.dismiss()
                                }
                                builder.setNegativeButton("Hapus"){_,_ ->
                                    val buil = AlertDialog.Builder(this@DataActivity)
                                    buil.setTitle("Apakah anda yakin mau menghapus ${items.nama} ?")
                                    buil.setPositiveButton("Ya"){_,_ ->
                                        remove(this@DataActivity,items.key.toString(), "Tipe")
                                    }
                                    buil.setNegativeButton("Tidak"){p,_ ->
                                        p.dismiss()
                                    }
                                    buil.create().show()
                                }
                                builder.create().show()
                            }
                        })
                        layoutEmpty.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showText(this@DataActivity, databaseError.details + " " + databaseError.message)
                }
            })

            faTambah.setOnClickListener{
                builder.setTitle("Tambah Tipe")
                val view     = View.inflate(this@DataActivity, R.layout.input_data, null)
                val dev      = view.findViewById<EditText>(R.id.etTipeDev)
                val tipeNama = view.findViewById<EditText>(R.id.etTipeNama)
                val tipeSales= view.findViewById<EditText>(R.id.etTipeSales)
                val tipeAdm  = view.findViewById<EditText>(R.id.etTipeAdmin)
                val llData   = view.findViewById<LinearLayout>(R.id.llData)
                val llTipe   = view.findViewById<LinearLayout>(R.id.llTipe)
                llTipe.visibility = View.VISIBLE
                llData.visibility = View.GONE
                dev.setText("150")

                builder.setView(view)
                builder.setCancelable(false)
                builder.setPositiveButton("Tambah"){_,_->
                    if (dev.text.toString().trim().isEmpty() || tipeNama.text.toString().trim().isEmpty() || tipeSales.text.toString().trim().isEmpty() || tipeAdm.text.toString().trim().isEmpty()){
                        showText(this@DataActivity, "Semua Wajib diisi")
                    }else{
                        val tipe = TipeAdmin()
                        tipe.nama  = tipeNama.text.toString().trim()
                        tipe.dev   = dev.text.toString().toInt()
                        tipe.sales = tipeSales.text.toString().toInt()
                        tipe.admin = tipeAdm.text.toString().toInt()
                        submitTipe(this, tipeNama.text.toString(), tipe)
                    }
                }
                builder.setNegativeButton("Batal"){p,_->
                    p.dismiss()
                }
                builder.create().show()
            }
        }
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