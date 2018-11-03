package com.example.android.asinventory;

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
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.asinventory.InventoryContract.Inventory;

public class DetailsActivity extends AppCompatActivity implements
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
    TextView productName;
    TextView productPrice;
    TextView productQty;
    TextView productSupplierName;
    TextView productSupplierPhoneNumber;
    EditText et_qtyAmountChanged;

    /**
     * Variable that holds the amount of product to increase or decrease by
     */
    int qtyAmountChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        qtyAmountChange = 1;

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
        if (currentProductUri == null){
            /**
             * This is a new product.
             */
            setTitle("Add a Product");
        } else {
            /**
             * This is an existing product so let's do the following
             * Initialize a Loader to load the data into the editors
             */
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER,null,this);
        }

        /**
         * Load product information to the views
         */
        productName = (TextView) findViewById(R.id.text_view_product_name);
        productPrice = (TextView) findViewById(R.id.text_view_product_price);
        productQty = (TextView) findViewById(R.id.text_view_product_quantity);
        productSupplierName = (TextView) findViewById(R.id.text_view_product_supplier_name);
        productSupplierPhoneNumber = (TextView) findViewById(R.id.text_view_product_phone_number);
        et_qtyAmountChanged = (EditText) findViewById(R.id.edit_text_increase_amount);
        et_qtyAmountChanged.setText(String.valueOf(qtyAmountChange));
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
            productPrice.setText(this.getResources().getString(R.string.money_sign,price));
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
        /**
         * Let's create a dialog here to check that the user is sure!
         * URL: https://stackoverflow.com/a/5127506/9849310
         */
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Do you really want to delete the product?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Delete the product
                        deleteProduct();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void buttonReduceQty(View view){
        /**
         * Setting increase amount
         */
        qtyAmountChange = Integer.parseInt(et_qtyAmountChanged.getText().toString());
        /**
         * This will reduce the Qty. of product by the amount in the EditText
         */
        int qty = Integer.valueOf(productQty.getText().toString());
        if ((qty-qtyAmountChange) >= 0 ){
            qty = qty - qtyAmountChange;
            //Now we need to update the database
            saveQtyValue(qty);
            productQty.setText(String.valueOf(qty));
        } else {
            //We can't go below zero, so we do nothing.
        }
    }

    public void buttonIncreaseQty(View view){
        /**
         * Setting decrease amount
         */
        qtyAmountChange = Integer.parseInt(et_qtyAmountChanged.getText().toString());
        /**
         * This will reduce the Qty. of product by the amount in the EditText
         */
        int qty = Integer.valueOf(productQty.getText().toString());
        qty = qty + qtyAmountChange;
        //Now we need to update the database
        saveQtyValue(qty);
        productQty.setText(String.valueOf(qty));
    }

    public void callContact(View view){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String phone =  productSupplierPhoneNumber.getText().toString();
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    public void saveQtyValue(int i){
        /**
         * Let's sell an item, but make sure that there's nothing less than zero!
         */
        Cursor cursor;
        int qty = i;

        /**
         * Get the cursor
         */
        cursor = getContentResolver().query(currentProductUri,null,null,null,null);
        /**
         * Get the current quantity
         */
        cursor.moveToFirst();

        //Subtract one from the qty and save it.
        ContentValues values = new ContentValues();
        values = cursorRowToContentValues(cursor);
        values.put(Inventory.COLUMN_QUANTITY, qty);
        getContentResolver().update(currentProductUri,values,Inventory._ID+"=?",new String[] {String.valueOf(cursor.getColumnIndex(Inventory.COLUMN_QUANTITY))});

        /**
         * Close the cursor
         */
        cursor.close();
    }

    /**
     * The below is some very useful code to get all the content values
     * URL: https://stackoverflow.com/a/28019056/9849310
     */
    public static ContentValues cursorRowToContentValues(Cursor cursor) {
        ContentValues values = new ContentValues();
        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            switch (cursor.getType(i)) {
                case Cursor.FIELD_TYPE_NULL:
                    values.putNull(columns[i]);
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    values.put(columns[i], cursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    values.put(columns[i], cursor.getDouble(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    values.put(columns[i], cursor.getString(i));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    values.put(columns[i], cursor.getBlob(i));
                    break;
            }
        }
        return values;
    }
}
