package com.example.android.libratory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.libratory.data.BookContract.BookEntry;


public class EditorActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    //Required param that identifies the CursorLoader. Can be any int.
    private static final int URL_LOADER = 0;

    private Uri currentUri;

    //Declare the views containing user input
    private EditText mBookTitle;
    private EditText mBookAuthor;
    private EditText mPrice;
    private EditText mQuantity;
    private Spinner mSupplier;

    //Variables for saving user input
    private String titleString;
    private String authorString;
    private String priceString;
    private String quantityString;

    private String supplierPhone;

    double priceInt = 0;
    int quantityInt = 0;

    //Declare the supplier selection to be assigned to constants stored in BookContract.java
    private int mSelectedSupplier;

    //variable for determining if changes have been made to any views in the activity_editor.xml
    //if view was clicked, onTouch will be triggered and mBookEntryChanged to true.
    private boolean mBookEntryChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBookEntryChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Get the URI from the MainActivity via passed intent
        Intent intent = getIntent();
        currentUri = intent.getData();
        //If there is an existing Uri, set the toolbar title to "Edit Book"
        if (currentUri != null) {
            setTitle(R.string.edit_toolbar);
            getSupportLoaderManager().initLoader(URL_LOADER, null, this);
            //Otherwise, set the toolbar title to "Add book"
        } else {
            setTitle(R.string.add_toolbar);
            invalidateOptionsMenu();
        }

        //locate the Views containing UserInput via findViewById
        mBookTitle = (EditText) findViewById(R.id.title_edit_view);
        mBookAuthor = (EditText) findViewById(R.id.author_edit_view);
        mPrice = (EditText) findViewById(R.id.price_edit_view);
        mQuantity = (EditText) findViewById(R.id.quantity_edit_view);
        mSupplier = (Spinner) findViewById(R.id.supplier_spinner);

        //Set onTouchListener on each view to see if user clicked/began editing
        mBookTitle.setOnTouchListener(mTouchListener);
        mBookAuthor.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mSupplier.setOnTouchListener(mTouchListener);

        setupSpinner();


    }

    //Method for creating an AlertDialog.Builder
    //Set the alert message and click listeners for the + and - buttons
    //@param DialogInterface.OnClickListener for the positive button
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.dialog_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_keep_editing, new DialogInterface.OnClickListener() {
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
    }

    //Override the activity's normal back button to hook up the showUnsavedChangesDialog()
    @Override
    public void onBackPressed() {
        //If the book entry form hasn't changed and mBookEntryChanged is false
        // use regular back  button functionality
        if (!mBookEntryChanged) {
            super.onBackPressed();
            return;
        }

        //Otherwise create the discardButtonClickListener and pass it into showUnsavedChangesDialog()
        //To alter the user that unsaved changes may be discarded
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            //User clicked the discard button, close the current activity
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //Set up the Spinner and the display options for the book supplier
    private void setupSpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(EditorActivity.this, R.array.supplier_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSupplier.setAdapter(adapter);

        //Get the user input for the item selected from the Spinner, and set it to the constants stored in BookContract.java
        mSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String supplierSelected = (String) parent.getItemAtPosition(position);
                if (supplierSelected.equals(getString(R.string.supplier_ebay))) {
                    mSelectedSupplier = BookEntry.SUPPLIER_EBAY;
                    supplierPhone = getString(R.string.ebay_phone);
                } else if (supplierSelected.equals(getString(R.string.supplier_amazon))) {
                    mSelectedSupplier = BookEntry.SUPPLIER_AMAZON;
                    supplierPhone = getString(R.string.amazon_phone);
                } else if (supplierSelected.equals(getString(R.string.supplier_abes))) {
                    mSelectedSupplier = BookEntry.SUPPLIER_ABES_BOOKS;
                    supplierPhone = getString(R.string.abes_phone);
                } else {
                    mSelectedSupplier = BookEntry.SUPPLIER_ATYPICAL;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedSupplier = BookEntry.SUPPLIER_ATYPICAL;
            }
        });
    }

    //helper method for validating user data before saving to database
    private boolean isBookEntryComplete() {

        //Extract the used input from EditFields
        titleString = mBookTitle.getText().toString().trim();
        authorString = mBookAuthor.getText().toString().trim();
        priceString = mPrice.getText().toString().trim();
        quantityString = mQuantity.getText().toString().trim();

        //If EditText isn't empty, use the user's input, otherwise keep value at 0 to avoid a crash.
        if (!TextUtils.isEmpty(priceString)) {
            priceInt = Double.parseDouble(priceString);
        }

        //If EditText isn't empty, use the user's input, otherwise keep value at 0 to avoid a crash.
        if (!TextUtils.isEmpty(quantityString)) {
            quantityInt = Integer.parseInt(quantityString);
        }

        //If any of the fields are empty, alter the user and a boolean value of false
        //Otherwise book is considered a valid database entry and could continue with saveBook()
        // validate title
        if (TextUtils.isEmpty(titleString)) {
            Toast.makeText(EditorActivity.this, R.string.editor_title_required_toast, Toast.LENGTH_SHORT).show();
            return false;
            //validate author
        } else if
                (TextUtils.isEmpty(authorString)) {
            Toast.makeText(EditorActivity.this, R.string.editor_author_required_toast, Toast.LENGTH_SHORT).show();
            return false;
            //validate quantity
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(EditorActivity.this, R.string.editor_quantity_required_toast, Toast.LENGTH_SHORT).show();
            return false;
            //validate price
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(EditorActivity.this, R.string.editor_price_required_toast, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    //Helper method for saving or updating a book
    private void saveBook() {

        //Create ContentValued object and populate with user values
        ContentValues values = new ContentValues();

        values.put(BookEntry.COLUMN_PRODUCT_NAME, titleString);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookEntry.COLUMN_PRICE, priceInt);
        values.put(BookEntry.COLUMN_QUANTITY, quantityInt);
        values.put(BookEntry.COLUMN_SUPPLIER, mSelectedSupplier);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);


        if (currentUri == null) {
            //If there is no existing Uri, insert the ContentValues and return a new Uri for this book
            //Notify the user if the insert was successful
            Uri newBookUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newBookUri == null) {
                Toast.makeText(EditorActivity.this, R.string.editor_save_error_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditorActivity.this, R.string.editor_book_saved_toast, Toast.LENGTH_SHORT).show();
            }

            //If a book record already exists, update any changes and return the affected rows
            //Notify the user if the insert was successful
        } else {
            int booksUpdated = getContentResolver().update(currentUri, values, null, null);
            if (booksUpdated == 0) {
                Toast.makeText(EditorActivity.this, R.string.editor_update_error_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditorActivity.this, R.string.editor_book_updated_toast, Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Inflate the menu in the res/menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    //Defines what happens when menu items are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                if (isBookEntryComplete()) {
                    saveBook();
                    finish();
                    return true;
                } else {
                    return false;
                }
            case R.id.menu_delete_book:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                //If mBookEntryChanged is false, navigate up normally
                if (!mBookEntryChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise set up a dialog to alert the user to unsaved changes and feed in the onClickListener
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //User clicked discard button, navigate to parent activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Delete a Book record
    public void deleteBook() {
        if (currentUri != null) {
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(EditorActivity.this, R.string.editor_delete_error_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditorActivity.this, R.string.editor_book_delete_toast, Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Show confirmation dialog before deleting the book
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message and +/- button click listeners
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
                finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentUri == null) {
            MenuItem deleteAll = menu.findItem(R.id.menu_delete_book);
            deleteAll.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER
        };

        return new CursorLoader(this, currentUri, projection, null, null, null);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        //Quit if Cursor is null or empty
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //If cursor successfully moved to first row, extract the values
        if (cursor.moveToFirst()) {
            String editTitle = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME));
            String editAuthor = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR));
            double editPrice = cursor.getDouble(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));
            int editQuantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
            int editSupplier = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER));

            //Set values on the editorActivity EditViews
            mBookTitle.setText(editTitle);
            mBookAuthor.setText(editAuthor);
            mPrice.setText((String.valueOf(editPrice)));
            mQuantity.setText(String.valueOf(editQuantity));

            switch (editSupplier) {
                case BookEntry.SUPPLIER_EBAY:
                    mSupplier.setSelection(1);
                    break;
                case BookEntry.SUPPLIER_AMAZON:
                    mSupplier.setSelection(2);
                    break;
                case BookEntry.SUPPLIER_ABES_BOOKS:
                    mSupplier.setSelection(3);
                    break;
                default:
                    mSupplier.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mBookTitle.getText().clear();
        mBookAuthor.getText().clear();
        mPrice.getText().clear();
        mQuantity.getText().clear();
        mSupplier.setSelection(0);
    }


}
