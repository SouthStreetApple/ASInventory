package com.example.android.asinventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.asinventory.InventoryContract.Inventory;

public class DbHelper extends SQLiteOpenHelper{
    /**
     * This is the class that will help us read and write to the database we are using.
     */
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ASInventory.db";

    public DbHelper(Context context){
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + Inventory.TABLE_NAME
                + " (" + Inventory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Inventory.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + Inventory.COLUMN_PRICE + " NUMERIC NOT NULL, "
                + Inventory.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + Inventory.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + Inventory.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //Deletes the table on upgrade, which is fine for now.
        String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Inventory.TABLE_NAME;
        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion,int newVersion){
        onUpgrade(db,oldVersion,newVersion);
    }

}
