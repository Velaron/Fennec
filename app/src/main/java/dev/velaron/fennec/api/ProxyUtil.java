package dev.velaron.fennec.api;

import static dev.velaron.fennec.util.Objects.nonNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.InetSocketAddress;
import java.net.Proxy;

import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.util.ValidationUtil;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class ProxyUtil {

    public static InetSocketAddress obtainAddress(@NonNull ProxyConfig config){
        if(ValidationUtil.isValidIpAddress(config.getAddress())){
            return new InetSocketAddress(config.getAddress(), config.getPort());
        } else {
            return InetSocketAddress.createUnresolved(config.getAddress(), config.getPort());
        }
    }

    public static void applyProxyConfig(OkHttpClient.Builder builder, @Nullable ProxyConfig config){
        if (nonNull(config)) {
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, obtainAddress(config));

            builder.proxy(proxy);

            if (config.isAuthEnabled()) {
                Authenticator authenticator = (route, response) -> {
                    String credential = Credentials.basic(config.getUser(), config.getPass());
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                };

                builder.proxyAuthenticator(authenticator);
            }
        }
    }
}