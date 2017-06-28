package net.maiatoday.hellothings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
        val RED_LED_DB = "ledRedOn"
        val GREEN_LED_DB = "ledGreenOn"
    }
    lateinit var redLedRef: DatabaseReference
    lateinit var greenLedRef: DatabaseReference
    var redLedIsOn: Boolean = false
    var greenLedIsOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupData()
        setupTouch()
    }


    private fun setupData() {
        val database = FirebaseDatabase.getInstance()
        redLedRef = database.getReference(RED_LED_DB)
        greenLedRef = database.getReference(GREEN_LED_DB)
        val redEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val on = dataSnapshot.getValue(Boolean::class.java)
                Log.d(TAG, "dataChange Red")
                on?.let {
                    redLedIsOn = it
                    if (it) {
                        redLed.setImageResource(R.drawable.led_red)
                    } else {
                        redLed.setImageResource(R.drawable.led_off)
                    }
                    Log.d(TAG, "Red is: " + it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read red.", error.toException())
            }
        }
        redLedRef.addValueEventListener(redEventListener)
        val greenEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val on = dataSnapshot.getValue(Boolean::class.java)
                Log.d(TAG, "dataChange Green")
                on?.let {
                    greenLedIsOn = it
                    if (it) {
                        greenLed.setImageResource(R.drawable.led_green)
                    } else {
                        greenLed.setImageResource(R.drawable.led_off)
                    }
                    Log.d(TAG, "Green is: " + it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read green.", error.toException())
            }
        }
        greenLedRef.addValueEventListener(greenEventListener)

        Log.d(TAG, "db setup complete")
    }


    private fun setupTouch() {
        redLed.setOnClickListener {  redLedRef.setValue(!redLedIsOn) }
        greenLed.setOnClickListener {  greenLedRef.setValue(!greenLedIsOn) }
    }


}
