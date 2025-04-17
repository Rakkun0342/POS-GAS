package com.example.adminkogas.model

data class Koordinator (
    var idPelanggan:Int? = null,
    var namaPelanggan:String? = null,
    var alamatPelanggan:String? = null,
    var periode: String? = null,
    var m1: Int? = null,
    var m2: Int? = null,
    var m3: Int? = null,
    var biaya: Int? = null,
    var denda: Int? = null,
    var retur: Int? = null,
    var total: Int? = null
)