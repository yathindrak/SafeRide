package com.saferide.saferide

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

class LoginActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var mLoginEmail: TextInputLayout
    private lateinit var mLoginPassword: TextInputLayout

    private lateinit var mLogin_btn: Button

    private lateinit var mLoginProgress: ProgressBar

    private lateinit var mAuth: FirebaseAuth

    private lateinit var mUserDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mLoginProgress = findViewById(R.id.progressBar)
        /*mLoginProgress.visibility = View.VISIBLE
        mLoginProgress.visibility = View.INVISIBLE*/

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        //Action Bar
        mToolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Login")

        //Database
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("Users")


        mLoginEmail = findViewById(R.id.login_email)
        mLoginPassword = findViewById(R.id.login_password)
        mLogin_btn = findViewById(R.id.login_btn)


        //Calls when user clicks on login button
        mLogin_btn.setOnClickListener {
            // Get user inputs
            val email = mLoginEmail.editText!!.text.toString()
            val password = mLoginPassword.editText!!.text.toString()

            if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                // Running progress
                mLoginProgress.visibility = View.VISIBLE

                //Pass user name and password
                loginUser(email, password)

            }
        }
    }

    //Log the user in
    private fun loginUser(email: String, password: String) {

        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                // Stop running progress
                mLoginProgress.visibility = View.INVISIBLE

                //Go to the main activity
                val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(mainIntent)
                finish()

                //Getting token for firebase cloud functions
                //get user id
                val current_user_id = mAuth!!.currentUser!!.uid
                //Get the token id
                val deviceToken = FirebaseInstanceId.getInstance().token
                //set the device token
                mUserDatabase?.child(current_user_id)!!.child("device_token").setValue(deviceToken).addOnSuccessListener {
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(mainIntent)
                    finish()
                }

            } else {

                // hide the progress
                mLoginProgress.visibility = View.INVISIBLE

                val task_result = task.exception!!.message.toString()
                Toast.makeText(this@LoginActivity, "Error : $task_result", Toast.LENGTH_LONG).show()

            }
        }
    }
}
