package com.example.android.asinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private boolean productHasCHanged;

    /**
     * Setup the variables for the activity items
     */
    EditText productName;
    EditText productPrice;
    EditText productQty;
    EditText productSupplierName;
    EditText productSupplierPhoneNumber;
    Button productSave;

    /**
     * Variable for if this is a new or existing product
     */
    Boolean existingProduct;

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
         * Set producthasChanged
         */
        productHasCHanged = false;

        /**
         * Now we check the intent to examine if we are editing an old pet or a new one
         */
        if (currentProductUri == null){
            /**
             * This is a new product.
             */
            setTitle("Add a Product");
            /**
             * Hide the delete button
             */
            Button deleteButton = (Button) findViewById(R.id.button_delete);
            deleteButton.setVisibility(View.GONE);
            /**
             * Set boolean variable for existing product
             */
            existingProduct = false;
        } else {
            /**
             * Set boolean variable for existing product
             */
            existingProduct = true;
            /**
             * This is an existing product so let's do the following
             * Initialize a Loader to load the data into the editors
             */
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER,null,this);
        }

        /**
         * Set view variables
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

        productSave = (Button) findViewById(R.id.button_Save);
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
            int idColumnIndex = cursor.getColumnIndex(Inventory._ID);
            int nameColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_PRICE);
            int qtyColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_QUANTITY);
            int manufNameColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_SUPPLIER_NAME);
            int manufPhoneColumnIndex = cursor.getColumnIndex(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER);

            /**
             * Now we're  going to get the values from the cursor for the given indices
             */
            String id = cursor.getString(idColumnIndex);
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
            productSave.setTag(id);
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
     * Let's handle onBackPressed now
     */
    @Override
    public void onBackPressed(){
        /**
         * If the product hasn't changed then they can go back with no problem.
         */
        if (!productHasCHanged){
            super.onBackPressed();
            return;
        } else {
            /**
             * Product has changed, let's warn the user that we should save the data?
             */
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            /**
                             * The user clicked discard so we can exit
                             */
                            finish();
                        }
                    };
            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    /**
     * For the 'soft' backPress in the ActionBar
     * Now onBackPressed will be called when you use the actionbar
     * and the check to see if data has changed will be run.
     * URL: https://stackoverflow.com/a/46499387/9849310
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        setDatabaseInfo(view);
    }

    private void setDatabaseInfo(View view) {
        /**
         * This code sets information into the database
         */

        //Now let's create the values variable
        ContentValues values = new ContentValues();

        //Now we'll retrieve the data from the users screen
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

        if(validateData(productName,productPrice,productQty,productSupplierName,productSupplierPhoneNumber)==false) {
            //Return out of this if the data is not valid.
            return;
        } else {
            //Now we add all these values to the ContentValues variable we made
            values.put(Inventory.COLUMN_PRODUCT_NAME, productName);
            values.put(Inventory.COLUMN_PRICE, Double.parseDouble(productPrice));
            values.put(Inventory.COLUMN_QUANTITY, Integer.parseInt(productQty));
            values.put(Inventory.COLUMN_SUPPLIER_NAME, productSupplierName);
            values.put(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER, productSupplierPhoneNumber);
            //Now we have a try block to attempt to write data to the
            try {
                /**
                 * Check to see if this is a new or existing product
                 */
                Object result;
                if (existingProduct==true){
                    //This is an existing product
                    result = getContentResolver().update(Inventory.CONTENT_URI,values,"_id="+view.getTag(),null);
                } else {
                    //This is a new product
                    result = getContentResolver().insert(Inventory.CONTENT_URI,values);
                }
                //If the operation fails jump out of this, return.
                if (result == null){
                    return;
                }
                /**
                 * Close the activity
                 */
                finish();
            } catch (Exception e) {
                Log.e("INSERT_ERROR",e.getMessage().toString());
            }
        }
    }

    private boolean validateData(String productName, String productPrice, String productQuantity, String productSupplier, String productSupplierNumber){
        /**
         * Setup some variables to help us.
         */
        String toastMessage = "";
        Boolean returnResult = true;
        /**
         * This is where we will validate the data before sending it to the contentResolver
         */
        if (productName==null || productName.equalsIgnoreCase("")){
            toastMessage = toastMessage + "\n" + "Please enter a valid name.";
            returnResult = false;
        }

        if (productPrice.isEmpty() || !isNumeric(productPrice) || Double.parseDouble(productPrice)<0 ||  productPrice == null){
            toastMessage = toastMessage + "\n" + "Please enter a valid price.";
            returnResult = false;
        }

        if (productQuantity.isEmpty()||!isNumeric(productPrice)||Integer.parseInt(productQuantity) < 0 || productQuantity == null){
            toastMessage = toastMessage + "\n" + "Please enter a valid quantity (must be greater then zero).";
            returnResult = false;
        }

        if (productSupplier == null || productSupplier.equalsIgnoreCase("")){
            toastMessage = toastMessage + "\n" + "Please enter a valid supplier name.";
            returnResult = false;
        }

        if (validPhoneNumber(productSupplierNumber) == false){
            toastMessage = toastMessage + "\n" + "Please enter a valid supplier phone number.";
            returnResult = false;
        }

        if (returnResult == false){
            /**
             * Give message telling user what to do to correct the product information
             * Multi line Toasts can be shown using the below method
             * URL: https://stackoverflow.com/a/43631673/9849310
             */
            Toast t = Toast.makeText(this,toastMessage,Toast.LENGTH_LONG);
            t.show();
            return returnResult;
        } else {
            return returnResult;
        }
    }

    /**
     * Will help us determine if it is a number
     * URL: https://stackoverflow.com/a/1102916/9849310
     */
    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    /**
     * This will return true if it looks like a valid number.
     * URL: https://stackoverflow.com/a/23675722/9849310
     */
    public boolean validPhoneNumber(String number)
    {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }
}
