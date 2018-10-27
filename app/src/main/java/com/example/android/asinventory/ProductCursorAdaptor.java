package com.example.android.asinventory;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductCursorAdaptor extends CursorAdapter {

    public ProductCursorAdaptor(Context context, Cursor c){
        super(context,c,0);
    }

    @Override
    public View newView(Context context,Cursor cursor, ViewGroup parent){
        //This is where we inflate our listview item
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @NonNull
    @Override
    public void bindView(View view,Context context, Cursor cursor){
        /**
         * This is where we setup our product view so that it contains the right data we then return
         * the view so that it can be shown.
         */

        /**
         * Gets the column indexes
         */
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.Inventory._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.Inventory.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.Inventory.COLUMN_PRICE);
        int availabilityColumnIndex = cursor.getColumnIndex(InventoryContract.Inventory.COLUMN_QUANTITY);

        //Below here update the items in the view with info from the database
        //Product Name
        TextView productName = (TextView) view.findViewById(R.id.li_text_view_product_name);
        productName.setText(cursor.getString(nameColumnIndex));
        //Product Price
        TextView productPrice = (TextView) view.findViewById(R.id.li_text_view_product_price);
        productPrice.setText(cursor.getString(priceColumnIndex));
        //Product Availability
        TextView productAvailability = (TextView) view.findViewById(R.id.li_text_view_product_quantity);
        productAvailability.setText(cursor.getString(availabilityColumnIndex));
        //Set tag of the button to the ID of the database item!
        //This makes it easy to retrieve the item when the button is clicked
        //Url: https://jmsliu.com/2444/click-button-in-listview-and-get-item-position.html
        Button itemButton = (Button) view.findViewById(R.id.li_button_edit);
        itemButton.setTag(cursor.getString(idColumnIndex));
    }
    
}
