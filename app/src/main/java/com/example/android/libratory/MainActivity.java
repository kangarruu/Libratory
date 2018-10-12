package com.example.android.libratory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.example.android.libratory.data.BookContract.BookEntry;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    BookCursorAdapter cursorAdapter;
    //Required param that identifies the CursorLoader. Can be any int.
    private static final int URL_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Locate the ListView and emptyView via id. set the empty view on the listView
        ListView bookListView = findViewById(R.id.book_list_view);
        View emptyView = findViewById(R.id.empty_view_parent);
        bookListView.setEmptyView(emptyView);

        //Set up the CursorAdapter to display items in the ListView
        cursorAdapter = new BookCursorAdapter(this, null, 0);
        bookListView.setAdapter(cursorAdapter);

        getSupportLoaderManager().initLoader(URL_LOADER, null, this);
    }

    private void insertDummyData() {
        //Create a ContentValues object, insert values into it and then insert the object into the db
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "This Dark Duet");
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, "V.E. Schwab");
        values.put(BookEntry.COLUMN_PRICE, 27.99);
        values.put(BookEntry.COLUMN_QUANTITY, 23);
        values.put(BookEntry.COLUMN_SUPPLIER, 0);

        Uri newBookUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
        if (newBookUri == null) {
            Toast.makeText(MainActivity.this, R.string.editor_save_error_toast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, R.string.editor_book_saved_toast, Toast.LENGTH_SHORT).show();
        }

    }

    //Inflate the menu in the res/menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_save:
                insertDummyData();
                return true;
            case R.id.menu_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.menu_delete_all:
                clearData();
                return true;
            case R.id.menu_enter_new_book:
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //helper method for clearing the database
    private void clearData() {
        if (BookEntry.CONTENT_URI != null){
            int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
            Log.v(LOG_TAG, rowsDeleted + " books deleted from the database");
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        //Define a projection to pass in desired columns to the CursorLoader:
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY
        };

        //If param matches LoaderID, create a new CursorLoader that will create a cursor off the main thread
        switch (loaderId) {
            case URL_LOADER:
                return new CursorLoader(this, BookEntry.CONTENT_URI, projection, null, null, null);
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        //swap out the current cursor
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //delete the old cursor reference to prevent memory leaks
        cursorAdapter.swapCursor(null);
    }


}
