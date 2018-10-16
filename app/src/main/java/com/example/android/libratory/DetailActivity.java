package com.example.android.libratory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.libratory.data.BookContract.BookEntry;


public class DetailActivity extends AppCompatActivity implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    //Required param that identifies the CursorLoader. Can be any int.
    private static final int URL_LOADER = 0;

    //Declare the views to display book details and setOnClickListeners
    private TextView mBookTitle;
    private TextView mBookAuthor;
    private TextView mPrice;
    private TextView mQuantity;
    private TextView mSupplier;
    private TextView mSupplierPhone;

    private ImageButton mIncrement;
    private ImageButton mDecrement;
    private ImageButton mCallSupplier;

    //Variable to store Uri passed in via intent
    private Uri currentUri;

    //Variables for saving values in currentUri
    private String displayTitle;
    private String displayAuthor;
    private double displayPrice;
    private int displayQuantity;
    private int displaySupplier;
    private String displaySupplierPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Get the URI from the MainActivity via passed intent
        Intent intent = getIntent();
        currentUri = intent.getData();

        //locate the Views containing UserInput via findViewById
        mBookTitle = (TextView) findViewById(R.id.title_display_view);
        mBookAuthor = (TextView) findViewById(R.id.author_display_view);
        mPrice = (TextView) findViewById(R.id.price_display_view);
        mQuantity = (TextView) findViewById(R.id.quantity_display_view);
        mSupplier = (TextView) findViewById(R.id.supplier_display_view);
        mSupplierPhone = (TextView) findViewById(R.id.supplier_phone_display_view);
        mDecrement = (ImageButton) findViewById(R.id.quantity_decrement);
        mIncrement = (ImageButton) findViewById(R.id.quantity_increment);
        mCallSupplier = (ImageButton) findViewById(R.id.supplier_call_button);

        mDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementQuantity();
            }
        });

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementQuantity();
            }
        });

        //Initialize the LoaderManager
        getSupportLoaderManager().initLoader(URL_LOADER, null, this);

        //Set an intent to call the supplier when phone button is clicked
        mCallSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", displaySupplierPhone, null));
                startActivity(intent);
            }


        });


    }

    //Helper method for increasing stock quantity via onClickListener on button
    private void incrementQuantity() {
        if (displayQuantity >= 0) {
            displayQuantity = displayQuantity + 1;
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_QUANTITY, displayQuantity);
            getContentResolver().update(currentUri, values, null, null);
        }
    }

    //Helper method for decreasing stock quantity via onClickListener on button
    private void decrementQuantity() {
        if (displayQuantity > 0) {
            displayQuantity = displayQuantity - 1;
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_QUANTITY, displayQuantity);
            getContentResolver().update(currentUri, values, null, null);
        }
    }

    //Inflate the menu in the res/menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_book:
                Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
                intent.setData(currentUri);
                startActivity(intent);
                return true;
            case R.id.menu_delete_book:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Delete a Book record
    private void deleteBook() {
        if (currentUri != null) {
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(DetailActivity.this, R.string.editor_delete_error_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailActivity.this, R.string.editor_book_delete_toast, Toast.LENGTH_SHORT).show();
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


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER,
                BookEntry.COLUMN_SUPPLIER_PHONE
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
            displayTitle = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME));
            displayAuthor = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR));
            displayPrice = cursor.getDouble(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));
            displayQuantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
            displaySupplier = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER));
            displaySupplierPhone = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE));

            //Set values on the DetailActivity Views
            mBookTitle.setText(displayTitle);
            mBookAuthor.setText(displayAuthor);
            mQuantity.setText(String.valueOf(displayQuantity));

            //Format the price to display in the user's currency preference
            NumberFormat format = NumberFormat.getCurrencyInstance();
            format.setMinimumFractionDigits(0);
            mPrice.setText(format.format(displayPrice));

            //Format and set the supplier's phone number
            String formattedNumber = PhoneNumberUtils.formatNumber(displaySupplierPhone);
            mSupplierPhone.setText(formattedNumber);

            //Set the supplier name depending db value
            switch (displaySupplier) {
                case 0:
                    mSupplier.setText(R.string.supplier_other);
                    break;
                case 1:
                    mSupplier.setText(R.string.supplier_ebay);
                    break;
                case 2:
                    mSupplier.setText(R.string.supplier_amazon);
                    break;
                case 3:
                    mSupplier.setText(R.string.supplier_abes);
                    break;

            }

        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mBookTitle.setText("");
        mBookAuthor.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mSupplier.setText("");
        mSupplierPhone.setText("");
    }
}
