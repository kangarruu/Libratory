package com.example.android.libratory;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.example.android.libratory.data.BookContract.BookEntry;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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

        //Set an setOnItemClickListener to open the DetailActivity
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create a Uri for the clicked item to pass in the intent to the DetailActivity
                final Uri clickedRowUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.setData(clickedRowUri);
                startActivity(intent);
            }
        });

        //Set an setOnItemLongClickListener to display an AlertDialog asking the user if they want to edit
        bookListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditChangesDialog(view, id);
                return true;
            }


        });

        //Initialize the LoaderManager
        getSupportLoaderManager().initLoader(URL_LOADER, null, this);
    }


    private boolean showEditChangesDialog(View view, long uriId) {
        //save the _id for the clicked row as a local variable
        final long mUriId = uriId;

        //Create a Uri for the clicked item to pass in the intent to the Editor Activity & deleteBook()
        final Uri currentUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, mUriId);

        //Create an AlertDialog.Builder and set the alert message and click listeners for the 3 buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.edit_book_dialog_msg);

        //Edit button opens EditActivity and passes the currentUri
        builder.setNeutralButton(R.string.dialog_edit_book, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(currentUri);
                startActivity(intent);
            }
        });

        //Delete button deletes the current book
        builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook(currentUri);
            }
        });

        //Cancel button closes the dialog
        builder.setNegativeButton(R.string.dialog_cancel_edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }

    //Delete a Book record
    public void deleteBook(Uri uri) {
        int rowsDeleted = getContentResolver().delete(uri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(MainActivity.this, R.string.editor_delete_error_toast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, R.string.editor_book_delete_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void insertDummyData() {
        //Create a ContentValues object, insert values into it and then insert the object into the db
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_data_title));
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, getString(R.string.dummy_data_author));
        values.put(BookEntry.COLUMN_PRICE, 14.99);
        values.put(BookEntry.COLUMN_QUANTITY, 25);
        values.put(BookEntry.COLUMN_SUPPLIER, BookEntry.SUPPLIER_AMAZON);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, getString(R.string.amazon_phone));

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
        if (BookEntry.CONTENT_URI != null) {
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
