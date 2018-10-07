package com.example.android.asinventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.asinventory.InventoryContract.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    /**
     * Product data variables
     */
    int numberOfProducts;
    int selectedProduct;
    ArrayList<product> products;

    /**
     *ListView variable
     */
    ListView productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Loads the ListView into memory
         */
        productList = findViewById(R.id.list_view_products);

        /**
         * Allows us to show the selected song as highlighted.
         * URL: https://stackoverflow.com/questions/5925892/how-to-highlight-row-in-listview-in-android
         */
        productList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        /**
         * Set OnItemClickListener
         * We grab the index of the ListItem that was clicked then use that to look up the song
         * element in the songs array.
         */
        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Item is selected so we should do something?
                Toast.makeText(getApplicationContext(), products.get(i).productName, Toast.LENGTH_SHORT).show();
                selectedProduct = i;
            }
        });



        /**
         * Load product data
         */
        numberOfProducts = getDatabaseRowCount();
        products = new ArrayList<product>();

        //Load the products
        loadDatabaseRows();

    }

    public void buttonRowClick(View view) {
        displayDatabaseInfo();
    }

    public void buttonSaveData(View view) {
        //Save the data to the database
        setDatabaseInfo();
    }

    public void editDetails(View view) {
        //We clicked details int he listitem, let's do something here.
        ListView listView;
        listView = findViewById(((ListView)view.getParent()).getId());
        int listItemIndex;
        listItemIndex = listView.getSelectedItemPosition();
        Toast.makeText(getApplicationContext(), "Select Item " + listItemIndex, Toast.LENGTH_SHORT).show();
    }
    

    private int getDatabaseRowCount(){
        /**
         * This will retrieve the number of rows in the table
         */

        //Initiate the database
        DbHelper mDbHelper = new DbHelper(this);

        //Create OR Open a Database so we can read data from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Now let's try to select some data and display it
        //Let's get all the rows from the DB table
        Cursor cursor = db.rawQuery("SELECT * FROM " + Inventory.TABLE_NAME, null);

        //set the row count variable
        int numberOfROws = cursor.getCount();

        //close cursor
        cursor.close();

        return numberOfROws;
    }

    private void loadDatabaseRows() {
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
            //Display the rows in the lower text box
            //URL: https://stackoverflow.com/a/27362598/9849310
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                //Create variables to hold the data
                String productIndex ="0";
                String productName ="";
                String productPrice = "";
                String productQuantity = "";
                String manufacturer = "" ;
                String manufacturerPhoneNumber = "";
                //This allows us to recursively move through the rows
                do {
                    /*Create new instance of the custom class product*/
                    product p = new product();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        switch (i){
                            case 0:
                                //This is the row index
                                productIndex = cursor.getString(i);
                                break;
                            case 1:
                                //This should be the product name
                                productName = cursor.getString(i);
                                break;
                            case 2:
                                //This should be the price
                                productPrice = cursor.getString(i);
                                break;
                            case 3:
                                //This should be the quantity
                                productQuantity = cursor.getString(i);
                                if (productQuantity==null){
                                    productQuantity="0";
                                }
                                break;
                            case 4:
                                //This should be the manufacturer name
                                manufacturer = cursor.getString(i);
                                break;
                            case 5:
                                //This should be the manufacturer phone number
                                manufacturerPhoneNumber = cursor.getString(i);
                                break;
                            default:
                                //Do nothing
                        }
                    }
                    //Now we add the variables into the custom class product
                    p.setProduct(Integer.parseInt(productIndex),productName,Double.parseDouble(productPrice),Integer.parseInt(productQuantity),manufacturer,manufacturerPhoneNumber);
                    //Add the product to the ArrayList
                    products.add(p);
                    //Write to the Log
                    Log.e("Adding Product: ",p.productName);
                } while (cursor.moveToNext());
                /**
                 * Now we create a new custom adaptor pa, productAdaptor, using this context
                 * and the arrayList products, then set the adaptor to the ListView
                 */
                productAdaptor pa = new productAdaptor(this,products);
                productList.setAdapter(pa);
            }
        } finally {
            //We close our cursor so that it does not remain open.
            cursor.close();
        }
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
            if (cursor.getCount() > 0) {
                TextView textViewTableContents = (TextView) findViewById(R.id.textview_table_contents);
                String tempTableContents = "";
                cursor.moveToFirst();
                //This allows us to recursively move through the rows
                do {
                    String row_values = "";

                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        //This gathers each rows column data and adds it to the row_values variable
                        row_values = row_values + " || " + cursor.getString(i);
                    }
                    //This builds the string with a newline at the end so that it is easier to read
                    tempTableContents = tempTableContents + row_values + "\n";
                } while (cursor.moveToNext());
                //Sets the text of the TableContents view.
                textViewTableContents.setText(tempTableContents);
            }
        } finally {
            //We close our cursor so that it does not remain open.
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
        productName = product_name.getText().toString().trim();
        product_name.setText("");

        String productPrice;
        EditText product_price = (EditText) findViewById(R.id.text_view_product_price);
        productPrice = product_price.getText().toString().trim();
        product_price.setText("");

        String productQty;
        EditText product_qty = (EditText) findViewById(R.id.text_view_product_quantity);
        productQty = product_qty.getText().toString().trim();
        product_qty.setText("");

        String productSupplierName;
        EditText product_supplier_name = (EditText) findViewById(R.id.text_view_product_supplier_name);
        productSupplierName = product_supplier_name.getText().toString().trim();
        product_supplier_name.setText("");

        String productSupplierPhoneNumber;
        EditText product_supplier_phone_number = (EditText) findViewById(R.id.text_view_product_phone_number);
        productSupplierPhoneNumber = product_supplier_phone_number.getText().toString().trim();
        product_supplier_phone_number.setText("");

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
                db.insert(Inventory.TABLE_NAME, null, values);
            } catch (Exception e) {
                Log.e("INSERT_ERROR",e.getMessage().toString());
            }
            //Update the database info
            displayDatabaseInfo();
        }

    }


}
