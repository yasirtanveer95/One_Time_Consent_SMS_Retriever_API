package com.example.otptesting

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_start.setOnClickListener {
            startlistener()
        }
        val smsVerificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                    val extras = intent.extras
                    val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                    when (smsRetrieverStatus.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            // Get consent intent
                            val consentIntent =
                                extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                            try {
                                // Start activity to show consent dialog to user, activity must be started in
                                // 5 minutes, otherwise you'll receive another TIMEOUT intent
                                otpResult.launch(consentIntent)
                            } catch (e: ActivityNotFoundException) {
                                // Handle the exception ...
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            // Time out occurred, handle the error.
                        }
                    }
                }
            }
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsVerificationReceiver, intentFilter)
    }

    private fun startlistener() {
// Get an instance of SmsRetrieverClient, used to start listening for a matching
// SMS message.
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
// SMS message.
        val client = SmsRetriever.getClient(this /* context */)

// Starts SmsRetriever, which waits for ONE matching SMS message until timeout
// (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
// action SmsRetriever#SMS_RETRIEVED_ACTION.

// Starts SmsRetriever, which waits for ONE matching SMS message until timeout
// (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
// action SmsRetriever#SMS_RETRIEVED_ACTION.
        val task: Task<Void> = client.startSmsUserConsent(null)

// Listen for success/failure of the start Task. If in a background thread, this
// can be made blocking using Tasks.await(task, [timeout]);

// Listen for success/failure of the start Task. If in a background thread, this
// can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(OnSuccessListener<Void?> {
            Toast.makeText(this, "Task Started", Toast.LENGTH_SHORT).show()
            // Successfully started retriever, expect broadcast intent
            // ...
        })

        task.addOnFailureListener(OnFailureListener {
            Toast.makeText(this, "Task Failed", Toast.LENGTH_SHORT).show()
            // Failed to start retriever, inspect Exception for more details
            // ...
        })
    }

    var otpResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val message = it.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            et_otp.text = message.toString()
            // Extract one-time code from the message and complete verification
            // `message` contains the entire text of the SMS message, so you will need
            // to parse the string.
            //val oneTimeCode = parseOneTimeCode(message) // define this function
        }
    }

}