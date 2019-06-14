package com.example.huqiang.retrofitdemo;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by 83916 on 2018/1/1.
 */

public interface TestApiService {

    @GET("top25")
    Flowable<Movie> getTopMovies(@Query("start") int start, @Query("count") int count);
}
