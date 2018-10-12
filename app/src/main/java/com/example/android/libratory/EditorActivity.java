package com.example.android.libratory;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.libratory.data.BookContract.BookEntry;


public class EditorActivity extends AppCompatActivity {

    //Declare the views containing user input
    private EditText mBookTitle;
    private EditText mBookAuthor;
    private EditText mPrice;
    private EditText mQuantity;
    private Spinner mSupplier;

    //Declare the supplier selection to be assigned to constants in BookContract.java
    private int mSelectedSupplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        //locate the Views containing UserInput via findViewById
        mBookTitle = (EditText) findViewById(R.id.title_edit_view);
        mBookAuthor = (EditText) findViewById(R.id.author_edit_view);
        mPrice = (EditText) findViewById(R.id.price_edit_view);
        mQuantity = (EditText) findViewById(R.id.quantity_edit_view);
        mSupplier = (Spinner) findViewById(R.id.supplier_spinner);

        setupSpinner();

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
                if (supplierSelected.equals(getString(R.string.supplier_amazon))) {
                    mSelectedSupplier = BookEntry.SUPPLIER_AMAZON;
                } else if (supplierSelected.equals(getString(R.string.supplier_ebay))) {
                    mSelectedSupplier = BookEntry.SUPPLIER_EBAY;
                } else if (supplierSelected.equals(getString(R.string.supplier_abes))) {
                    mSelectedSupplier = BookEntry.SUPPLIER_ABES_BOOKS;
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

    private void insertBook() {
        //Get user info from the edit fields and save them as a new book entry
        String titleString = mBookTitle.getText().toString().trim();
        String authorString = mBookAuthor.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        double priceInt = Double.parseDouble(priceString);
        String quantityString = mQuantity.getText().toString().trim();
        int quantityInt = Integer.parseInt(quantityString);

        //Create a ContentValues object, insert values into it and then insert the object into the db
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, titleString);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookEntry.COLUMN_PRICE, priceInt);
        values.put(BookEntry.COLUMN_QUANTITY, quantityInt);
        values.put(BookEntry.COLUMN_SUPPLIER, mSelectedSupplier);

        Uri newBookUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
        if (newBookUri == null) {
            Toast.makeText(EditorActivity.this, R.string.editor_save_error_toast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(EditorActivity.this, R.string.editor_book_saved_toast, Toast.LENGTH_SHORT).show();

        }

    }

    //Inflate the menu in the res/menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Defines what happens when menu items are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_save:
                insertBook();
                finish();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem dummyData = menu.findItem(R.id.menu_insert_dummy_data);
        dummyData.setVisible(false);

        MenuItem newBook = menu.findItem(R.id.menu_enter_new_book);
        newBook.setVisible(false);

        MenuItem deleteAll = menu.findItem(R.id.menu_delete_all);
        deleteAll.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

}
