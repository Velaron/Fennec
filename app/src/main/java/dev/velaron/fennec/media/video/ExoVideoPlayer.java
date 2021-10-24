package dev.velaron.fennec.media.video;

import static dev.velaron.fennec.util.Objects.nonNull;

import android.content.Context;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.util.Util;

import java.lang.ref.WeakReference;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.App;
import dev.velaron.fennec.api.ProxyUtil;
import dev.velaron.fennec.media.exo.ExoUtil;
import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.model.VideoSize;
import okhttp3.OkHttpClient;

/**
 * Created by Ruslan Kolbasa on 14.08.2017.
 * phoenix
 */
public class ExoVideoPlayer implements IVideoPlayer {

    private final SimpleExoPlayer player;

    private final MediaSource source;

    private final OnVideoSizeChangedListener onVideoSizeChangedListener = new OnVideoSizeChangedListener(this);

    public ExoVideoPlayer(Context context, String url, ProxyConfig config) {
        this.player = createPlayer(context);
        this.player.addVideoListener(onVideoSizeChangedListener);
        this.source = createMediaSource(context, url, config);
    }

    private static MediaSource createMediaSource(Context context, String url, ProxyConfig proxyConfig) {
        Proxy proxy = null;
        if (nonNull(proxyConfig)) {
            proxy = new Proxy(Proxy.Type.HTTP, ProxyUtil.obtainAddress(proxyConfig));

            if (proxyConfig.isAuthEnabled()) {
                Authenticator authenticator = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyConfig.getUser(), proxyConfig.getPass().toCharArray());
                    }
                };

                Authenticator.setDefault(authenticator);
            } else {
                Authenticator.setDefault(null);
            }
        }

        String userAgent = Util.getUserAgent(context.getApplicationContext(), "phoenix-video-exo-player");
        OkHttpDataSource.Factory factory = new OkHttpDataSource.Factory(new OkHttpClient.Builder().build()).setUserAgent(userAgent);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:
        return new ProgressiveMediaSource.Factory(factory).createMediaSource(new MediaItem.Builder().setUri(url).build());
    }

    private static SimpleExoPlayer createPlayer(Context context) {
        return new SimpleExoPlayer.Builder(App.getInstance()).build();
    }

    private boolean supposedToBePlaying;

    private boolean prepareCalled;

    @Override
    public void play() {
        if (supposedToBePlaying) {
            return;
        }

        supposedToBePlaying = true;

        if (!prepareCalled) {
            player.prepare(source);
            prepareCalled = true;
        }

        ExoUtil.startPlayer(player);
    }

    @Override
    public void pause() {
        if(!supposedToBePlaying){
            return;
        }

        supposedToBePlaying = false;
        ExoUtil.pausePlayer(player);
    }

    @Override
    public void release() {
        player.removeVideoListener(onVideoSizeChangedListener);
        player.release();
    }

    @Override
    public int getDuration() {
        return (int) player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public boolean isPlaying() {
        return supposedToBePlaying;
    }

    @Override
    public int getBufferPercentage() {
        return player.getBufferedPercentage();
    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        player.setVideoSurfaceHolder(holder);
    }

    private static final class OnVideoSizeChangedListener implements Player.Listener {

        final WeakReference<ExoVideoPlayer> ref;

        private OnVideoSizeChangedListener(ExoVideoPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void onVideoSizeChanged(com.google.android.exoplayer2.video.VideoSize videoSize) {
            ExoVideoPlayer player = ref.get();
            if (player != null) {
                player.onVideoSizeChanged(videoSize.width, videoSize.height);
            }
        }

        @Override
        public void onRenderedFirstFrame() {

        }
    }

    private void onVideoSizeChanged(int w, int h) {
        for(IVideoSizeChangeListener listener : videoSizeChangeListeners){
            listener.onVideoSizeChanged(this, new VideoSize(w, h));
        }
    }

    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.remove(listener);
    }
}