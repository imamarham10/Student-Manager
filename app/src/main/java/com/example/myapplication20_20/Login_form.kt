package com.example.myapplication20_20

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup_form.*


class Login_form : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_form)
        supportActionBar?.setTitle("Login Form")

        mAuth = FirebaseAuth.getInstance();
    }

    fun buRegister(view: View)
    {
        val intent = Intent(this,Signup_form::class.java)
        startActivity(intent)
    }

    fun buLogin(view: View)
    {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        LoginToFirebase(email, password)

    }

    fun LoginToFirebase(email:String,password:String)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(applicationContext,"Please enter email!",Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(applicationContext,"Please Enter the password",Toast.LENGTH_LONG).show()
            return
        }
        if(password.length<8)
        {
            Toast.makeText(applicationContext,"Password too short",Toast.LENGTH_LONG).show()
            return
        }
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful)
                {
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(applicationContext,"Login Successful",Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(applicationContext,"Login Failed or User not available",Toast.LENGTH_LONG).show()
                }
            }

    }


}