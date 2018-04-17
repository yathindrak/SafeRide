package com.saferide.saferide

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class StartActivity : AppCompatActivity() {

    private lateinit var mRegBtn:Button
    private lateinit var mLoginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        mRegBtn = findViewById(R.id.start_reg_btn)
        mLoginBtn = findViewById(R.id.start_login_btn)

        mRegBtn.setOnClickListener {

            // Go to register activity
            val reg_intent = Intent(this@StartActivity, RegisterActivity::class.java)
            startActivity(reg_intent)
        }

        mLoginBtn.setOnClickListener {

            // Go to login activity
            val login_intent = Intent(this@StartActivity, LoginActivity::class.java)
            startActivity(login_intent)

        }

    }
}
