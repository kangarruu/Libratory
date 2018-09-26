package com.example.android.libratory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.libratory.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = BookDbHelper.class.getSimpleName();

    //Database name constant
    public static final String DATABASE_NAME = "Inventory.db";
    //Database version starting at 1, as per convention
    public static final int DATABASE_VERSION = 1;

    //BookDbHelper constructor
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //onCreate method allows the BookDbHelper to create a new db on the User's device
    @Override
    public void onCreate(SQLiteDatabase db) {
    //String for SQL statement to create the Books table. Constants stored in BookContract.java
        String SQL_CREATE_BOOK_TABLE =
                "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL, "
                + BookEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_SUPPLIER + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_SUPPLIER_PHONE + " TEXT);";

    // Execute the actual SQL command to create the table using the above String
        db.execSQL(SQL_CREATE_BOOK_TABLE);
    }

    //Once the initial table is created, the following is called if any upgrades or changes are made to version 1
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // String for SQL statement that will drop the table and recreate it
        String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME;
    // Execute the drop table command and then recreate the db
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
