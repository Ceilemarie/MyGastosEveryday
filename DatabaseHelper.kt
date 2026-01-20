package com.example.impr

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ExpenseDatabase.db"
        const val TABLE_NAME = "expenses"
        const val ID = "id" // unique id for each expenses

        ///column for each
        const val CATEGORY = "category"
        const val AMOUNT = "amount"
        const val DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CATEGORY + " TEXT, "
                + AMOUNT + " REAL, "
                + DATE + " TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertExpense(category: String, amount: Double, date: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(CATEGORY, category)
        contentValues.put(AMOUNT, amount)
        contentValues.put(DATE, date)
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun getAllExpenses(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun deleteAllExpenses(): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, null, null)
        return result > 0
    }
}
