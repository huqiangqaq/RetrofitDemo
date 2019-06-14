package com.zhpan.idea.net.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.zhpan.idea.cookie.CookieJarImpl;
import com.zhpan.idea.cookie.PersistentCookieStore;
import com.zhpan.idea.net.converter.GsonConverterFactory;
import com.zhpan.idea.net.https.SslContextFactory;
import com.zhpan.idea.net.interceptor.HttpCacheInterceptor;
import com.zhpan.idea.net.interceptor.HttpHeaderInterceptor;
import com.zhpan.idea.net.interceptor.LoggingInterceptor;
import com.zhpan.idea.utils.LogUtils;
import com.zhpan.idea.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created by zhpan on 2018/3/21.
 */

public class RetrofitUtils {
    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        File cacheFile = new File(Utils.getContext().getCacheDir(), "cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb

        return new OkHttpClient.Builder()
                .readTimeout(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(new LoggingInterceptor())
                .cookieJar(new CookieJarImpl(new PersistentCookieStore(Utils.getContext())))    //持久化管理你的Cookie
                .addInterceptor(new HttpHeaderInterceptor())
                .addNetworkInterceptor(new HttpCacheInterceptor())
                .connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))      //支持https,明文Http与比较新的Https
//                .sslSocketFactory(SslContextFactory.getSSLSocketFactoryForTwoWay())  // https认证 如果要使用https且为自定义证书 可以去掉这两行注释，并自行配制证书。
               // .hostnameVerifier(new SafeHostnameVerifier())
                .cache(cache);
    }

    public static Retrofit.Builder getRetrofitBuilder(String baseUrl) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();
        OkHttpClient okHttpClient = getOkHttpClientBuilder().build();
        return new Retrofit.Builder()
                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create(gson)) //用gson转换器过滤
                .addConverterFactory(buildGsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl);
    }

    /**
     * 构建GSON转换器
     * @return GsonConverterFactory
     */
    private static GsonConverterFactory buildGsonConverterFactory(){
        GsonBuilder builder = new GsonBuilder();
        builder.setLenient();

        // 注册类型转换适配器
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return null == json ? null : new Date(json.getAsLong());
            }
        });

        Gson gson = builder.create();
        return GsonConverterFactory.create(gson);
    }

    /**
     * 公共参数，有些接口某些参数是公共的，不可能一个个都去加
     * 参考链接  https://juejin.im/entry/5825300b2f301e005c47fac5
     */
    private Interceptor addQueryParamterInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (request.method().equals("GET")) {
                HttpUrl httpUrl = request.url().newBuilder()
                        .addQueryParameter("version", "xxx")
                        .addQueryParameter("device", "Android")
                        .addQueryParameter("timestamp", String.valueOf(System.currentTimeMillis()))
                        .build();
                request = request.newBuilder().url(httpUrl).build();
            } else if (request.method().equals("POST")) {
                if (request.body() instanceof FormBody) {
                    FormBody.Builder bodyBuilder = new FormBody.Builder();
                    FormBody formBody = (FormBody) request.body();

                    for (int i = 0; i < formBody.size(); i++) {
                        bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
                    }
                    formBody = bodyBuilder
                            .addEncoded("version", "xxx")
                            .addEncoded("device", "Android")
                            .addEncoded("timestamp", String.valueOf(System.currentTimeMillis()))
                            .build();

                    request = request.newBuilder().post(formBody).build();
                }
            }

            return chain.proceed(request);
        }

    };
}
