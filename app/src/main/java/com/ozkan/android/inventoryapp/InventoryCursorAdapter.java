package com.ozkan.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ozkan.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView productNameTV = view.findViewById(R.id.product_name);
        TextView productPriceTV = view.findViewById(R.id.product_price);
        TextView productQuantityTV = view.findViewById(R.id.product_quantity);
        ImageButton saleButton = view.findViewById(R.id.sale_button);

        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        String productName = cursor.getString(productNameColumnIndex);
        String productPrice = cursor.getString(productPriceColumnIndex);
        String productQuantity = cursor.getString(productQuantityColumnIndex);

        productNameTV.setText(productName);
        productPriceTV.setText(productPrice);
        productQuantityTV.setText(productQuantity);

        final long id = cursor.getLong(cursor.getColumnIndex(InventoryEntry._ID));
        final int currentQuantity = Integer.parseInt(productQuantity);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = currentQuantity;
                if (quantity > 0) {
                    quantity -= 1;
                    ContentValues contentValues = new ContentValues();
                    Uri newUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                    contentValues.put(InventoryEntry.COLUMN_QUANTITY, quantity);
                    context.getContentResolver().update(newUri, contentValues, null, null);
                } else {
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
