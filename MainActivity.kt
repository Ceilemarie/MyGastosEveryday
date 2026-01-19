package com.example.impr

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var inputAmount: EditText
    private lateinit var chooseExpense: Spinner
    private lateinit var listView: ListView
    private lateinit var btnDatenTime: Button
    private lateinit var btnSend: Button
    private lateinit var Totalamount: TextView

    private lateinit var btnclear: Button
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var expensesAdapter: ArrayAdapter<String>
    private var expenseList = ArrayList<String>() //for listview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inputAmount = findViewById(R.id.inputAmount)
        chooseExpense = findViewById(R.id.chooseExpense)
        listView = findViewById(R.id.listView)
        btnDatenTime = findViewById(R.id.btnDatenTime)
        btnSend = findViewById(R.id.btnSend)
        btnclear = findViewById(R.id.btnclear)
        Totalamount = findViewById(R.id.Totalamount)

        dbHelper = DatabaseHelper(this)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.expenses,
            R.layout.custom_text_item
        )
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        chooseExpense.adapter = spinnerAdapter

        btnDatenTime.setOnClickListener {
            showDatePicker()
        }

        btnSend.setOnClickListener {
            addExpense()
        }

        btnclear.setOnClickListener {
            showClearConfirmationDialog()
        }

        loadExpenses()
    }

    private fun showClearConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to delete all expenses? This action cannot be undone.")
            .setPositiveButton("Yes, Clear All") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("No", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun clearAllData() {
        if (dbHelper.deleteAllExpenses()) {
            Toast.makeText(this, "All expenses cleared", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No expenses to clear", Toast.LENGTH_SHORT).show()
        }
        loadExpenses() // Refresh the list
    }

    private fun addExpense() {
        val amount = inputAmount.text.toString().toDoubleOrNull()
        val category = chooseExpense.selectedItem.toString()
        val date = btnDatenTime.text.toString()

        if (amount == null) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (date == "Date") {
            Toast.makeText(this, "Please select a date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.insertExpense(category, amount, date)
        if (success) {
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
            inputAmount.text.clear()
            btnDatenTime.text = "Date"
            loadExpenses()
        } else {
            Toast.makeText(this, "Error adding expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExpenses() {
        val cursor = dbHelper.getAllExpenses()
        expenseList.clear()
        var totalAmount = 0.0

        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE))
                expenseList.add("$category: $amount - $date")
                totalAmount += amount
            } while (cursor.moveToNext())
        }
        cursor.close()

        expensesAdapter = ArrayAdapter(this, R.layout.custom_text_item, expenseList)
        listView.adapter = expensesAdapter
        Totalamount.text = String.format("%.2f", totalAmount)
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                showTimePicker()
            },
            year, month, day
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                updateLabel()
            },
            hour, minute, DateFormat.is24HourFormat(this)
        )
        timePicker.show()
    }

    private fun updateLabel() {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val formattedDate = formatter.format(calendar.time)
        btnDatenTime.text = formattedDate
    }
}
