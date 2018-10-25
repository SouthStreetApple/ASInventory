package com.example.android.asinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;

import com.example.android.asinventory.InventoryContract.Inventory;

public class EditActivity extends AppCompatActivity {

    int listItemIndex;
    ArrayList<product> products;
    product currentProduct;

    //Initiate the database
    DbHelper mDbHelper;

    //Create OR Open a Database so we can set data to it
    SQLiteDatabase db;

    //Product Provider
    //ProductProvider productProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        /**
         * Initialize Database
         */
        //mDbHelper = new DbHelper(this);
        //db = mDbHelper.getWritableDatabase();

        //Create product provider
        //productProvider = new ProductProvider();


        /**
         * Load the variables from the previous screen
         */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Load the products array!
            products = extras.getParcelableArrayList("products");
            //Gets which product we are working with in the array list
            listItemIndex = extras.getInt("listItemIndex");
            currentProduct = products.get(listItemIndex);
        }

        /**
         * Load product information to the views
         */
        EditText productName = (EditText) findViewById(R.id.text_view_product_name);
        productName.setText(currentProduct.productName);

        EditText productPrice = (EditText) findViewById(R.id.text_view_product_price);
        productPrice.setText(currentProduct.price.toString());

        EditText productQty = (EditText) findViewById(R.id.text_view_product_quantity);
        productQty.setText(currentProduct.quantity.toString());

        EditText productSupplierName = (EditText) findViewById(R.id.text_view_product_supplier_name);
        productSupplierName.setText(currentProduct.supplierName);

        EditText productSupplierPhoneNumber = (EditText) findViewById(R.id.text_view_product_phone_number);
        productSupplierPhoneNumber.setText(currentProduct.supplierPhoneNumber);


    }

    public void buttonDeleteData(View view){
        //Delete the data
        if (deleteDatabaseInfo()){
            //Now we kick the user our of this view, as this record no longer exists.
            //db.close();
            super.onBackPressed();
        };
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
                db.update(Inventory.TABLE_NAME,values,"_id="+currentProduct.ID,null);
                //db.insert(Inventory.TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.e("INSERT_ERROR",e.getMessage().toString());
            }
        }

    }

    private boolean deleteDatabaseInfo(){
        /*//Delete this record in the database
        Boolean result;
        result =  db.delete(Inventory.TABLE_NAME, "_id="+currentProduct.ID, null) > 0;

        //Should notify that a change has been made.
        //this.getBaseContext().getContentResolver().notifyChange(Inventory.buildLocationUri(currentProduct.ID),null); //<- ALWAYS ERRORS
        return result;*/


        Integer result;
        //Log Location
        Log.e("DATABASE LOCATION:",Inventory.buildLocationUri(currentProduct.ID).toString());
        result = getContentResolver().delete(Inventory.buildLocationUri(currentProduct.ID),currentProduct.productName,null);
        if (result > 0){
           return true;
        } else {
            return false;
        }
    }
}
