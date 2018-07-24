package com.ozkan.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ozkan.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EXISTING_INVENTORY_LOADER = 0;
    private Uri currentUri;
    private EditText name;
    private EditText quantity;
    private EditText price;
    private EditText supplierName;
    private EditText supplierPhone;
    private ImageButton plusButton;
    private ImageButton minusButton;
    private boolean itemHasChanged = false;
    private FirebaseAnalytics mFirebaseAnalytics;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent();
                callIntent.setAction(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + supplierPhone.getText().toString().trim()));

                if (callIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(callIntent);
                }
            }
        });

        Intent intent = getIntent();
        currentUri = intent.getData();
        if (currentUri == null) {
            setTitle("Add new item");
        } else {
            setTitle("Edit item");
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        name = findViewById(R.id.name);
        quantity = findViewById(R.id.quantity);
        price = findViewById(R.id.price);
        supplierName = findViewById(R.id.supplier_name);
        supplierPhone = findViewById(R.id.supplier_phone);
        minusButton = findViewById(R.id.minus_button);
        plusButton = findViewById(R.id.plus_button);

        name.setOnTouchListener(touchListener);
        quantity.setOnTouchListener(touchListener);
        price.setOnTouchListener(touchListener);
        supplierName.setOnTouchListener(touchListener);
        supplierPhone.setOnTouchListener(touchListener);

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityValue = quantity.getText().toString().trim();
                int quantityValueInt;
                if (quantityValue.isEmpty() || quantityValue.equals("")) {
                    quantityValueInt = 0;
                    quantity.setText(String.valueOf(quantityValueInt));
                } else {
                    quantityValueInt = Integer.parseInt(quantityValue);
                    if (quantityValueInt > 0) {
                        quantityValueInt -= 1;
                        quantity.setText(String.valueOf(quantityValueInt));
                    } else {
                        Toast.makeText(EditorActivity.this, getString(R.string.quantity_bigger_than_zero), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityValue = quantity.getText().toString().trim();
                int quantityValueInt;
                if (quantityValue.isEmpty() || quantityValue.equals("")) {
                    quantityValueInt = 0;
                    quantityValueInt += 1;
                    quantity.setText(String.valueOf(quantityValueInt));
                } else {
                    quantityValueInt = Integer.parseInt(quantityValue);
                    quantityValueInt += 1;
                    quantity.setText(String.valueOf(quantityValueInt));
                }
            }
        });
    }

    private boolean saveItem() {
        String nameString = name.getText().toString().trim();
        String quantityString = quantity.getText().toString().trim();
        String priceString = price.getText().toString().trim();
        String supplierNameString = supplierName.getText().toString().trim();
        String supplierPhoneString = supplierPhone.getText().toString().trim();

        if (currentUri == null && (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierPhoneString))) {
            Toast.makeText(this, getString(R.string.editor_fill_all_sections), Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);

        int quantityValue = 0;
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(getApplicationContext(), getString(R.string.quantity_not_valid), Toast.LENGTH_SHORT).show();
            return false;
        }
        quantityValue = Integer.parseInt(quantityString);

        double priceValue = 0.0;
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(getApplicationContext(), getString(R.string.price_not_valid), Toast.LENGTH_SHORT).show();
            return false;
        }
        priceValue = Double.parseDouble(priceString);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantityValue);
        values.put(InventoryEntry.COLUMN_PRICE, priceValue);

        if (currentUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowAffected = getContentResolver().update(currentUri, values, null, null);
            if (rowAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_item_successful), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveItem()) {
                    finish();
                    return true;
                }
                break;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,
                currentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int supplierNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String nameValue = data.getString(nameColumnIndex);
            String supplierNameValue = data.getString(supplierNameColumnIndex);
            String supplierPhoneValue = data.getString(supplierPhoneColumnIndex);
            double priceValue = data.getDouble(priceColumnIndex);
            int quantityValue = data.getInt(quantityColumnIndex);

            name.setText(nameValue);
            quantity.setText(Integer.toString(quantityValue));
            price.setText(Double.toString(priceValue));
            supplierName.setText(supplierNameValue);
            supplierPhone.setText(supplierPhoneValue);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        name.setText("");
        quantity.setText("");
        price.setText("");
        supplierName.setText("");
        supplierPhone.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.unsaved_changes_dialog_msg));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_dialog_msg));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (currentUri != null) {
            int rowDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
