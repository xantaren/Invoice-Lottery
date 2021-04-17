package com.shashipage.invoicechecker;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.GET;

public class ApiResponse {
    //json轉換成object
    @SerializedName("data")
    @Expose
    public ArrayList<Datum> data = new ArrayList<Datum>();

}

class Datum {

    @SerializedName("published")
    @Expose
    public String published;
    @SerializedName("grand")
    @Expose
    public String grand;
    @SerializedName("first")
    @Expose
    public String first;
    @SerializedName("head")
    @Expose
    public String head;
    @SerializedName("bonus_six")
    @Expose
    public String bonusSix;

}

interface ApiService {
    @GET("exec")
    Call<ApiResponse> getData();
}

