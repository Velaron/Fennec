package dev.velaron.fennec.media.gif;

import androidx.annotation.NonNull;
import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.settings.IProxySettings;
import dev.velaron.fennec.settings.ISettings;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 13.08.2017.
 * phoenix
 */
public class AppGifPlayerFactory implements IGifPlayerFactory {

    private final IProxySettings proxySettings;
    private final ISettings.IOtherSettings otherSettings;

    public AppGifPlayerFactory(IProxySettings proxySettings, ISettings.IOtherSettings otherSettings) {
        this.proxySettings = proxySettings;
        this.otherSettings = otherSettings;
    }

    @Override
    public IGifPlayer createGifPlayer(@NonNull String url) {
        ProxyConfig config = proxySettings.getActiveProxy();

        if (nonNull(config) || otherSettings.isForceExoplayer()) {
            return new ExoGifPlayer(url, config);
        } else {
            return new DefaultGifPlayer(url);
        }
    }
}