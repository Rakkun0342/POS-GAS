package com.example.adminkogas.model

class DetailPembayaran {
    var key: String? = null
    var idPrimer: Int? = null
    var id: Int? = null
    var nama: String? = null
    var alamat: String? = null
    var m1: Int? = null
    var m2: Int? = null
    var m3: Int? = null
    var gol: String? = null
    var periode: String? = null
    var biaya: Int? = null
    var refund: Int? = null
    var denda: Int? = null
    var kolek: Int? = null
    var sales: Int? = null
    var dev: Int? = null
    var admin: Int? = null
    var jumlah: Int? = null
    var tipe: String? = null
    var nota: Int? = null
    var tanggal: String? = null
    var createdBy: String? = null

    constructor(){}
    constructor(idP: Int, namaP: String,alamatP: String, m1P: Int, m2P: Int, m3P: Int, golP:String, periodeP:String, biayaP:Int, refundP:Int, dendaP:Int, kolekP: Int, salesP:Int, devP: Int, adminP: Int, jumlahP:Int,tipeP:String, notaP: Int, tanggalP: String, createdByP:String){
        id = idP
        nama = namaP
        alamat = alamatP
        m1 = m1P
        m2 = m2P
        m3 = m3P
        gol = golP
        periode = periodeP
        biaya = biayaP
        refund = refundP
        denda = dendaP
        kolek = kolekP
        sales = salesP
        dev = devP
        admin = adminP
        jumlah = jumlahP
        tipe = tipeP
        nota = notaP
        tanggal = tanggalP
        createdBy = createdByP
    }
}