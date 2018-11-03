package com.example.android.asinventory;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    //Set the content authority variable
    public static final String CONTENT_AUTHORITY = "com.example.android.asinventory";

    //We use the content authority string to create the base URI that all apps will use to reach
    //the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Paths, we'll just use Inventory because that's the only data set we'll need for this
    public static final String PATH_INVENTORY = "inventory";

    /**
     * Now we will create the inner class which defines the table contents of the inventory database
     */
    public static final class Inventory implements BaseColumns {
        //Setting the variables for the URI and CONTENT Types and Content ITEM Type
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INVENTORY).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //Setting the Table name Variable
        public static final String TABLE_NAME = "products";

        //Column Name
        public static final String _ID = "_id";
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";

        //Return the completed URI
        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildProductName(String productName) {
            return CONTENT_URI.buildUpon().appendPath(productName).build();
        }

        public static boolean isValidPrice(Double price) {
            if (price >= 0) {
                return true;
            }
            return false;
        }
    }
}
