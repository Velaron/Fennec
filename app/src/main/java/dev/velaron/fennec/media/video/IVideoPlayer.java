package dev.velaron.fennec.media.video;

import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import dev.velaron.fennec.model.VideoSize;

/**
 * Created by Ruslan Kolbasa on 14.08.2017.
 * phoenix
 */
public interface IVideoPlayer {
    void play();

    void pause();

    void release();

    int getDuration();

    int getCurrentPosition();

    void seekTo(int position);

    boolean isPlaying();

    int getBufferPercentage();

    void setSurfaceHolder(SurfaceHolder holder);

    interface IVideoSizeChangeListener {
        void onVideoSizeChanged(@NonNull IVideoPlayer player, VideoSize size);
    }

    void addVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void removeVideoSizeChangeListener(IVideoSizeChangeListener listener);
}