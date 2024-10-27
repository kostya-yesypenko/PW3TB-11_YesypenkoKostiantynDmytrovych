package com.example.solarprofitcalc

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // View references
        val avgPowerInput = findViewById<EditText>(R.id.pcInput)
        val deviationInput = findViewById<EditText>(R.id.sigmaInput)
        val rateInput = findViewById<EditText>(R.id.bInput)
        val computeButton = findViewById<Button>(R.id.calculateButton)
        val outputText = findViewById<TextView>(R.id.resultText)

        // Compute button click listener
        computeButton.setOnClickListener {
            val avgPower = avgPowerInput.text.toString().toDoubleOrNull() ?: 0.0
            val deviation = deviationInput.text.toString().toDoubleOrNull() ?: 0.0
            val rate = rateInput.text.toString().toDoubleOrNull() ?: 0.0

            // Perform calculations
            val energyShare = computeEnergyDistribution(
                function = { power -> normalDistributionFormula(power, avgPower, deviation) },
                centralPower = avgPower,
                numSteps = 10000
            )

            val earnings = avgPower * 24 * energyShare * rate
            val penalties = avgPower * 24 * (1 - energyShare) * rate
            val netProfit = earnings - penalties

            // Display results
            outputText.text = """
                Revenue: ${String.format("%.1f", earnings)} thousand UAH
                Penalty: ${String.format("%.1f", penalties)} thousand UAH
                Net Profit${if (netProfit < 0) " (Loss)" else ""}: ${String.format("%.1f", netProfit)} thousand UAH 
            """.trimIndent()
        }
    }

    // Normal distribution of power
    private fun normalDistributionFormula(
        power: Double,
        centralPower: Double,
        stDev: Double
    ): Double {
        return (1 / (stDev * sqrt(2 * PI))) * exp(
            -((power - centralPower).pow(2)) / (2 * stDev.pow(2))
        )
    }

    // Approximate integral calculation using trapezoidal method
    private fun computeEnergyDistribution(
        function: (Double) -> Double,
        centralPower: Double,
        numSteps: Int,
        deviationRange: Double = 0.05
    ): Double {
        val minLimit = centralPower * (1 - deviationRange)
        val maxLimit = centralPower * (1 + deviationRange)
        val stepSize = (maxLimit - minLimit) / numSteps
        var accumulatedResult = 0.0

        for (i in 0 until numSteps) {
            val startPoint = minLimit + i * stepSize
            val endPoint = startPoint + stepSize
            accumulatedResult += 0.5 * (function(startPoint) + function(endPoint)) * stepSize
        }

        return accumulatedResult
    }
}
