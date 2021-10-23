package dev.velaron.fennec.media.voice;

import android.content.Context;

import androidx.annotation.NonNull;
import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.settings.IProxySettings;
import dev.velaron.fennec.settings.ISettings;

import static dev.velaron.fennec.util.Objects.isNull;

/**
 * Created by r.kolbasa on 27.11.2017.
 * Phoenix-for-VK
 */
public class VoicePlayerFactory implements IVoicePlayerFactory {

    private final Context app;
    private final IProxySettings proxySettings;
    private final ISettings.IOtherSettings otherSettings;

    public VoicePlayerFactory(Context context, IProxySettings proxySettings, ISettings.IOtherSettings otherSettings) {
        this.app = context.getApplicationContext();
        this.proxySettings = proxySettings;
        this.otherSettings = otherSettings;
    }

    @NonNull
    @Override
    public IVoicePlayer createPlayer() {
        ProxyConfig config = proxySettings.getActiveProxy();

        if (isNull(config) && !otherSettings.isForceExoplayer()) {
            return new DefaultVoicePlayer();
        } else {
            return new ExoVoicePlayer(app, config);
        }
    }
}