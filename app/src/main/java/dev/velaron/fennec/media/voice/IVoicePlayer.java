package dev.velaron.fennec.media.voice;

import androidx.annotation.Nullable;
import dev.velaron.fennec.model.VoiceMessage;
import dev.velaron.fennec.util.Optional;

/**
 * Created by r.kolbasa on 27.11.2017.
 * Phoenix-for-VK
 */
public interface IVoicePlayer {

    boolean toggle(int id, VoiceMessage audio) throws PrepareException;

    float getProgress();

    void setCallback(@Nullable IPlayerStatusListener listener);

    void setErrorListener(@Nullable IErrorListener errorListener);

    Optional<Integer> getPlayingVoiceId();

    boolean isSupposedToPlay();

    void stop();

    void release();

    int STATUS_NO_PLAYBACK = 0;
    int STATUS_PREPARING = 1;
    int STATUS_PREPARED = 2;

    interface IPlayerStatusListener {
        void onPlayerStatusChange(int status);
    }

    interface IErrorListener {
        void onPlayError(Throwable t);
    }
}