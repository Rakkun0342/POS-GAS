package com.example.adminkogas.model

class DewaAkun {
    var user: String? = null
    var pas: String? = null
    var admin: Int? = null
    var dev: Int? = null

    constructor(){}
    constructor(userP: String, passP: String, adminP:Int, devP: Int){
        user = userP
        pas = passP
        admin = adminP
        dev = devP
    }
}