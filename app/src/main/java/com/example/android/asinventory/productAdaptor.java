package com.example.android.asinventory;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class productAdaptor extends ArrayAdapter<product> {

    //This is so we can grab the application context later
    private static Context appContext;

    public productAdaptor(Activity context, ArrayList<product> objects){
        super(context,0,objects);
        //Set he application context
        appContext = getContext().getApplicationContext();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        /**
         * This is where we setup our product view so that it contains the right data we then return
         * the view so that it can be shown.
         */

        /**
         * Gets the current product
         */
        product currentProduct = getItem(position);

        //Check to make sure the current view is null, if it is create it.
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);
        }

        //Below here update the items in the view with info from the database
        //Product Name
        TextView productName = (TextView) listItemView.findViewById(R.id.li_text_view_product_name);
        productName.setText(currentProduct.productName);
        //Product Price
        TextView productPrice = (TextView) listItemView.findViewById(R.id.li_text_view_product_price);
        productPrice.setText(currentProduct.price.toString());
        //Product Availability
        TextView productAvailability = (TextView) listItemView.findViewById(R.id.li_text_view_product_quantity);
        productAvailability.setText(appContext.getResources().getString(R.string.product_quantity,currentProduct.quantity));
        //end

        //Return the listViewItem
        return listItemView;
    }
    
}
