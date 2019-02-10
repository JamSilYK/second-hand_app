package com.example.lhw.app_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("user_idx")
    @Expose
    public String user_idx;

    @SerializedName("user_email")
    @Expose
    public String user_email;

    @SerializedName("user_pass")
    @Expose
    public String user_pass;

    @SerializedName("user_addr")
    @Expose
    public String user_addr;

    @SerializedName("user_name")
    @Expose
    public String user_name;

    @SerializedName("user_phone")
    @Expose
    public String user_phone;

    @SerializedName("user_detail_addr")
    @Expose
    public String user_detail_addr;

    public UserModel(){

    }

    public UserModel(String user_email, String user_pass, String user_addr, String user_name, String user_phone) {
        this.user_email = user_email;
        this.user_pass = user_pass;
        this.user_addr = user_addr;
        this.user_name = user_name;
        this.user_phone = user_phone;
    }

    public UserModel(String user_email, String user_pass, String user_addr, String user_name, String user_phone, String user_detail_addr) {
        this.user_email = user_email;
        this.user_pass = user_pass;
        this.user_addr = user_addr;
        this.user_name = user_name;
        this.user_phone = user_phone;
        this.user_detail_addr = user_detail_addr;
    }
}
