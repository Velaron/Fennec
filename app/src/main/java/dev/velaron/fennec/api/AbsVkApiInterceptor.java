package dev.velaron.fennec.api;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.isEmpty;

import android.os.SystemClock;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Random;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.api.model.Captcha;
import dev.velaron.fennec.api.model.Error;
import dev.velaron.fennec.api.model.response.VkReponse;
import dev.velaron.fennec.exception.UnauthorizedException;
import dev.velaron.fennec.service.ApiErrorCodes;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by admin on 21.12.2016.
 * phoenix
 */
abstract class AbsVkApiInterceptor implements Interceptor {

    private final String version;
    private final Gson gson;

    AbsVkApiInterceptor(String version, Gson gson) {
        this.version = version;
        this.gson = gson;
    }

    protected abstract String getToken();

    private static final Random RANDOM = new Random();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        String token = getToken();

        if (isEmpty(token)) {
            throw new UnauthorizedException("No authorization! Please, login and retry");
        }

        FormBody.Builder formBuiler = new FormBody.Builder()
                .add("v", version)
                .add("access_token", token);

        RequestBody body = original.body();

        if (body instanceof FormBody) {
            FormBody formBody = (FormBody) body;

            for (int i = 0; i < formBody.size(); i++) {
                String name = formBody.name(i);
                String value = formBody.value(i);
                formBuiler.add(name, value);
            }
        }

        Request request = original.newBuilder()
                .method("POST", formBuiler.build())
                .build();

        Response response;
        ResponseBody responseBody;
        String responseBodyString;

        while (true) {
            response = chain.proceed(request);
            responseBody = response.body();
            responseBodyString = responseBody.string();

            VkReponse vkReponse = gson.fromJson(responseBodyString, VkReponse.class);

            Error error = isNull(vkReponse) ? null : vkReponse.error;

            if (nonNull(error)) {
                switch (error.errorCode) {
                    case ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND:
                    case ApiErrorCodes.CAPTCHA_NEED:
                        // no logging
                        break;
                    default:
                        //FirebaseCrash.log("ApiError, method: " + error.method + ", code: " + error.errorCode + ", message: " + error.errorMsg);
                        break;
                }

                if (error.errorCode == ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND) {
                    synchronized (AbsVkApiInterceptor.class) {
                        int sleepMs = 1000 + RANDOM.nextInt(500);
                        SystemClock.sleep(sleepMs);
                    }

                    continue;
                }

                if (error.errorCode == ApiErrorCodes.CAPTCHA_NEED) {
                    Captcha captcha = new Captcha(error.captchaSid, error.captchaImg);

                    ICaptchaProvider provider = Injection.provideCaptchaProvider();
                    provider.requestCaptha(captcha.getSid(), captcha);

                    String code = null;

                    while (true) {
                        try {
                            code = provider.lookupCode(captcha.getSid());

                            if (nonNull(code)) {
                                break;
                            } else {
                                SystemClock.sleep(1000);
                            }
                        } catch (OutOfDateException e) {
                            break;
                        }
                    }

                    if (nonNull(code)) {
                        formBuiler.add("captcha_sid", captcha.getSid());
                        formBuiler.add("captcha_key", code);

                        request = original.newBuilder()
                                .method("POST", formBuiler.build())
                                .build();
                        continue;
                    }
                }
            }

            break;
        }

        Response.Builder builder = response.newBuilder()
                .body(ResponseBody.create(responseBody.contentType(), responseBodyString));

        return builder.build();
    }
}