package dev.velaron.fennec;

import android.content.Context;

import dev.velaron.fennec.api.CaptchaProvider;
import dev.velaron.fennec.api.ICaptchaProvider;
import dev.velaron.fennec.api.impl.Networker;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.db.impl.AppStorages;
import dev.velaron.fennec.db.impl.LogsStorage;
import dev.velaron.fennec.db.interfaces.ILogsStorage;
import dev.velaron.fennec.db.interfaces.IStorages;
import dev.velaron.fennec.domain.IAttachmentsRepository;
import dev.velaron.fennec.domain.IBlacklistRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.domain.impl.AttachmentsRepository;
import dev.velaron.fennec.domain.impl.BlacklistRepository;
import dev.velaron.fennec.media.gif.AppGifPlayerFactory;
import dev.velaron.fennec.media.gif.IGifPlayerFactory;
import dev.velaron.fennec.media.voice.IVoicePlayerFactory;
import dev.velaron.fennec.media.voice.VoicePlayerFactory;
import dev.velaron.fennec.push.IDevideIdProvider;
import dev.velaron.fennec.push.IPushRegistrationResolver;
import dev.velaron.fennec.push.PushRegistrationResolver;
import dev.velaron.fennec.settings.IProxySettings;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.ProxySettingsImpl;
import dev.velaron.fennec.settings.SettingsImpl;
import dev.velaron.fennec.upload.IUploadManager;
import dev.velaron.fennec.upload.UploadManagerImpl;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static dev.velaron.fennec.util.Objects.isNull;

/**
 * Created by ruslan.kolbasa on 01.12.2016.
 * phoenix
 */
public class Injection {

    private static volatile ICaptchaProvider captchaProvider;

    private static IProxySettings proxySettings = new ProxySettingsImpl(provideApplicationContext());

    public static IProxySettings provideProxySettings(){
        return proxySettings;
    }

    public static IGifPlayerFactory provideGifPlayerFactory(){
        return new AppGifPlayerFactory(proxySettings, provideSettings().other());
    }

    private static volatile IPushRegistrationResolver resolver;

    public static IVoicePlayerFactory provideVoicePlayerFactory(){
        return new VoicePlayerFactory(provideApplicationContext(), provideProxySettings(), provideSettings().other());
    }

    public static IPushRegistrationResolver providePushRegistrationResolver(){
        if(isNull(resolver)){
            synchronized (Injection.class){
                if(isNull(resolver)){
                    final Context context = provideApplicationContext();
                    final IDevideIdProvider devideIdProvider = () -> Utils.getDiviceId(context);
                    resolver = new PushRegistrationResolver(devideIdProvider, provideSettings(), provideNetworkInterfaces());
                }
            }
        }

        return resolver;
    }

    private static volatile IUploadManager uploadManager;
    private static final Object UPLOADMANAGERLOCK = new Object();

    public static IUploadManager provideUploadManager(){
        if(uploadManager == null){
            synchronized (UPLOADMANAGERLOCK){
                if(uploadManager == null){
                    uploadManager = new UploadManagerImpl(App.getInstance(), provideNetworkInterfaces(),
                            provideStores(), provideAttachmentsRepository(), Repository.INSTANCE.getWalls());
                }
            }
        }

        return uploadManager;
    }

    public static ICaptchaProvider provideCaptchaProvider() {
        if(isNull(captchaProvider)){
            synchronized (Injection.class){
                if(isNull(captchaProvider)){
                    captchaProvider = new CaptchaProvider(provideApplicationContext(), provideMainThreadScheduler());
                }
            }
        }
        return captchaProvider;
    }

    private static volatile IAttachmentsRepository attachmentsRepository;

    public static IAttachmentsRepository provideAttachmentsRepository(){
        if(isNull(attachmentsRepository)){
            synchronized (Injection.class){
                if(isNull(attachmentsRepository)){
                    attachmentsRepository = new AttachmentsRepository(provideStores().attachments(), Repository.INSTANCE.getOwners());
                }
            }
        }

        return attachmentsRepository;
    }

    private static INetworker networkerInstance = new Networker(proxySettings);

    public static INetworker provideNetworkInterfaces(){
        return networkerInstance;
    }

    public static IStorages provideStores(){
        return AppStorages.getInstance(App.getInstance());
    }

    private static volatile IBlacklistRepository blacklistRepository;

    public static IBlacklistRepository provideBlacklistRepository() {
        if(isNull(blacklistRepository)){
            synchronized (Injection.class){
                if(isNull(blacklistRepository)){
                    blacklistRepository = new BlacklistRepository();
                }
            }
        }
        return blacklistRepository;
    }

    public static ISettings provideSettings(){
        return SettingsImpl.getInstance(App.getInstance());
    }

    private static volatile ILogsStorage logsStore;

    public static ILogsStorage provideLogsStore(){
        if(isNull(logsStore)){
            synchronized (Injection.class){
                if(isNull(logsStore)){
                    logsStore = new LogsStorage(provideApplicationContext());
                }
            }
        }
        return logsStore;
    }

    public static Scheduler provideMainThreadScheduler(){
        return AndroidSchedulers.mainThread();
    }

    public static Context provideApplicationContext() {
        return App.getInstance();
    }
}