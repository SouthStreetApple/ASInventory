package com.example.android.asinventory;

import android.app.DownloadManager;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.asinventory.InventoryContract.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * Identifier for the product data loader
     */
    private static final int PRODUCT_LOADER = 0;

    /**
     * Adapter for the product data loader
     */
    ProductCursorAdaptor productCursorAdapter;

    /**
     *ListView variable
     */
    ListView productList;

    /**
     * TextView variable
     */
    TextView emptyList;

    /**
     * Setup the menu
     * URL: https://stackoverflow.com/a/51773631/9849310
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.action_add_product){
            addProduct();
        }
        if (item.getItemId()==R.id.action_insert_dummy_data){
            Toast.makeText(this, "Insert Dummy Data", Toast.LENGTH_SHORT).show();
        }

        if (item.getItemId()==R.id.action_delete_all_entries){
            deleteAllProducts();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Loads the ListView into memory
         */
        productList = findViewById(R.id.list_view_products);

        /**
         * Load TextView into memory
         */
        emptyList = findViewById(R.id.text_view_empty_list);

        /**
         * Allows us to show the selected song as highlighted.
         * URL: https://stackoverflow.com/questions/5925892/how-to-highlight-row-in-listview-in-android
         */
        productList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        /**
         * Set an empty view for now, this will only show when the list has 0 items
         */
        View emptyView = findViewById(R.id.empty_view);
        productList.setEmptyView(emptyView);

        /**
         * Let's setup the adapter now!
         */
        productCursorAdapter = new ProductCursorAdaptor(this,null);

        /**
         * Set the listview adapter
         */
        productList.setAdapter(productCursorAdapter);

        /**
         * Set OnItemClickListener
         * We grab the index of the ListItem that was clicked then use that to look up the product
         * element in the products array.
         */
        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /**
                 * This is where we open the details view!
                 */
                /**
                 * Now I create an intent so that I can show the details screen
                 * We're going to pass the URI manually here, for some reason it would not pass
                 * automatically through the intent, so we do it here with putExtra
                 */

                //Gets the database index for the item
                int listItemIndex;
                listItemIndex = Integer.parseInt(view.getTag().toString());
                //launches the intent
                Intent intent = new Intent(getApplicationContext(),DetailsActivity.class);
                intent.putExtra("currentProductUri", Inventory.buildLocationUri(listItemIndex).toString());
                startActivity(intent);
                /**/
            }
        });
        /**
         * Start the loader
         */
        getLoaderManager().initLoader(PRODUCT_LOADER,null,this);
        /**
         * Check to see if the listview is empty
         * RegisterDataObserver idea from,
         * URL: https://stackoverflow.com/a/28823703/9849310
         */
        productCursorAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                /**
                 * Is the ListView empty?
                 */
                if (productCursorAdapter.getCount()==0){
                    /**
                     * Show a TextView or something that will tell the user to add some data
                     */
                    emptyList.setVisibility(View.VISIBLE);
                    /**/
                } else {
                    emptyList.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        /**
         * Now we define a projection that specifies the columns from the table we are working with
         */
        String[] projection  = {
                Inventory._ID,
                Inventory.COLUMN_PRODUCT_NAME,
                Inventory.COLUMN_PRICE,
                Inventory.COLUMN_QUANTITY,
                Inventory.COLUMN_SUPPLIER_NAME,
                Inventory.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        /**
         * Return  the new cursor loader
         */
        return new CursorLoader(this, //Parent Activity Conext (should be)
                Inventory.CONTENT_URI, //Provider conten URI to query
                projection, //Columns to include in the resulting cursor
                null, //No selection clause
                null, //No selection arguments
                null); //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        /**
         * This will update the ProductCursorAdapter with this new cursor
         */
        productCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        /**
         * Callback called when the data needs to be deleted
         */
        productCursorAdapter.swapCursor(null);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    public void buttonSaveData(View view) {
        //Save the data to the database
        setDatabaseInfo();
    }

    public void buttonSale(View view){
        /**
         * Let's sell an item, but make sure that there's nothing less than zero!
         */
        int qty;
        int listItemIndex;
        String[] projection;
        Uri uri;
        Cursor cursor;
        listItemIndex = Integer.parseInt(view.getTag().toString());
        /**
         * Build the URI
         */
        uri = Inventory.buildLocationUri(listItemIndex);
        /**
         * Get the cursor
         */
        cursor = getContentResolver().query(uri,null,null,null,null);
        /**
         * Get the current quantity
         */
        cursor.moveToFirst();
        qty = cursor.getInt(cursor.getColumnIndex(Inventory.COLUMN_QUANTITY));
        /**
         * Now we determine if the quantity is 0, if it is, we do nothing.
         * If it is 1 or more we subtract one.
         */
        if (qty == 0){
            //Do nothing
        } else {
            //Subtract one from the qty and save it.
            qty--;
            ContentValues values = new ContentValues();
            values = cursorRowToContentValues(cursor);
            values.put(Inventory.COLUMN_QUANTITY, qty);
            getContentResolver().update(uri,values,Inventory._ID+"=?",new String[] {String.valueOf(cursor.getColumnIndex(Inventory.COLUMN_QUANTITY))});
        }
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

    public void editDetails(View view) {
        /**
         * This shows a toast to illustrate the list item the button is on
         */
        int listItemIndex;
        listItemIndex = Integer.parseInt(view.getTag().toString());
        Toast.makeText(getApplicationContext(), "Select Item " + listItemIndex, Toast.LENGTH_SHORT).show();
        /**
         * Now I create an intent so that I can show the edit screen
         * We're going to pass the URI manually here, for some reason it would not pass
         * automatically through the intent, so we do it here with putExtra
         */
        Intent i = new Intent(MainActivity.this,EditActivity.class);
        i.putExtra("currentProductUri", Inventory.buildLocationUri(listItemIndex).toString());
        startActivity(i);
    }

    public void addProduct(){
        Intent i = new Intent(MainActivity.this,EditActivity.class);
        startActivity(i);
    }

    public void deleteAllProducts(){
        Integer i = getContentResolver().delete(Inventory.CONTENT_URI,null,null);
        Toast t = Toast.makeText(this,i + " items deleted.",Toast.LENGTH_LONG);
        t.show();
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
                getContentResolver().insert(Inventory.CONTENT_URI,values);
            } catch (Exception e) {
                Log.e("INSERT_ERROR",e.getMessage().toString());
            }
        }

    }
}
