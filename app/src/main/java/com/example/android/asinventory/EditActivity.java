package com.example.android.asinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Currency;

import com.example.android.asinventory.InventoryContract.Inventory;

public class EditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing product (null if it is new)
     */
    private Uri currentProductUri;

    /**
     * Boolean flag that will track if the product has been changed
     */
    private boolean productHasCHanged = false;

    /**
     * Setup the variables for the activity items
     */
    EditText productName;
    EditText productPrice;
    EditText productQty;
    EditText productSupplierName;
    EditText productSupplierPhoneNumber;

    /**
     * Now we will create an OnTouchListener that will tell if the user touches a view,
     * if this takes place we will assume that the user has changed the product
     */
    private View.OnTouchListener productTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent){
            productHasCHanged = true;
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        /**
         * Load the variables from the previous screen
         */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Load the songs array!
            currentProductUri = Uri.parse(extras.getString("currentProductUri"));
        }

        /**
         * Now we check the intent to examine if we are editing an old pet or a new one
         */
        //Intent intent = getIntent();
        //currentProductUri = intent.getData();

        /**
         * Initialize a Loader to load the data into the editors
         */
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER,null,this);


        /**
         * Load product information to the views
         */
        productName = (EditText) findViewById(R.id.text_view_product_name);
        productName.setOnTouchListener(productTouchListener);

        productPrice = (EditText) findViewById(R.id.text_view_product_price);
        productPrice.setOnTouchListener(productTouchListener);

        productQty = (EditText) findViewById(R.id.text_view_product_quantity);
        productQty.setOnTouchListener(productTouchListener);

        productSupplierName = (EditText) findViewById(R.id.text_view_product_supplier_name);
        productSupplierName.setOnTouchListener(productTouchListener);

        productSupplierPhoneNumber = (EditText) findViewById(R.id.text_view_product_phone_number);
        productSupplierPhoneNumber.setOnTouchListener(productTouchListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        /**
         * Now we want to define a projection to get all the data for each product
         */
        String[] projection = {
                Inventory._ID,
                Inventory.COLUMN_PRODUCT_NAME,
                Inventory.COLUMN_QUANTITY,
                Inventory.COLUMN_PRICE,
                Inventory.COLUMN_SUPPLIER_NAME,
                Inventory.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        /**
         * Setup a check to get out of this if the cursor is null or
         * if the table has less than one row
         */
        if(cursor == null || cursor.getCount() < 1){
            return;
        }

        if (cursor.moveToFirst()){
            /**
             * Now we're going to get the columns of the product attributes we want
             */
            int nameColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_PRICE);
            int qtyColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_QUANTITY);
            int manufNameColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_SUPPLIER_NAME);
            int manufPhoneColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER);

            /**
             * Now we're  going to get the values from the cursor for the given indices
             */
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String qty = cursor.getString(qtyColumnIndex);
            String manufName = cursor.getString(manufNameColumnIndex);
            String manufPhone = cursor.getString(manufPhoneColumnIndex);

            /**
             * Here is where we update the views
             */
            productName.setText(name);
            productPrice.setText(price);
            productQty.setText(qty);
            productSupplierName.setText(manufName);
            productSupplierPhoneNumber.setText(manufPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        /**
         * If the loader is invalidated then we need to null the data from the fields.
         */
        productName.setText("");
        productPrice.setText("");
        productQty.setText("");
        productSupplierName.setText("");
        productSupplierPhoneNumber.setText("");
    }

    /**
     * Now we make sure the user doesn't leave without saving, or at least warn them
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        /**
         * Now we're going to arrange to show the user a message should they try and leave
         * without saving.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to leave, you have unsaved changes.");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        /**
         * Now we'll create the dialog and show it
         */
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteProduct(){
        /**
         * We will only run this code if it is an existing product
         */
        if (currentProductUri != null){
            /**
             * Now we will delete the row
             */
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            /**
             * Show a message on if this removal worked
             */
            if (rowsDeleted == 0){
                Toast.makeText(this,"Something went wrong, product not deleted",Toast.LENGTH_LONG).show();
            } else {
                /**
                 * The delete must have worked
                 */
                Toast.makeText(this,"Product removed",Toast.LENGTH_LONG).show();
            }
        }
        /**
         * Close the edit activity
         */
        finish();
    }

    public void buttonDeleteData(View view){
        deleteProduct();
    }

    public void buttonSaveData(View view) {
        //Save the data to the database
        setDatabaseInfo();
    }

    private void setDatabaseInfo() {
        /**
         * This code sets information into the database
         */

        //Now let's create the values variable
        ContentValues values = new ContentValues();

        //Now we'll retrieve the data from the users screen
        //We will also set he EditText boxes values to "" so that the user can enter another product
        String productName;
        EditText product_name = (EditText) findViewById(R.id.text_view_product_name);
        productName = product_name.getText().toString().trim();

        String productPrice;
        EditText product_price = (EditText) findViewById(R.id.text_view_product_price);
        productPrice = product_price.getText().toString().trim();

        String productQty;
        EditText product_qty = (EditText) findViewById(R.id.text_view_product_quantity);
        productQty = product_qty.getText().toString().trim();

        String productSupplierName;
        EditText product_supplier_name = (EditText) findViewById(R.id.text_view_product_supplier_name);
        productSupplierName = product_supplier_name.getText().toString().trim();

        String productSupplierPhoneNumber;
        EditText product_supplier_phone_number = (EditText) findViewById(R.id.text_view_product_phone_number);
        productSupplierPhoneNumber = product_supplier_phone_number.getText().toString().trim();

        if(productName.equalsIgnoreCase("") || productPrice.equalsIgnoreCase("") || productQty.equalsIgnoreCase("") || productSupplierName.equalsIgnoreCase("")) {
            //Show toast because one of the EditText boxes is empty
            Toast t = Toast.makeText(this,getText(R.string.toast_data_missing),Toast.LENGTH_LONG);
            t.show();
        } else {
            //Now we add all these values to the ContentValues variable we made
            values.put(Inventory.COLUMN_PRODUCT_NAME, productName);
            values.put(Inventory.COLUMN_PRICE, Double.parseDouble(productPrice));
            values.put(Inventory.COLUMN_QUANTITY, Integer.parseInt(productQty));
            values.put(Inventory.COLUMN_SUPPLIER_NAME, productSupplierName);
            values.put(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER, productSupplierPhoneNumber);

            //Now we have a try block to attempt to write data to the
            try {
                //Try to insert the data
                //db.update(Inventory.TABLE_NAME,values,"_id="+currentProduct.ID,null);
                //db.insert(Inventory.TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.e("INSERT_ERROR",e.getMessage().toString());
            }
        }
    }
}
