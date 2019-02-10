package com.example.lhw.app_1;

import com.google.gson.annotations.SerializedName;



public class ImageModel {

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("response")
    private String response;

    public String getResponse(){
        return response;
    }

}
