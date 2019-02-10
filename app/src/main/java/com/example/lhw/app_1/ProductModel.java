package com.example.lhw.app_1;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductModel implements Serializable {
    String product_idx;
    String product_title;
    String product_price;
    String product_contents;
    String user_email;
    String user_idx;
    String product_check;
    String purchase_user;
    String listcount;
    String response;
    //ArrayList<PictureModel> picture_array = new ArrayList<>();

//    public class PictureModel{
//        public String picture_idx;
//        public String product_idx;
//        public String user_idx;
//        public String picture_name;
//        public String picture_path;
//
//        @Override
//        public String toString() {
//            return "picture_idx : " + picture_idx +
//                    "product_idx : " + product_idx +
//                    "user_idx : " + user_idx +
//                    "product_idx : " + product_idx +
//                    "picture_name : " + picture_name +
//                    "picture_path : " + picture_path;
//        }
//    }

    @Override
    public String toString() {
        return "product_idx : " + product_idx +
                "product_title : " + product_title +
                "product_price : " + product_price +
                "product_contents : " + product_contents +
                "user_email : " + user_email +
                "user_idx : " + user_idx;
    }


    public ProductModel() {

    }


    public ProductModel(String product_title, String product_price, String product_contents) {
        this.product_title = product_title;
        this.product_price = product_price;
        this.product_contents = product_contents;
    }

    public ProductModel(String product_title, String product_price, String product_contents, String user_email) {
        this.product_title = product_title;
        this.product_price = product_price;
        this.product_contents = product_contents;
        this.user_email = user_email;
    }

    public ProductModel(String product_idx, String product_title, String product_price, String product_contents, String user_idx) {
        this.product_idx = product_idx;
        this.product_title = product_title;
        this.product_price = product_price;
        this.product_contents = product_contents;
        this.user_idx = user_idx;
    }
}
