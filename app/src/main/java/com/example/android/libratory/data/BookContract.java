package com.example.android.libratory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {
    public static final String LOG_TAG = BookContract.class.getSimpleName();

    //private constructor
    private BookContract() {
    }

    //Contruct a URI pointing to the books table by saving its various components as constants:
    //Content_authority helps identify the Content Provider
    public static final String CONTENT_AUTHORITY = "com.example.android.libratory";
    //Concatenate the CONTENT_AUTHORITY with the scheme for content uris
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    public static final class BookEntry implements BaseColumns {

        // Constants defining the books table Uri, MIME types, table name, and columns
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                "/" + PATH_BOOKS;

        public static final String TABLE_NAME = "books";

        //Columns..._ID is derived from the BaseColumns class and is a unique row identifier
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

        public static boolean isSupplierValid(int supplier) {
            if (supplier == SUPPLIER_ATYPICAL || supplier == SUPPLIER_ABES_BOOKS
                    || supplier == SUPPLIER_AMAZON || supplier == SUPPLIER_EBAY) {
                return true;
            }
            return false;
        }
    }
}
