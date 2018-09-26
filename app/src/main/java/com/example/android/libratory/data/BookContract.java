package com.example.android.libratory.data;

import android.provider.BaseColumns;

public final class BookContract {
    public static final String LOG_TAG = BookContract.class.getSimpleName();

    //private constructor
    private BookContract(){}

    public static final class BookEntry implements BaseColumns{

        /* Constants defining the books table name and columns within the inventory db
        * _ID is derived from the BaseColumns class and is a unique row identifier*/

        public static final String TABLE_NAME = "books";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "title";
        public static final String COLUMN_BOOK_AUTHOR = "author";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_SUPPLIER_PHONE = "phone";

        /* Key for possible supplier options saved as INTEGERS */
        public static final int SUPPLIER_ATYPICAL = 0;
        public static final int SUPPLIER_EBAY = 1;
        public static final int SUPPLIER_AMAZON = 2;
        public static final int SUPPLIER_ABES_BOOKS = 3;

    }
}
