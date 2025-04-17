package com.example.adminkogas.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminkogas.R
import com.example.adminkogas.adapter.DataUserAdapter
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.utils.FirebaseQuery
import com.example.adminkogas.utils.FirebaseQuery.myRef
import com.example.adminkogas.utils.FirebaseQuery.update
import com.example.adminkogas.utils.Utils
import com.example.adminkogas.utils.Utils.showText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_saldo.rvSaldo
import kotlinx.android.synthetic.main.activity_saldo.toolbarSaldo
import kotlinx.android.synthetic.main.layout_empty.layoutEmpty

class SaldoActivity : AppCompatActivity() {

    private lateinit var dataUserAdapter: DataUserAdapter
    private lateinit var itemUser: MutableList<UserFirebase>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saldo)
        setSupportActionBar(toolbarSaldo)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saldo"

        rvSaldo.layoutManager = LinearLayoutManager(this)
        rvSaldo.setHasFixedSize(false)

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
                    dataUserAdapter = DataUserAdapter(itemUser, true)
                    rvSaldo.adapter = dataUserAdapter
                    dataUserAdapter.setOnItemClickCallback(object : DataUserAdapter.OnItemClickCallback{
                        override fun onItemClicked(items: UserFirebase) {
                            val builder = AlertDialog.Builder(this@SaldoActivity)
                            builder.setTitle("Update Saldo")
                            val view     = View.inflate(this@SaldoActivity, R.layout.input_data, null)
                            val tvId     = view.findViewById<LinearLayout>(R.id.llId)
                            val tvNama   = view.findViewById<LinearLayout>(R.id.llNama)
                            val tvNomor  = view.findViewById<LinearLayout>(R.id.llPhone)
                            val tvPass   = view.findViewById<LinearLayout>(R.id.llPass)
                            val tvAlamat = view.findViewById<LinearLayout>(R.id.llAlamat)
                            val llDepo   = view.findViewById<LinearLayout>(R.id.llDepo)
                            val llKolek  = view.findViewById<LinearLayout>(R.id.llKolektif)
                            val tvDeposit  = view.findViewById<TextView>(R.id.tvDataDeposit)
                            val tvKolektif  = view.findViewById<TextView>(R.id.tvDataKolektif)
                            val tvKolek  = view.findViewById<EditText>(R.id.etKolektif)
                            val tvDepo   = view.findViewById<EditText>(R.id.etDepo)
                            val tvType   = view.findViewById<RelativeLayout>(R.id.llType)
                            tvId.visibility = View.GONE
                            tvNama.visibility = View.GONE
                            tvNomor.visibility = View.GONE
                            tvPass.visibility = View.GONE
                            tvAlamat.visibility = View.GONE
                            tvType.visibility = View.GONE
                            tvDeposit.visibility = View.VISIBLE
                            tvKolektif.visibility = View.VISIBLE
                            llDepo.visibility = View.VISIBLE
                            llKolek.visibility = View.VISIBLE
                            tvDepo.setText(items.deposit.toString())
                            tvKolek.setText(items.kolektif.toString())
                            builder.setView(view)
                            builder.setPositiveButton("Update"){_,_->
                                val userFirebase = UserFirebase()
                                userFirebase.id   = items.id
                                userFirebase.nama = items.nama
                                userFirebase.nomor = items.nomor
                                userFirebase.pass = items.pass
                                userFirebase.alamat = items.alamat
                                userFirebase.tipe = items.tipe
                                userFirebase.kolektif = tvKolek.text.toString().toInt()
                                val saldo = tvDepo.text.toString().trim()
                                if (saldo.toInt() <= 100000000){
                                    userFirebase.deposit = tvDepo.text.toString().toInt()
                                    update(
                                        this@SaldoActivity,
                                        items.key.toString(),
                                        userFirebase
                                    )
                                }else{
                                    showText(this@SaldoActivity, "Batas maksimal deposit Rp.100.000.000")
                                }
                            }
                            builder.setNegativeButton("Batal"){p,_->
                                p.dismiss()
                            }
                            builder.create().show()
                        }
                    })
                    layoutEmpty.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showText(
                    this@SaldoActivity,
                    databaseError.details + " " + databaseError.message
                )
            }
        })
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