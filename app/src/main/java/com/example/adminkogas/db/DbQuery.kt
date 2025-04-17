package com.example.adminkogas.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.model.Keuntungan

class DbQuery(context: Context) {
    private var db = DbHelper(context).writableDatabase
    companion object{
        private const val TABLE_NAME = DbContract.NoteColumns.TABLE_NAME
        private var INSTANCE: DbQuery? = null

        fun getInstance(context: Context): DbQuery =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DbQuery(context)
            }
    }

    fun close() {
        db.close()

        if (db.isOpen)
            db.close()
    }

    fun insert(values: ContentValues?): Long {
        return db.insert(TABLE_NAME, null, values)
    }

    fun update(id: String, values: ContentValues?): Int{
        return db.update(TABLE_NAME, values, "${DbContract.NoteColumns.KEY} = ?", arrayOf(id))
    }

    fun delete() {
        db.delete(TABLE_NAME, null, null)
    }

    fun queryById(id: String): Cursor {
        return db.query(
            TABLE_NAME,
            null,
            "${DbContract.NoteColumns.KEY} = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }

    fun getAllItem(id: String): Cursor {
        return db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    fun queryGetBulanan():MutableList<String>{
        val items = mutableListOf<String>()
        val query = db.rawQuery("SELECT CASE STRFTIME('%m',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '01' THEN 'January' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '02' THEN 'February' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '03' THEN 'March' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '04' THEN 'April' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '05' THEN 'May' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '06' THEN 'June' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '07' THEN 'July' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '08' THEN 'August' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '09' THEN 'September' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '10' THEN 'October' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '11' THEN 'November' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '12' THEN 'December' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "ELSE '' END AS Tanggal FROM $TABLE_NAME " +
                "GROUP BY STRFTIME('%m-%Y',${DbContract.NoteColumns.TANGGAL})", null)

        query.apply {
            while (moveToNext()){
                val tanggal = getString(getColumnIndexOrThrow("Tanggal"))
                items.add(tanggal)
            }
        }
        return items
    }

    fun queryDetailBulanan(tanggal: String):MutableList<DetailPembayaran>{
        val items = mutableListOf<DetailPembayaran>()
        val query = db.rawQuery("SELECT *, " +
                "CASE STRFTIME('%m',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '01' THEN 'January' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '02' THEN 'February' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '03' THEN 'March' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '04' THEN 'April' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '05' THEN 'May' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '06' THEN 'June' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '07' THEN 'July' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '08' THEN 'August' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '09' THEN 'September' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '10' THEN 'October' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '11' THEN 'November' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "WHEN '12' THEN 'December' || STRFTIME('-%Y',${DbContract.NoteColumns.TANGGAL}) " +
                "ELSE '' END AS FormattedDate FROM $TABLE_NAME " +
                "WHERE FormattedDate = '$tanggal'", null)
        query.apply {
            items.clear()
            while (moveToNext()){
                val detailPembayaran    = DetailPembayaran()
                detailPembayaran.key    = getString(getColumnIndexOrThrow(DbContract.NoteColumns.KEY))
                detailPembayaran.idPrimer = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.ID))
                detailPembayaran.id     = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.IDPELANGGAN))
                detailPembayaran.nama   = getString(getColumnIndexOrThrow(DbContract.NoteColumns.NAMA))
                detailPembayaran.alamat = getString(getColumnIndexOrThrow(DbContract.NoteColumns.ALAMAT))
                detailPembayaran.m1     = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.METERAWAL))
                detailPembayaran.m2     = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.METERAKHIR))
                detailPembayaran.m3     = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.METERTERPAKAI))
                detailPembayaran.gol    = getString(getColumnIndexOrThrow(DbContract.NoteColumns.GOLONGAN))
                detailPembayaran.periode= getString(getColumnIndexOrThrow(DbContract.NoteColumns.PERIODE))
                detailPembayaran.biaya  = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.BIAYA))
                detailPembayaran.refund = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.REFUNDPPN))
                detailPembayaran.denda  = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.DENDA))
                detailPembayaran.kolek  = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.KOLEK))
                detailPembayaran.sales  = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.SALES))
                detailPembayaran.dev    = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.DEV))
                detailPembayaran.admin  = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.ADMIN))
                detailPembayaran.jumlah = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.JUMLAH))
                detailPembayaran.tipe   = getString(getColumnIndexOrThrow(DbContract.NoteColumns.TIPE))
                detailPembayaran.nota   = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.NOTA))
                detailPembayaran.tanggal= getString(getColumnIndexOrThrow(DbContract.NoteColumns.TANGGAL))
                detailPembayaran.createdBy  = getString(getColumnIndexOrThrow(DbContract.NoteColumns.CREATEDBY))
                items.add(detailPembayaran)
            }
        }
        return items
    }

    fun getHarian():MutableList<Keuntungan>{
        val items = mutableListOf<Keuntungan>()
        val query = db.rawQuery("SELECT DATE(${DbContract.NoteColumns.TANGGAL}) AS Tanggal, COUNT(*) AS Count, SUM(${DbContract.NoteColumns.JUMLAH}) AS Jumlah FROM $TABLE_NAME GROUP BY DATE(${DbContract.NoteColumns.TANGGAL})", null)
        query.apply {
            while (moveToNext()){
                val tanggal = getString(getColumnIndexOrThrow("Tanggal"))
                val pay = getInt(getColumnIndexOrThrow("Jumlah"))
                val count = getInt(getColumnIndexOrThrow("Count"))
                items.add(Keuntungan(tanggal, count, pay))
            }
        }
        return items
    }

    fun queryCekKey(id:String): Int{
        var hasil = 0
        val query = db.rawQuery("SELECT CASE WHEN EXISTS (SELECT 1 FROM $TABLE_NAME WHERE ${DbContract.NoteColumns.KEY} = '$id' ) " +
                "THEN '1' ELSE '0' END " +
                "AS ${DbContract.NoteColumns.KEY}", null)
        query.apply {
            while (query.moveToNext()){
                hasil = getInt(getColumnIndexOrThrow(DbContract.NoteColumns.KEY))
            }
        }
        return hasil
    }

}