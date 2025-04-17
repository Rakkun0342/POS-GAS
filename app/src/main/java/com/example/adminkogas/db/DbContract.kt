package com.example.adminkogas.db

import android.provider.BaseColumns

class DbContract {
    internal class NoteColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "Pembayaran"
            const val ID = "id"
            const val KEY = "key"
            const val IDPELANGGAN = "id_pelanggan"
            const val NAMA = "nama"
            const val ALAMAT = "alamat"
            const val METERAWAL = "M1"
            const val METERAKHIR = "M2"
            const val METERTERPAKAI = "M3"
            const val GOLONGAN = "golongan"
            const val PERIODE = "periode"
            const val BIAYA = "biaya"
            const val REFUNDPPN = "refund"
            const val KOLEK = "kolek"
            const val DENDA = "denda"
            const val SALES = "sales"
            const val DEV = "dev"
            const val ADMIN = "admin"
            const val JUMLAH = "jumlah"
            const val TIPE = "tipe"
            const val NOTA = "nota"
            const val TANGGAL = "tanggal"
            const val CREATEDBY = "createdBy"
        }
    }
}