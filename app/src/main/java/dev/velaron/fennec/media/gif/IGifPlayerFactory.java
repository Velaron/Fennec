package dev.velaron.fennec.media.gif;

import androidx.annotation.NonNull;

/**
 * Created by admin on 13.08.2017.
 * phoenix
 */
public interface IGifPlayerFactory {
    IGifPlayer createGifPlayer(@NonNull String url);
}