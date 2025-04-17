package com.example.adminkogas.model

class UserFirebase {
    var key: String? = null
    var id: String? = null
    var nama: String? = null
    var nomor: String? = null
    var pass: String? = null
    var alamat: String? = null
    var kolektif: Int? = null
    var tipe: String? = null
    var deposit: Int? = null

    constructor(){}

    constructor(idUser: String?, namaUser: String?, nomorUser:String?, passUser:String?, alamatUser:String?, kolek: Int?,type: String?, depositUser:Int?){
        id = idUser
        nama = namaUser
        nomor = nomorUser
        pass = passUser
        alamat = alamatUser
        kolektif = kolek
        tipe = type
        deposit = depositUser
    }
}