package com.example.lhw.app_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("id_check.php") // 아이디 체크 하기
    Call<JsonObject> getId(@Query("id") String id);

    @GET("product_read.php") //상품 정보 가지고 오기(타이틀, 설명, 가격)
    Call<ArrayList<JsonObject>> getProduct();

    @GET("picture_read.php") //사진 가지고오기
    Call<ArrayList<JsonObject>> getPicture(@Query("product_idx") String product_idx);

    @GET("getprofile.php") //프로필 정보 가지고오기
    Call<JsonObject> getProfile(@Query("user_email") String user_email);

    @GET("getcommentprofile.php") //프로필 정보 가지고오기
    Call<JsonObject> getCommentProfile(@Query("user_idx") String user_idx);

    @GET("getuser.php")
    Call<JsonObject> getUserModel(@Query("user_email") String user_email);

    @GET("getProductUser.php") //등록한 상품의 유저 정보 가지고오기
    Call<JsonObject> getProductUser(@Query("user_idx") String user_idx);

    @GET("getproductcount.php") // 등록한 상품 개수 가지고오기 AND 구매한 상품 개수 가지고오기
    Call<JsonObject> getProductCount(@Query("user_email") String user_email);

    @GET("getlikeitcount.php") //찜 개수 가지고오기
    Call<JsonObject> getLikeitCount(@Query("user_email") String user_email);

    @GET("getlikeit.php") //찜 정보 가지고오기
    Call<JsonObject> getLikeit(@Query("user_email") String user_email, @Query("product_idx") String product_idx);

    @GET("getlikeitlist.php") //찜 ArrayList가지고오기
    Call<ArrayList<JsonObject>> getLikteitList(@Query("user_email") String user_email);

    @GET("getpurchaselist.php") //구매리스트 가지고오기
    Call<ArrayList<JsonObject>> getPurchaseList(@Query("user_email") String user_email);

    @GET("getsalelist.php") //자신이 올린 판매 리스트 가지고오기
    Call<ArrayList<JsonObject>> getSaleList(@Query("user_email") String user_email);

    @GET("getcomment.php") //댓글 리스트 가지고 오기
    Call<ArrayList<JsonObject>> getComment(@Query("user_email") String user_email, @Query("product_idx") String product_idx) ;

    @GET("getsearch.php") //검색어 리스트 가지고 오기
    Call<ArrayList<JsonObject>> getSearch(@Query("user_email") String user_email);

    @GET("getsearchproduct.php") //검색어 리스트 가지고 오기
    Call<ArrayList<JsonObject>> getSearchProduct(@Query("search_text") String search_text, @Query("search_check") String search_check);

    @GET("getchat.php") //이전 대화 내용 가지고 오기
    Call<ArrayList<JsonObject>> getChat(@Query("user_email") String user_email, @Query("product_user_email") String product_user_email);

    @GET("getchatroom.php") //이전 대화 내용 가지고 오기
    Call<ArrayList<JsonObject>> getChatRoom(@Query("user_email") String user_email);

    @GET("getauction.php") //이전 대화 내용 가지고 오기
    Call<ArrayList<JsonObject>> getAuction();








    @FormUrlEncoded
    @POST("login.php") //로그인
    Call<JsonObject> login(@Field("user_email") String user_email,
                           @Field("user_pass") String password);

    @POST("signin.php") //회원가입
    Call<JsonObject> setSignin(@Body UserModel user);

    @POST("product.php") //상품 정보 가지고 오기
    Call<JsonObject> setProduct(@Body ProductModel productModel);

    @FormUrlEncoded  //단일사진전송
    @POST("setprofile_image.php")
    Call<JsonObject> upload_profile(@Field("title") String title, @Field("image") String image, @Field("user_email") String user_email);

    @FormUrlEncoded
    @POST("updateuser.php") //유저 정보 업데이트
    Call<JsonObject> UpdateUser(@Field("user_email") String user_email, @Field("user_phone") String user_phone, @Field("user_name") String user_name);

    @FormUrlEncoded
    @POST("upload.php") //사진 + 상품 내용
    Call<ImageModel> upload(
            @Field("image_title[]") ArrayList<String> image_title,
            @Field("image_list[]") ArrayList<String> image_list,
            @Field("user_email") String user_email,
            @Field("product_title") String product_title,
            @Field("product_contents") String product_contents,
            @Field("product_price") String product_price);

    @FormUrlEncoded
    @POST("chatimg.php") //사진 + 상품 내용
    Call<JsonObject> chatimg(
            @Field("img_title")String img_title,
            @Field("img") String img,
            @Field("user_email") String user_email,
            @Field("product_user_email") String product_user_email,
            @Field("chat_date") String chat_Date);


    @FormUrlEncoded
    @POST("uploadmodify.php") //사진 + 상품 내용
    Call<ImageModel> uploadmodify(
            @Field("image_title[]") ArrayList<String> image_title,
            @Field("contenturl_list[]") ArrayList<String> contenturl_list,
            @Field("httpurl_list[]") ArrayList<String> httpurl_list,
            @Field("user_idx") String user_idx,
            @Field("product_idx") String product_idx,
            @Field("product_title") String product_title,
            @Field("product_contents") String product_contents,
            @Field("product_price") String product_price);

//    Call<ArrayList<JsonObject>> getListRepos();

    @FormUrlEncoded
    @POST("setlike.php") //찜 등록
    Call<JsonObject> SetLike(@Field("user_email") String user_email,
                           @Field("product_idx") String product_idx);

    @FormUrlEncoded
    @POST("delproduct.php") //상품삭제하기
    Call<JsonObject> DelProduct(@Field("user_email") String user_email,
                                @Field("product_idx") String product_idx);

    @FormUrlEncoded
    @POST("delcomment.php") //댓글삭제
    Call<JsonObject> DelComment(@Field("comment_idx") String comment_idx);


    @FormUrlEncoded
    @POST("setcomment.php") //댓글 작성
    Call<JsonObject> SetComment(@Field("user_email") String user_email,
                                @Field("product_idx") String product_idx,
                                @Field("comment_contents") String comment_contents,
                                @Field("comment_date") String comment_date);

    @FormUrlEncoded
    @POST("updateproduct.php") //상품판매완료
    Call<JsonObject> UpdateProduct(@Field("product_idx") String product_idx, @Field("user_idx") String user_idx);

    @FormUrlEncoded
    @POST("setsearch.php") //검색어 저장
    Call<JsonObject> setSearch(@Field("user_email") String user_email, @Field("search_text") String search_text, @Field("search_date") String search_date);

    @FormUrlEncoded
    @POST("delsearch.php") //상품삭제하기
    Call<JsonObject> delSearch(@Field("search_idx") String search_idx,
                               @Field("user_idx") String user_idx);

    @FormUrlEncoded
    @POST("setchat.php") //찜 등록
    Call<JsonObject> setChat(@Field("user_email") String user_email,
                             @Field("product_user_email") String product_user_email,
                             @Field("chat_date") String chat_date,
                             @Field("chat_contents") String chat_contents);

    @FormUrlEncoded
    @POST("setauction.php") //옥션 등록
    Call<JsonObject> setAuction(@Field("broadcaster_email") String broadcaster_email,
                                @Field("auction_title") String auction_title);


}

