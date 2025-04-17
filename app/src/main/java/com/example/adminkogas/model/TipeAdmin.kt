package com.example.adminkogas.model

class TipeAdmin {
    var key: String? = null
    var sales: Int? = null
    var dev: Int? = null
    var admin: Int? = null
    var nama: String? = null

    constructor(){}
    constructor(salesP:Int, devP:Int, adminP:Int, namaP:String){
        sales = salesP
        dev = devP
        admin = adminP
        nama = namaP
    }
}