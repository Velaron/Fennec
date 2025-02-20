package dev.velaron.fennec.api;

import static dev.velaron.fennec.util.Objects.nonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import dev.velaron.fennec.BuildConfig;
import dev.velaron.fennec.settings.IProxySettings;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Objects;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class UploadRetrofitProvider implements IUploadRetrofitProvider {

    private final IProxySettings proxySettings;

    public UploadRetrofitProvider(IProxySettings proxySettings) {
        this.proxySettings = proxySettings;
        this.proxySettings.observeActive()
                .subscribe(ignored -> onProxySettingsChanged());
    }

    private void onProxySettingsChanged(){
        synchronized (uploadRetrofitLock){
            if(nonNull(uploadRetrofitInstance)){
                uploadRetrofitInstance.cleanup();
                uploadRetrofitInstance = null;
            }
        }
    }

    private final Object uploadRetrofitLock = new Object();
    private volatile RetrofitWrapper uploadRetrofitInstance;

    @Override
    public Single<RetrofitWrapper> provideUploadRetrofit() {
        return Single.fromCallable(() -> {

            if (Objects.isNull(uploadRetrofitInstance)) {
                synchronized (uploadRetrofitLock) {
                    if (Objects.isNull(uploadRetrofitInstance)) {
                        uploadRetrofitInstance = RetrofitWrapper.wrap(createUploadRetrofit(), true);
                    }
                }
            }

            return uploadRetrofitInstance;
        });
    }

    private Retrofit createUploadRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);

        Gson gson = new GsonBuilder()
                .create();

        ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());

        return new Retrofit.Builder()
                .baseUrl(Settings.get().other().getApiDomain())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(builder.build())
                .build();
    }
}