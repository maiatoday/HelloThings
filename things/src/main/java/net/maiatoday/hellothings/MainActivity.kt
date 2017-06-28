package net.maiatoday.hellothings

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Looper
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS




/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity() {
    companion object {
        val TAG = "MainActivity"
        val GREEN_LED_PIN = "BCM26"
        val RED_LED_PIN = "BCM16"
    }

    lateinit var busGreen: Gpio
    lateinit var busRed: Gpio
    lateinit var ledToggleHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val service = PeripheralManagerService()
        val test = "Available GPIO: " + service.gpioList
        Log.d(TAG, test)
        textView.text = test

        try {
            busGreen = service.openGpio(GREEN_LED_PIN)
            busRed = service.openGpio(RED_LED_PIN)
        } catch (e: IOException) {
            throw IllegalStateException(GREEN_LED_PIN + " busGreen or busRed cannot be opened.", e)
        }


        try {
            busRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            busRed.setActiveType(Gpio.ACTIVE_HIGH)
            busGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH)
            busGreen.setActiveType(Gpio.ACTIVE_HIGH)
        } catch (e: IOException) {
            throw IllegalStateException(GREEN_LED_PIN + " busGreen or busRed cannot be configured.", e)
        }


        ledToggleHandler = Handler(Looper.getMainLooper())
    }

    override fun onStart() {
        super.onStart()
        ledToggleHandler.post(toggleLed)
    }

    private val toggleLed = object : Runnable {
        override fun run() {
            val isOnRed: Boolean
            val isOnGreen: Boolean
            try {
                isOnRed = busRed.getValue()
                isOnGreen = busGreen.getValue()
            } catch (e: IOException) {
                throw IllegalStateException(GREEN_LED_PIN + " cannot be read.", e)
            }

            try {
                if (isOnRed) {
                    busRed.setValue(false)
                } else {
                    busRed.setValue(true)
                }

                if (isOnGreen) {
                    busGreen.setValue(false)
                } else {
                    busGreen.setValue(true)
                }
            } catch (e: IOException) {
                throw IllegalStateException(GREEN_LED_PIN + " cannot be written.", e)
            }

            ledToggleHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
        }
    }

    override fun onStop() {
        ledToggleHandler.removeCallbacks(toggleLed)
        super.onStop()
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
}
