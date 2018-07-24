package com.ozkan.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.ozkan.android.inventoryapp.data.InventoryContract.*;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    public static final int INVENTORY = 1000;
    public static final int INVENTORY_ID = 1001;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private InventoryDbHelper mDbHelper;

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Inventroy requires Product Name");
        }
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Inventroy requires valid quantity");
        }
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Inventroy requires valid price");
        }
        String suplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (suplierName == null) {
            throw new IllegalArgumentException("Inventroy requires Suplier Name");
        }
        String phoneNumber = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Inventroy requires Suplier PhoneNumber");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventroy requires Product Name");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Inventroy requires valid quantity");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(InventoryEntry.COLUMN_PRICE);
            if (price != null && price <= 0) {
                throw new IllegalArgumentException("Inventroy requires valid price");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {
            String suplierName = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (suplierName == null) {
                throw new IllegalArgumentException("Inventroy requires Suplier Name");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String phoneNumber = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (phoneNumber == null) {
                throw new IllegalArgumentException("Inventroy requires Suplier PhoneNumber");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
