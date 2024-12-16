package com.van1164.photoviewer.upload;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("api_root/Post/")
    Call<ResponseBody> uploadImage(
            @Header("Authorization") String token,
            @Part("title") RequestBody title,
            @Part("text") RequestBody text,
            @Part MultipartBody.Part image
    );
}
