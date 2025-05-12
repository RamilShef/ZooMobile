package com.example.zooproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

public interface ZooApiService {
    @GET("/api/section")
    Call<List<Section>> getAllSections();

    @GET("/api/section/{id}")
    Call<Section> getSectionById(@Path("id") int id);
}
