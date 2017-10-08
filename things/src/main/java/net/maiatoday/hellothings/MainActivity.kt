package net.maiatoday.hellothings

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : Activity() {
    companion object {
        val TAG = "MainActivity"

        private val DEVICE_RPI = "rpi3"
        val GREEN_LED_PIN = if (Build.DEVICE == DEVICE_RPI)
            "BCM26"
        else
            "GPIO_37"
        val RED_LED_PIN = if (Build.DEVICE == DEVICE_RPI)
            "BCM16"
        else
            "GPIO_39"
        val RED_LED_DB = "ledRedOn"
        val GREEN_LED_DB = "ledGreenOn"

    }

    lateinit var busGreen: Gpio
    lateinit var busRed: Gpio
    lateinit var redLedRef: DatabaseReference
    lateinit var greenLedRef: DatabaseReference
    var redLedIsOn: Boolean = false
    var greenLedIsOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLeds()
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
                busRed.setValue(true)
                on?.let {
                    redLedIsOn = it
                    if (it) {
                        redLed.setImageResource(R.drawable.led_red)
                        busRed.setValue(true)
                    } else {
                        redLed.setImageResource(R.drawable.led_off)
                        busRed.setValue(false)
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
                busGreen.setValue(true)
                on?.let {
                    greenLedIsOn = it
                    if (it) {
                        greenLed.setImageResource(R.drawable.led_green)
                        busGreen.setValue(true)
                    } else {
                        greenLed.setImageResource(R.drawable.led_off)
                        busGreen.setValue(false)
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
        redLed.setOnClickListener { redLedRef.setValue(!redLedIsOn) }
        greenLed.setOnClickListener { greenLedRef.setValue(!greenLedIsOn) }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            busRed.close()
            busGreen.close()
        } catch (e: IOException) {
            Log.e("TUT", GREEN_LED_PIN + " bus cannot be closed, you may experience errors on next launch.", e)
        }

    }
    private fun setupLeds() {
        val service = PeripheralManagerService()
        try {
            busGreen = service.openGpio(GREEN_LED_PIN)
            busRed = service.openGpio(RED_LED_PIN)
        } catch (e: IOException) {
            throw IllegalStateException(GREEN_LED_PIN + " busGreen or busRed cannot be opened.", e)
        }

        try {
            busRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            busRed.setActiveType(Gpio.ACTIVE_HIGH)
            busGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            busGreen.setActiveType(Gpio.ACTIVE_HIGH)
        } catch (e: IOException) {
            throw IllegalStateException(GREEN_LED_PIN + " busGreen or busRed cannot be configured.", e)
        }
        Log.d(TAG, "Leds configured")

        val test = Build.DEVICE+" Hrmph Available GPIO: " + service.gpioList
        Log.d(TAG, test)
        textView.text = test
    }


}
