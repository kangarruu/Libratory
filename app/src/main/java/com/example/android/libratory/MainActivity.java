package com.example.android.libratory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import com.example.android.libratory.data.BookContract.BookEntry;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.libratory.data.BookContract;
import com.example.android.libratory.data.BookDbHelper;

public class MainActivity extends AppCompatActivity {
    //Declare a BookDbHelper
    private BookDbHelper mDbHelper;

    //Declare the views containing user input
    private EditText mBookTitle;
    private EditText mBookAuthor;
    private EditText mPrice;
    private EditText mQuantity;
    private Spinner mSupplier;
    private EditText mSupplierPhone;
    private TextView mSummaryView;

    //Declare the supplier selection to be assigned to constants in BookContract.java
    private int mSelectedSupplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //locate the Views containing UserInput via findViewById
        mBookTitle = (EditText) findViewById(R.id.title_edit_view);
        mBookAuthor = (EditText) findViewById(R.id.author_edit_view);
        mPrice =(EditText) findViewById(R.id.price_edit_view);
        mQuantity = (EditText) findViewById(R.id.quantity_edit_view);
        mSupplierPhone = (EditText) findViewById(R.id.phone_edit_view);
        mSupplier = (Spinner) findViewById(R.id.supplier_spinner);
        mSummaryView = (TextView) findViewById(R.id.summary_view);

        //Instantiate the mDbHelper to be used in creating, editing and reading from the db
        mDbHelper = new BookDbHelper(this);

        setupSpinner();

        mSummaryView.setText("Current Summary: \n"  +
                    BookEntry._ID + " | " +
                    BookEntry.COLUMN_PRODUCT_NAME + " | " +
                    BookEntry.COLUMN_BOOK_AUTHOR + " | " +
                    BookEntry.COLUMN_SUPPLIER);
    }

    //Set up the Spinner and the display options for the book supplier
    private void setupSpinner(){
        ArrayAdapter adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.supplier_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSupplier.setAdapter(adapter);

    //Get the user input for the item selected from the Spinner, and set it to the constants stored in BookContract.java
        mSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String supplierSelected = (String) parent.getItemAtPosition(position);
                if(supplierSelected.equals(getString(R.string.supplier_amazon))){
                    mSelectedSupplier = BookEntry.SUPPLIER_AMAZON;
                }else if(supplierSelected.equals(getString(R.string.supplier_ebay))){
                    mSelectedSupplier = BookEntry.SUPPLIER_EBAY;
                }else if(supplierSelected.equals(getString(R.string.supplier_abes))){
                    mSelectedSupplier = BookEntry.SUPPLIER_ABES_BOOKS;
                }else {
                    mSelectedSupplier = BookEntry.SUPPLIER_ATYPICAL;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedSupplier = BookEntry.SUPPLIER_ATYPICAL;
            }
        });
    }

    private void insertBook(){
        //Get user info from the edit fields and save them as a new book entry
        String titleString = mBookTitle.getText().toString().trim();
        String authorString = mBookAuthor.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        int priceInt = Integer.parseInt(priceString);
        String quantityString = mQuantity.getText().toString().trim();
        int quantityInt = Integer.parseInt(quantityString);
        String phoneString = mSupplierPhone.getText().toString().trim();


        //Get an instance of a writable db to be able to insert content into the db
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //Create a ContentValues object, insert values into it and then insert the object into the db
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, titleString);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookEntry.COLUMN_PRICE, priceInt);
        values.put(BookEntry.COLUMN_QUANTITY, quantityInt);
        values.put(BookEntry.COLUMN_SUPPLIER, mSelectedSupplier);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, phoneString);

        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);
        if (newRowId == -1){
            Toast.makeText(MainActivity.this, "Error with saving book", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Book saved with id: " + newRowId, Toast.LENGTH_SHORT).show();

        }

    }

    //Inflate the menu in the res/menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //When save option is clicked in the menu, add book entry to the db
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save){
            insertBook();
            displayDatabaseInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Display the Id, Title, Author and Supplier by creating a Cursor that extracts that info
    private void displayDatabaseInfo(){
        //Create a readable database using the mDbHelper
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Define a projection to display desired columns:
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_SUPPLIER
         };

        //Create a cursor using the projection above
        Cursor cursor = db.query(BookEntry.TABLE_NAME, projection, null,null, null, null, null, null );

        //Identify the index of the 4 columns to display:
        int idIndex = cursor.getColumnIndex(BookEntry._ID);
        int titleIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int authorIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
        int supplierIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER);

        //Iterate through all the rows in the cursor and pull out the entries in the columns selected
        while (cursor.moveToNext()){
            int currentId = cursor.getInt(idIndex);
            String currentTitle = cursor.getString(titleIndex);
            String currentAuthor = cursor.getString(authorIndex);
            int currentSupplier = cursor.getInt(supplierIndex);

            //Temporarily display these rows at the beneath the input form
            mSummaryView.append(("\n" + currentId + " | " +
                        currentTitle + " | " +
                        currentAuthor + " | " +
                        currentSupplier));
        }

        cursor.close();
    }
}
