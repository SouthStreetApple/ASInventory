package com.example.android.asinventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.asinventory.InventoryContract.Inventory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonRowClick(View view) {
        displayDatabaseInfo();
    }

    public void buttonSaveData(View view) {
        //Save the data to the database
        setDatabaseInfo();
        //Update the database info
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo() {
        /**
         * This code displays data base information on the screen
         */

        //Initiate the database
        DbHelper mDbHelper = new DbHelper(this);

        //Create OR Open a Database so we can read data from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Now let's try to select some data and display it
        //Let's get all the rows from the DB table
        Cursor cursor = db.rawQuery("SELECT * FROM " + Inventory.TABLE_NAME, null);
        //Try Block, in case this doesn't work we don't crash the application
        try {
            //Show number of rows
            TextView textViewTableRows = (TextView) findViewById(R.id.text_view_table_rows);
            textViewTableRows.setText("Rows: " + cursor.getCount());
            //Display the rows in the lower text box
            //URL: https://stackoverflow.com/a/27362598/9849310
            Integer rowCount = cursor.getCount();
            Integer columnCount = cursor.getColumnCount();
            if (cursor.getCount() > 0) {
                TextView textViewTableContents = (TextView) findViewById(R.id.textview_table_contents);
                String tempTableContents = "";
                cursor.moveToFirst();
                do {
                    String row_values = "";

                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row_values = row_values + " || " + cursor.getString(i);
                    }
                    tempTableContents = tempTableContents + row_values + "\n";
                } while (cursor.moveToNext());
                textViewTableContents.setText(tempTableContents);
            }
        } finally {
            cursor.close();
        }
    }

    private void setDatabaseInfo() {
        /**
         * This code sets information into the database
         */

        //Initiate the database
        DbHelper mDbHelper = new DbHelper(this);

        //Create OR Open a Database so we can set data to it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //Now let's create the values variable
        ContentValues values = new ContentValues();

        //Now we'll retrieve the data from the users screen
        //We will also set he EditText boxes values to "" so that the user can enter another product
        String productName;
        EditText product_name = (EditText) findViewById(R.id.text_view_product_name);
        productName = product_name.getText().toString();
        product_name.setText("");

        String productPrice;
        EditText product_price = (EditText) findViewById(R.id.text_view_product_price);
        productPrice = product_price.getText().toString();
        product_price.setText("");

        String productQty;
        EditText product_qty = (EditText) findViewById(R.id.text_view_product_quantity);
        productQty = product_qty.getText().toString();
        product_qty.setText("");

        String productSupplierName;
        EditText product_supplier_name = (EditText) findViewById(R.id.text_view_product_supplier_name);
        productSupplierName = product_supplier_name.getText().toString();
        product_supplier_name.setText("");

        String productSupplierPhoneNumber;
        EditText product_supplier_phone_number = (EditText) findViewById(R.id.text_view_product_phone_number);
        productSupplierPhoneNumber = product_supplier_phone_number.getText().toString();
        product_supplier_phone_number.setText("");

        //Now we add all these values to the ContentValues variable we made
        values.put(Inventory.COLUMN_PRODUCT_NAME, productName);
        //values.put(Inventory.COLUMN_PRICE,Double.valueOf(productPrice));
        //values.put(Inventory.COLUMN_QUANTITY,Integer.valueOf(productQty));
        values.put(Inventory.COLUMN_PRICE, productPrice);
        values.put(Inventory.COLUMN_QUANTITY, productQty);
        values.put(Inventory.COLUMN_SUPPLIER_NAME, productSupplierName);
        values.put(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER, productSupplierPhoneNumber);

        //Now we have a try block to attempt to write data to the
        try {
            //Try to insert he data
            db.insert(Inventory.TABLE_NAME, null, values);
        } finally {
            //Nothing for now...
        }
    }


}
