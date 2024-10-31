package com.uptick.sample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uptick.sdk.UptickManager
import com.uptick.sdk.model.Placement

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
        val adView = findViewById<FrameLayout>(R.id.adView)
        val adViewInline = findViewById<FrameLayout>(R.id.adView_inline)
        val proceedButton = findViewById<Button>(R.id.proceed)
        val placementContainer = findViewById<RadioGroup>(R.id.placement)
        val integrationIdText = findViewById<EditText>(R.id.integration_id)
        val firstName = findViewById<EditText>(R.id.first_name)
        val uptickManager = UptickManager()
        uptickManager.onError = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        proceedButton.setOnClickListener {
            uptickManager.initiateView(
                this,
                if (placementContainer.checkedRadioButtonId == R.id.order_confirmation) adView else adViewInline,
                integrationIdText.text.toString(),
                placement = when (placementContainer.checkedRadioButtonId) {
                    R.id.order_confirmation -> Placement.ORDER_CONFIRMATION
                    R.id.order_status -> Placement.ORDER_STATUS
                    else -> Placement.SURVEY
                },
                optionalParams = if (firstName.text.toString().isEmpty()
                        .not()
                ) mapOf("first_name" to firstName.text.toString()) else mapOf()
            )
        }
    }
}