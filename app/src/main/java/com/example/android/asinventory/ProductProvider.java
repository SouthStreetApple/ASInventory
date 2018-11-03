

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.asinventory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.asinventory.InventoryContract.Inventory;

public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the products(inventory) table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    /**
     * Database helper object
     */
    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        //Create our DbHelper, make sure to use the right context
        mDbHelper = new DbHelper(getContext()); //new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(Inventory.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = Inventory._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(Inventory.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        /**
         * These validation checks are SECONDARY, the primary checks are in the EditActivity
         * class file.  However if for some reason something goes wrong between there and here, we
         * should throw exceptions, because something is truly broken.
         */
        String name = values.getAsString(Inventory.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("The product requires a name.");
        }

        // Check that the price is valid
        Double price = values.getAsDouble(Inventory.COLUMN_PRICE);
        if (price == null || !Inventory.isValidPrice(price)) {
            throw new IllegalArgumentException("Product requires a valid price");
        }

        // Check that the Qty. is provided
        Integer quantity = values.getAsInteger(Inventory.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        //Check the manufacturer name
        String manufacturerName = values.getAsString(Inventory.COLUMN_SUPPLIER_NAME);
        if (manufacturerName == null) {
            throw new IllegalArgumentException("Manufacturer needs a name");
        }

        //Check the manufacturer phone number
        String manufacturerPhoneNumber = values.getAsString(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (manufacturerPhoneNumber == null) {
            throw new IllegalArgumentException("Manufacturer phone number needs a value");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(Inventory.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the product_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = Inventory._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the name is not null
        String name = values.getAsString(Inventory.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("The product requires a name.");
        }

        // Check that the price is valid
        Double price = values.getAsDouble(Inventory.COLUMN_PRICE);
        if (price == null || !Inventory.isValidPrice(price)) {
            throw new IllegalArgumentException("Product requires a valid price");
        }

        // Check that the Qty. is provided
        Integer quantity = values.getAsInteger(Inventory.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        //Check the manufacturer name
        String manufacturerName = values.getAsString(Inventory.COLUMN_SUPPLIER_NAME);
        if (manufacturerName == null) {
            throw new IllegalArgumentException("Manufacturer needs a name");
        }

        //Check the manufacturer phone number
        String manufacturerPhoneNumber = values.getAsString(Inventory.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (manufacturerPhoneNumber == null) {
            throw new IllegalArgumentException("Manufacturer phone number needs a value");
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(Inventory.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(Inventory.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = Inventory._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(Inventory.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return Inventory.CONTENT_TYPE;
            case PRODUCT_ID:
                return Inventory.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}