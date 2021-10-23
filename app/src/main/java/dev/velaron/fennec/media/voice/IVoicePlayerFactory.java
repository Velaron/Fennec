package dev.velaron.fennec.media.voice;

import androidx.annotation.NonNull;

/**
 * Created by r.kolbasa on 27.11.2017.
 * Phoenix-for-VK
 */
public interface IVoicePlayerFactory {
    @NonNull
    IVoicePlayer createPlayer();
}