package com.example.android.libratory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.libratory.data.BookContract.BookEntry;


public class BookCursorAdapter extends CursorAdapter {
    //Global variables for book quantity amounts that are updated via button
    private int bookId;
    private int quantityInt;

    public BookCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        //Locate the views in list_view.xml
        TextView titleView = (TextView) view.findViewById(R.id.item_title_textView);
        TextView authorView = (TextView) view.findViewById(R.id.item_author_textView);
        TextView priceView = (TextView) view.findViewById(R.id.item_price_textView);
        final TextView quantityView = (TextView) view.findViewById(R.id.item_quantity_textView);
        ImageButton saleButton = (ImageButton) view.findViewById(R.id.item_sale_button);
        //Set the cursor position on the saleButton so the onClick is recognized for a specific item
        saleButton.setTag(cursor.getPosition());

        //Extract the column indexes and values from the cursor
        String titleString = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME));
        String authorString = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR));
        double priceInt = cursor.getDouble(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));
        quantityView.setText(context.getString(R.string.item_quantity_Q) + Integer.toString(quantityInt));
        quantityInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));

        //Set the cursor values on the TextViews
        titleView.setText(titleString);
        authorView.setText(authorString);

        //Format the price to display in the user's currency preference
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMinimumFractionDigits(0);
        priceView.setText(format.format(priceInt));

        quantityView.setText(context.getString(R.string.item_quantity_Q) + Integer.toString(quantityInt));

        //Identify the cursor position
        final int position = cursor.getPosition();
        //Set an onClickListener on the sale button to decrement the quantity
        saleButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set the cursor to the position of the clicked button
                cursor.moveToPosition(position);
                //Get the book id of the current row
                bookId = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
                quantityInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
                //If quantity is greater than 0, decrease the quantity by 1 and update, the db and swap the cursor
                if (quantityInt > 0) {
                    quantityInt = quantityInt - 1;
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, quantityInt);

                    Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);
                    context.getContentResolver().update(updateUri, values, null, null);
                    swapCursor(cursor);

                }
            }
        });


    }


}
