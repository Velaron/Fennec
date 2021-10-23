package dev.velaron.fennec.api.impl;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import dev.velaron.fennec.api.ApiException;
import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.model.Error;
import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.api.model.response.BaseResponse;
import io.reactivex.Single;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
class AbsApi {

    private final IServiceProvider retrofitProvider;
    private final int accountId;

    AbsApi(int accountId, IServiceProvider provider) {
        this.retrofitProvider = provider;
        this.accountId = accountId;
    }

    static <T> Function<BaseResponse<T>, T> extractResponseWithErrorHandling() {
        return response -> {
            if (nonNull(response.error)) {
                throw Exceptions.propagate(new ApiException(response.error));
            }

            return response.response;
        };
    }

    static <T> Function<BaseResponse<T>, BaseResponse<T>> handleExecuteErrors(String... expectedMethods) {
        if (expectedMethods.length == 0) {
            throw new IllegalArgumentException("No expected methods found");
        }

        return response -> {
            if (nonEmpty(response.executeErrors)) {
                for (Error error : response.executeErrors) {
                    for (String expectedMethod : expectedMethods) {
                        if (expectedMethod.equalsIgnoreCase(error.method)) {
                            throw Exceptions.propagate(new ApiException(error));
                        }
                    }
                }
            }

            return response;
        };
    }

    protected static <T> String join(Iterable<T> tokens, String delimiter, SimpleFunction<T, String> function) {
        if (isNull(tokens)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (T token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(function.apply(token));
        }

        return sb.toString();
    }

    protected static String join(Iterable<?> tokens, String delimiter) {
        if (isNull(tokens)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(token);
        }

        return sb.toString();
    }

    static String formatAttachmentToken(IAttachmentToken token) {
        return token.format();
    }

    static String toQuotes(String word) {
        if (word == null) {
            return null;
        }

        return "\"" + word + "\"";
    }

    <T> Single<T> provideService(Class<T> serviceClass, int... tokenTypes) {
        if (isNull(tokenTypes) || tokenTypes.length == 0) {
            tokenTypes = new int[]{TokenType.USER}; // user by default
        }

        return retrofitProvider.provideService(accountId, serviceClass, tokenTypes);
    }

    int getAccountId() {
        return accountId;
    }

    static Integer integerFromBoolean(Boolean value) {
        return isNull(value) ? null : (value ? 1 : 0);
    }

    interface SimpleFunction<F, S> {
        S apply(F orig);
    }
}