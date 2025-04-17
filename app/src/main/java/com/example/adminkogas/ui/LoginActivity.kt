package com.example.adminkogas.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import com.example.adminkogas.MainActivity
import com.example.adminkogas.R
import com.example.adminkogas.model.DewaAkun
import com.example.adminkogas.utils.FirebaseQuery.myRef
import com.example.adminkogas.utils.SessionLogin
import com.example.adminkogas.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.btnLogin
import kotlinx.android.synthetic.main.activity_login.etPass
import kotlinx.android.synthetic.main.activity_login.etUserLogin
import kotlinx.android.synthetic.main.activity_login.lottie_user
import kotlinx.android.synthetic.main.activity_login.tvHello
import kotlinx.android.synthetic.main.activity_login.tvLoginGagal
import java.util.Base64

class LoginActivity : AppCompatActivity() {
    private lateinit var sessionLogin: SessionLogin
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sessionLogin = SessionLogin(this)

        if (sessionLogin.isLoggedIn()){
            intentClass(MainActivity())
        }

        val animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        tvHello.visibility = View.GONE
        Handler().postDelayed({
            tvHello.visibility = View.VISIBLE
            tvHello.startAnimation(animationFadeOut)
        },1200)

        btnLogin.setOnClickListener{
            val nameUser = etUserLogin.text.toString().trim()
            val passUser = etPass.text.toString().trim()
            tvLoginGagal.visibility = View.GONE
            lottie_user.playAnimation()
            lottie_user.loop(true)
            tvHello.visibility = View.GONE
            myRef.child("Dewa").addValueEventListener(object:
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val mSnapshot = snapshot.getValue(DewaAkun::class.java)
                    if (mSnapshot == null){
                        tvLoginGagal.visibility = View.VISIBLE
                        tvLoginGagal.text = "User Salah, Silahkan cek besar kecil huruf"
                        lottie_user.playAnimation()
                        lottie_user.loop(false)
                        Handler().postDelayed({
                            tvHello.visibility = View.VISIBLE
                            tvHello.startAnimation(animationFadeOut)
                        },1200)
                    }else{
                        val decodedString: String = String(Base64.getDecoder().decode(mSnapshot.pas))
                        if (nameUser == mSnapshot.user && decodedString == passUser){
                            sessionLogin.createLoginSession(nameUser)
                            intentClass(MainActivity())
                        }else{
                            tvLoginGagal.visibility = View.VISIBLE
                            lottie_user.playAnimation()
                            lottie_user.loop(false)
                            Handler().postDelayed({
                                tvHello.visibility = View.VISIBLE
                                tvHello.startAnimation(animationFadeOut)
                            },1200)
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Utils.showText(this@LoginActivity, databaseError.details + " " + databaseError.message)
                }
            })
        }
    }

    private fun intentClass(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}