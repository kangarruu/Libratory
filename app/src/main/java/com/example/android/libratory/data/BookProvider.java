package com.example.android.libratory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.libratory.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    //Constants for path codes for URIs fed into the UriMatcher
    private static final int BOOKS = 1;
    private static final int BOOK_ID = 2;

    //Create a UriMatcher object and add paths to book table and individual rows
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    //Database helper object
    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        //Initialize the dbHelper object to gain access to the database
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        //Cursor variable that will be returned with the result of the query
        Cursor cursor;

        //Get readable database to query
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //See if the UriMatcher can match the input Uri to a specific path accepted by the BookProvider
        int match = sUriMatcher.match(uri);
        switch (match) {
            //If the Uri is matched to the whole table, query the database directly with the given inputs
            //save the query results in the cursor variable
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            //When querying for a specific row, parse the id at the end of the Uri and set as the selectionArgs
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //Set the cursor to the result of the SQL query
                cursor = database.query(BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("UriMatcher error querying URI: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        //See if the UriMatcher can match the input Uri to the BOOK path
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            //Does not make sense to insert a book with any other Uri, throw exception:
            default:
                throw new IllegalArgumentException("Could not insert book with Uri: " + uri);
        }

    }

    //Helper method to insert a new book into the db. Return a Uri pointing to the new db row
    private Uri insertBook(Uri uri, ContentValues values) {
        //Extract the values details and sanity-check the data against db requirements
        String NewTitle = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
        if (NewTitle == null) {
            throw new IllegalArgumentException("Book requires a title");
        }
        String newAuthor = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
        if (newAuthor == null) {
            throw new IllegalArgumentException("Book requires an author");
        }
        Double newPrice = values.getAsDouble(BookEntry.COLUMN_PRICE);
        if (newPrice == null || newPrice < 0) {
            throw new IllegalArgumentException("Book requires valid price");
        }
        Integer newQuantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (newQuantity != null && newQuantity < 0) {
            throw new IllegalArgumentException("Book requires valid quantity");
        }
        Integer supplier = values.getAsInteger(BookEntry.COLUMN_SUPPLIER);
        if (supplier == null || !BookEntry.isSupplierValid(supplier)) {
            throw new IllegalArgumentException("Book requires valid supplier");
        }

        //Get writeable database to insert into
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long newBookId = database.insert(BookEntry.TABLE_NAME, null, values);

        //If insertion failed and database returned a -1, log the error and return null
        if (newBookId == -1) {
            Log.e(LOG_TAG, "Error inserting book into database for " + uri);
            return null;
        }
        //Append the new row id returned by the database to the end of the contentUri and return the new Uri
        return ContentUris.withAppendedId(uri, newBookId);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        //See if the UriMatcher can match the input Uri to a specific path accepted by the BookProvider
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //If the Uri is matched to the whole table, simply pass inputs to updateBook() method
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            //If the Uri is matched to specific row, parse the id at the end of the Uri and set as the selectionArgs
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unable to update URI: " + uri);
        }
    }

    //Helper method to update the db with content values params passed in update(). Return the number of rows updated.
    private int updateBook(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        //Extract the ContentValues details and sanity-check them against db requirements
        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String NewTitle = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (NewTitle == null) {
                throw new IllegalArgumentException("Book requires a title");
            }
        }

        if (values.containsKey((BookEntry.COLUMN_BOOK_AUTHOR))) {
            String newAuthor = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
            if (newAuthor == null) {
                throw new IllegalArgumentException("Book requires an author");
            }
        }

        if (values.containsKey((BookEntry.COLUMN_PRICE))) {
            Double newPrice = values.getAsDouble(BookEntry.COLUMN_PRICE);
            if (newPrice == null || newPrice < 0) {
                throw new IllegalArgumentException("Book requires valid price");
            }
        }

        if (values.containsKey((BookEntry.COLUMN_QUANTITY))) {
            Integer newQuantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (newQuantity != null && newQuantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }

        if (values.containsKey((BookEntry.COLUMN_SUPPLIER))) {
            Integer supplier = values.getAsInteger(BookEntry.COLUMN_SUPPLIER);
            if (supplier == null || !BookEntry.isSupplierValid(supplier)) {
                throw new IllegalArgumentException("Book requires valid supplier");
            }
        }

        //if there are no valued to update, don't run the query
        if (values.size() == 0) {
            return 0;
        }

        //Otherwise, get a writeable database, run the update and return the number of rows updated.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        return database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        //See if the UriMatcher can match the input Uri to a specific path accepted by the BookProvider
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            //If the Uri is matched to the whole table, delete the rows that qualify for the given inputs
            case BOOKS:
                return database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
            //If the Uri is matched to specific row, parse the id at the end of the Uri and set as the selectionArgs
            //delete the rows that qualify for the given inputs
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unable to delete URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //If the Uri is matched to the whole table, return the list MIME type
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            //If the Uri is matched to specific row, return the item MIME type
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("URI: " + uri + "Does not match given MIME types");
        }
    }

}
