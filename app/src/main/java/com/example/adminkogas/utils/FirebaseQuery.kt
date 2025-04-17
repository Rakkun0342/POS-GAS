package com.example.adminkogas.utils

import android.content.Context
import android.util.Log
import com.example.adminkogas.model.Pembayaran
import com.example.adminkogas.model.TipeAdmin
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.utils.Utils.showText
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseQuery {

    private val mDatabase = FirebaseDatabase.getInstance()
    val myRef = mDatabase.getReference("Jargas")

    fun submit(context: Context, key: String, userFirebase: UserFirebase){
        myRef.child("ListUser").child(key).setValue(userFirebase).addOnSuccessListener {
            showText(context, "Berhasil Ditambahkan")
        }
    }

    fun submitTipe(context: Context, key: String, tipeAdmin: TipeAdmin){
        myRef.child("Tipe").child(key).setValue(tipeAdmin).addOnSuccessListener {
            showText(context, "Berhasil Ditambahkan")
        }
    }

    fun update(context: Context, key: String, userFirebase: UserFirebase){
        myRef.child("ListUser").child(key).setValue(userFirebase).addOnSuccessListener {
            showText(context, "Berhasil Diperbarui")
        }
    }

    fun updateTipe(context: Context, key: String, tipeAdmin: TipeAdmin){
        myRef.child("Tipe").child(key).setValue(tipeAdmin).addOnSuccessListener {
            showText(context, "Berhasil Diperbarui")
        }
    }

    fun remove(context: Context, key: String, chil: String){
        myRef.child(chil).child(key).removeValue().addOnSuccessListener {
            showText(context, "Berhasil Dihapus")
        }
    }

    fun submitPembayaran(key: String, pembayaran: Pembayaran){
        myRef.child("Pembayaran").child(key).push().setValue(pembayaran).addOnSuccessListener {}
    }
}