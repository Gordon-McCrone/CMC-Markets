package com.cmcmarkets

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class Home : Activity(), NetConProtocol {
    var TAG = "*5* Home"

    var buyAsDouble: Double = 0.0
    var sellAsDouble: Double = 0.0
    var editTextChanged = false // Avoid infinite loop of each updating the other

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        Log.d(TAG, "The onCreate() event")

        val unitsEditText = findViewById(R.id.unitsEditText) as EditText
        val amountsEditText = findViewById(R.id.amountsEditText) as EditText

        unitsEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (!editTextChanged) {
                    editTextChanged = true

                    if (s.count() > 0) {
                        val updatedNumber = s.toString().toDouble() * buyAsDouble

                        amountsEditText.setText(updatedNumber.toString())
                    } else {
                        amountsEditText.setText("")
                    }
                }
                else {
                    editTextChanged = false
                }
            }
        })

        amountsEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (!editTextChanged) {
                    editTextChanged = true
                    if (s.count() > 0) {
                        val updatedNumber = buyAsDouble / s.toString().toDouble()

                        unitsEditText.setText(updatedNumber.toString())
                    } else {
                        unitsEditText.setText("")
                    }
                }
                else {
                    editTextChanged = false
                }
            }
        })

        unitsEditText.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                unitsEditText.setBackgroundResource(R.drawable.edittextselected)
                amountsEditText.setBackgroundDrawable(null)
            }
        })

        amountsEditText.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                amountsEditText.setBackgroundResource(R.drawable.edittextselected)
                unitsEditText.setBackgroundDrawable(null)
            }
        })

        val confirmButton = findViewById(R.id.confirmButton) as Button

        confirmButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(applicationContext, "confirmButton onClick", Toast.LENGTH_LONG).show()
            }
        })

        val myTimer = Timer()
        myTimer.schedule(object : TimerTask() {
            override fun run() {
                updateData()
            }
        }, 0, 15000)
    }

    override fun Success(dict: Map<*,*>) {
        Log.d(TAG, "The Success() event")

        val GBPDict = dict["GBP"] as Map<*, *>
        val sell = GBPDict["sell"]
        val buy = GBPDict["buy"]

        Log.d(TAG, "Success() GBPDict ${GBPDict}")
        Log.d(TAG, "Success() sell ${sell}")
        Log.d(TAG, "Success() buy ${buy}")

        // Split up number to become Left Hand Side (LHS) and Right Hand Side (RHS) of decimal point
        // This will be used to populate sellDataDollars and sellDataCents respectively
        val sellAsString = sell.toString()
        val sellAsArray = sellAsString.split(".").toTypedArray()
        val sellDollars = sellAsArray[0]
        val sellCents = sellAsArray[1]
        Log.d(TAG, "Success() sell dollars ${sellDollars}")
        Log.d(TAG, "Success() sell cents ${sellCents}")

        val buyAsString = buy.toString()
        val buyAsArray = buyAsString.split(".").toTypedArray()
        val buyDollars = buyAsArray[0]
        val buyCents = buyAsArray[1]
        Log.d(TAG, "Success() buy dollars ${buyDollars}")
        Log.d(TAG, "Success() buy cents ${buyCents}")

        // Was having issue with sell & buy being reported as Double and BigDecimal, so arithmetic was not allowed for unknown reason
        buyAsDouble = buyAsString.toDouble()
        sellAsDouble = sellAsString.toDouble()
        val spread = buyAsDouble - sellAsDouble

        this.runOnUiThread(Runnable {
            Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()

            val sellDataDollars = findViewById(R.id.sellDataDollars) as TextView
            sellDataDollars.text = sellDollars

            val sellDataCents = findViewById(R.id.sellDataCents) as TextView
            sellDataCents.text = sellCents

            val buyDataDollars = findViewById(R.id.buyDataDollars) as TextView
            buyDataDollars.text = buyDollars

            val buyDataCents = findViewById(R.id.buyDataCents) as TextView
            buyDataCents.text = buyCents

            val spreadData = findViewById(R.id.spreadData) as TextView

            spreadData.text = spread.toString()

            val dateTimeData = findViewById(R.id.dateTimeData) as TextView
            val sdf = SimpleDateFormat("HH:mm:ss")
            val currentDate = sdf.format(Date())
            dateTimeData.text = currentDate
        })
    }

    override fun Fail(dict: Map<*,*>) {
        Log.d(TAG, "The Fail() event")

        val errorType = dict["error"]
        val errorCode = dict["code"]

        this.runOnUiThread(Runnable {
            Toast.makeText(applicationContext, "Fail " + errorType + " " + errorCode, Toast.LENGTH_LONG).show()
        })
    }

    fun updateData() {
        val netCon = NetCon()
        netCon.Post(this@Home, "ticker")
        netCon.execute()
    }

    /** Called when the activity is about to become visible.  */
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "The onStart() event")
    }

    /** Called when the activity has become visible.  */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "The onResume() event")
    }

    /** Called when another activity is taking focus.  */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "The onPause() event")
    }

    /** Called when the activity is no longer visible.  */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "The onStop() event")
    }

    /** Called just before the activity is destroyed.  */
    public override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "The onDestroy() event")
    }
}