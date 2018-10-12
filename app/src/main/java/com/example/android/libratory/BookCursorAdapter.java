package com.example.android.libratory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.libratory.data.BookContract;
import com.example.android.libratory.data.BookContract.BookEntry;


public class BookCursorAdapter extends CursorAdapter {


    public BookCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView titleView = (TextView) view.findViewById(R.id.item_title_textView);
        TextView authorView = (TextView) view.findViewById(R.id.item_author_textView);
        TextView priceView = (TextView) view.findViewById(R.id.item_price_textView);
        TextView quantityView = (TextView) view.findViewById(R.id.item_quantity_textView);
        ImageButton saleButton = (ImageButton) view.findViewById(R.id.item_sale_button);

        String titleString = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME));
        String authorString = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR));
        int quantityInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));
        int priceInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_PRICE));

        titleView.setText(titleString);
        authorView.setText(authorString);
        priceView.setText("$"+(Integer.toString(priceInt)));
        quantityView.setText(context.getString(R.string.item_quantity_Q)+Integer.toString(quantityInt));


    }
}
