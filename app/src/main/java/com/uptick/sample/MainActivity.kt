package com.uptick.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uptick.sdk.UptickManager

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val uptickManager = UptickManager()
        uptickManager.onError = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        uptickManager.initiateView(
            this,
            findViewById(R.id.adView),
            "0bf6f068-6bf5-49f1-a6bc-822eee7d4db3",
            optionalParams = mapOf("first_name" to "John")
        )
    }
}