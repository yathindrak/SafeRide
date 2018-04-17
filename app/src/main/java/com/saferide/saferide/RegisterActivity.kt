package com.saferide.saferide

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {

    private lateinit var mDisplayName: TextInputLayout
    private lateinit var mEmail: TextInputLayout
    private lateinit var mPassword: TextInputLayout
    private lateinit var mCreateBtn: Button

    //Firebase Auth
    private lateinit var mAuth: FirebaseAuth

    private var mToolbar: Toolbar? = null

    //ProgressBar
    private lateinit var mRegisterProgress: ProgressBar

    //Firebase database
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mRegisterProgress = findViewById(R.id.progressBar)

        //Toolbar Set
        mToolbar = findViewById(R.id.register_toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar!!.setTitle("Create Account")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Firebase auth
        mAuth = FirebaseAuth.getInstance()

        // Android Fields
        mDisplayName = findViewById(R.id.register_display_name) as TextInputLayout
        mEmail = findViewById(R.id.register_email) as TextInputLayout
        mPassword = findViewById(R.id.reg_password) as TextInputLayout
        mCreateBtn = findViewById(R.id.reg_create_btn) as Button


        mCreateBtn.setOnClickListener {
            // Get inputs from the user
            val display_name = mDisplayName.editText!!.text.toString()
            val email = mEmail.editText!!.text.toString()
            val password = mPassword.editText!!.text.toString()

            if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                // Running progress
                mRegisterProgress.visibility = View.VISIBLE
                mRegisterProgress.bringToFront()

                //pass properties to register the user
                register_user(display_name, email, password)

            }
        }
    }

    //Register the new user
    private fun register_user(display_name: String, email: String, password: String) {

        //Create new  user using email and password
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            // Task is successful means user has registered successfully
            if (task.isSuccessful) {
                val current_user = FirebaseAuth.getInstance().currentUser
                val uid = current_user!!.uid

                mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(uid)

                val device_token = FirebaseInstanceId.getInstance().token

                val userMap = HashMap<String, String>()
                userMap["name"] = display_name
                userMap["status"] = "Hi there I'm using YK Chat App."
                userMap["image"] = "default"
                userMap["thumb_image"] = "default"
                userMap["device_token"] = device_token.toString()

                val locationMap = HashMap<String, String>()
                locationMap["0"] = "6.9420421"
                locationMap["1"] = "79.880991"

                mDatabase.setValue(userMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        mDatabase?.child("l").setValue(locationMap)

                        //Dismiss the progress
                        mRegisterProgress.visibility = View.INVISIBLE

                        // Go to the main intent
                        val mainIntent = Intent(this@RegisterActivity, MainActivity::class.java)
                        //Add new task and clear previous tasks: unless when we press back it will still go to the start page
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(mainIntent)
                        finish()
                    }
                }
            } else {
                //Dismiss the progress
                mRegisterProgress.visibility = View.INVISIBLE

                Toast.makeText(this@RegisterActivity, "Cannot Sign in. Please check the form and try again.", Toast.LENGTH_LONG).show()
            }
        }

    }


}
