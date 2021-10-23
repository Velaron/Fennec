package dev.velaron.fennec.api.impl;

import dev.velaron.fennec.api.IOtherVkRetrofitProvider;
import dev.velaron.fennec.api.IUploadRetrofitProvider;
import dev.velaron.fennec.api.IVkRetrofitProvider;
import dev.velaron.fennec.api.OtherVkRetrofitProvider;
import dev.velaron.fennec.api.UploadRetrofitProvider;
import dev.velaron.fennec.api.VkMethodHttpClientFactory;
import dev.velaron.fennec.api.VkRetrofitProvider;
import dev.velaron.fennec.api.interfaces.IAccountApis;
import dev.velaron.fennec.api.interfaces.IAuthApi;
import dev.velaron.fennec.api.interfaces.ILongpollApi;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.interfaces.IUploadApi;
import dev.velaron.fennec.api.services.IAuthService;
import dev.velaron.fennec.settings.IProxySettings;

/**
 * Created by ruslan.kolbasa on 30.12.2016.
 * phoenix
 */
public class Networker implements INetworker {

    private final IOtherVkRetrofitProvider otherVkRetrofitProvider;
    private final IVkRetrofitProvider vkRetrofitProvider;
    private final IUploadRetrofitProvider uploadRetrofitProvider;

    public Networker(IProxySettings settings) {
        this.otherVkRetrofitProvider = new OtherVkRetrofitProvider(settings);
        this.vkRetrofitProvider = new VkRetrofitProvider(settings, new VkMethodHttpClientFactory());
        this.uploadRetrofitProvider = new UploadRetrofitProvider(settings);
    }

    @Override
    public IAccountApis vkDefault(int accountId) {
        return VkApies.get(accountId, vkRetrofitProvider);
    }

    @Override
    public IAccountApis vkManual(int accountId, String accessToken) {
        return VkApies.create(accountId, accessToken, vkRetrofitProvider);
    }

    @Override
    public IAuthApi vkDirectAuth() {
        return new AuthApi(() -> otherVkRetrofitProvider.provideAuthRetrofit().map(wrapper -> wrapper.create(IAuthService.class)));
    }

    @Override
    public ILongpollApi longpoll() {
        return new LongpollApi(otherVkRetrofitProvider);
    }

    @Override
    public IUploadApi uploads() {
        return new UploadApi(uploadRetrofitProvider);
    }
}