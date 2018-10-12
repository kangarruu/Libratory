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
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.libratory.data.BookContract.BookEntry;


public class DetailActivity extends AppCompatActivity {

    //Declare the views to display book details
    private TextView mBookTitle;
    private TextView mBookAuthor;
    private TextView mPrice;
    private TextView mQuantity;
    private TextView mSupplier;

    //Declare the supplier selection to be assigned to constants in BookContract.java
    private int mSelectedSupplier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //locate the Views containing UserInput via findViewById
        mBookTitle = (TextView) findViewById(R.id.title_display_view);
        mBookAuthor = (TextView) findViewById(R.id.author_display_view);
        mPrice = (TextView) findViewById(R.id.price_display_view);
        mQuantity = (TextView) findViewById(R.id.quantity_display_view);
        mSupplier = (TextView) findViewById(R.id.supplier_display_view);

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
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
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
