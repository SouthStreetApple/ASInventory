package com.example.android.asinventory;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class will hold our data for each product.
 */

public class product implements Parcelable {
    Integer ID;
    String productName;
    Double price;
    Integer quantity;
    String supplierName;
    String supplierPhoneNumber;

    public void setProduct(Integer id, String product_name,Double product_price, Integer product_quantity,String supplier_name,String supplier_phone_number){
        ID =  id;
        productName = product_name;
        price = product_price;
        quantity = product_quantity;
        supplierName = supplier_name;
        supplierPhoneNumber = supplier_phone_number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.ID);
        dest.writeString(this.productName);
        dest.writeValue(this.price);
        dest.writeValue(this.quantity);
        dest.writeString(this.supplierName);
        dest.writeString(this.supplierPhoneNumber);
    }

    public product() {
    }

    protected product(Parcel in) {
        this.ID = (Integer) in.readValue(Integer.class.getClassLoader());
        this.productName = in.readString();
        this.price = (Double) in.readValue(Double.class.getClassLoader());
        this.quantity = (Integer) in.readValue(Integer.class.getClassLoader());
        this.supplierName = in.readString();
        this.supplierPhoneNumber = in.readString();
    }

    public static final Parcelable.Creator<product> CREATOR = new Parcelable.Creator<product>() {
        @Override
        public product createFromParcel(Parcel source) {
            return new product(source);
        }

        @Override
        public product[] newArray(int size) {
            return new product[size];
        }
    };
}
