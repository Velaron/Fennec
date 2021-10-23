package dev.velaron.fennec;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.service.ErrorLocalizer;
import dev.velaron.fennec.service.KeepLongpollService;
import dev.velaron.fennec.settings.Settings;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static dev.velaron.fennec.longpoll.NotificationHelper.tryCancelNotificationForPeer;
import static dev.velaron.fennec.util.RxUtils.ignore;

public class App extends Application {

    private static App sInstanse;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        sInstanse = this;
        AppCompatDelegate.setDefaultNightMode(Settings.get().ui().getNightMode());

        super.onCreate();

        PicassoInstance.init(this, Injection.provideProxySettings());

        if (Settings.get().other().isKeepLongpoll()) {
            KeepLongpollService.start(this);
        }

        compositeDisposable.add(Repository.INSTANCE.getMessages()
                .observePeerUpdates()
                .flatMap(Flowable::fromIterable)
                .subscribe(update -> {
                    if (update.getReadIn() != null) {
                        tryCancelNotificationForPeer(App.this, update.getAccountId(), update.getPeerId());
                    }
                }, ignore()));

        compositeDisposable.add(Repository.INSTANCE.getMessages()
                .observeSentMessages()
                .subscribe(sentMsg -> tryCancelNotificationForPeer(App.this, sentMsg.getAccountId(), sentMsg.getPeerId()), ignore()));

        compositeDisposable.add(Repository.INSTANCE.getMessages()
                .observeMessagesSendErrors()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(throwable -> Toast.makeText(App.this, ErrorLocalizer.localizeThrowable(App.this, throwable), Toast.LENGTH_LONG).show(), ignore()));
    }

    @NonNull
    public static App getInstance() {
        if (sInstanse == null) {
            throw new IllegalStateException("App instance is null!!! WTF???");
        }

        return sInstanse;
    }
}