package com.example.adminkogas.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ADMIN"
        const val DATABASE_VERSION = 1

        private const val CREATE_TABLE_ = "CREATE TABLE ${DbContract.NoteColumns.TABLE_NAME}" +
                " (${DbContract.NoteColumns.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${DbContract.NoteColumns.KEY} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.IDPELANGGAN} TEXT NOT NULL,"+
                " ${DbContract.NoteColumns.NAMA} TEXT NOT NULL,"+
                " ${DbContract.NoteColumns.ALAMAT} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.METERAWAL} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.METERAKHIR} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.METERTERPAKAI} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.GOLONGAN} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.PERIODE} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.BIAYA} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.REFUNDPPN} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.DENDA} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.KOLEK} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.SALES} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.DEV} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.ADMIN} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.JUMLAH} INTEGER NOT NULL," +
                " ${DbContract.NoteColumns.TIPE} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.NOTA} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.TANGGAL} TEXT NOT NULL," +
                " ${DbContract.NoteColumns.CREATEDBY} TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS $CREATE_TABLE_")
        onCreate(db)
    }
}