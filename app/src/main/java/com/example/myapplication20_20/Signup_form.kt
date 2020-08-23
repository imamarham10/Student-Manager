package com.example.myapplication20_20

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_signup_form.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class Signup_form : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_form)
        supportActionBar?.setTitle("SignUp Form")
        mAuth = FirebaseAuth.getInstance();
        ivImagePerson.setOnClickListener(View.OnClickListener {
            checkPermission()
        })


    }
    val READIMAGE:Int=253
    fun checkPermission()
    {
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)
                return
            }

        }
        LoadImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when(requestCode)
        {
            READIMAGE ->
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    LoadImage()
                }
                else
                {
                    Toast.makeText(this,"Cannot acccess your image",Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
    var PICK_IMAGE_CODE = 253
    fun LoadImage()
    {
        var intent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_CODE && data!= null && resultCode == RESULT_OK)
        {
            val selectedImage = data.data
            val filePathCol = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!,filePathCol,null,null,null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathCol[0])
            val picturePath = cursor.getString( columnIndex)
            cursor.close()
            ivImagePerson.setImageBitmap(BitmapFactory.decodeFile(picturePath))

        }
    }




    fun buLoginEvent(view: View)
    {
        var Gender:String?=null
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val username = etUserName.text.toString()
        val name = etName.text.toString()
        if(male.isChecked())
        {
            Gender = "Male"
        }
        if(female.isChecked())
        {
            Gender = "Female"
        }
        if (Gender != null) {
            signupToFirebase(email, password,name,username,Gender)
        }
    }

    fun signupToFirebase(email:String,password:String,name:String,username:String,Gender:String)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(
                this,
                "Please Enter Email",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please Enter Password",Toast.LENGTH_LONG).show()
            return
        }
        if(password.length <8)
        {
            Toast.makeText(this,"Password too short",Toast.LENGTH_LONG).show()
            return
        }
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful)
                {
                    var currentuser = mAuth!!.currentUser
                    //save in database
                    var information = Student(name,username,email,Gender)
                    myRef.child("Users").child(currentuser!!.uid).setValue(information)
                    //save in storage
                    SaveToFirebase()

                    LoadMain()
                } else
                {
                    Toast.makeText(applicationContext,"Authentication Failed",Toast.LENGTH_LONG).show()

                }

                // ...
            }
    }
    fun SaveToFirebase()
    {
        var currentuser = mAuth!!.currentUser
        var email = currentuser!!.email.toString()
        var Storage = FirebaseStorage.getInstance()
        var df = SimpleDateFormat("ddMMyyHHmmss")
        var dataobj = Date()
        val storageRef = Storage.getReferenceFromUrl("gs://my-application-20-20.appspot.com")
        val imagePath = Split(email)+"."+df.format(dataobj)+".jpg"
        val imageRef = storageRef.child("Images/"+ imagePath)
        ivImagePerson.isDrawingCacheEnabled = true
        ivImagePerson.buildDrawingCache()

        val drawable = ivImagePerson.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"Failed to Upload",Toast.LENGTH_LONG).show()
        }.addOnSuccessListener{taskSnapshot ->
            var DownloadURL = taskSnapshot.storage.downloadUrl
            myRef.child("Users").child(currentuser.uid).child("email").setValue(currentuser.email)
            myRef.child("Users").child(currentuser.uid).child("ProfileImage").setValue(DownloadURL)
            Toast.makeText(applicationContext,"Upload Done",Toast.LENGTH_LONG).show()
        }
    }

    fun Split(email: String):String{
        val split = email.split("@")
        return split[0]
    }

    fun  LoadMain(){
        var currentUser =mAuth!!.currentUser

        if(currentUser!=null) {
            var intent = Intent(this, Login_form::class.java)
            startActivity(intent)
            finish()
        }
    }

}