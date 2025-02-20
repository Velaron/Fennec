package dev.velaron.fennec.media.gif;

import static dev.velaron.fennec.util.Objects.nonNull;

import android.view.SurfaceHolder;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.util.Util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.App;
import dev.velaron.fennec.api.ProxyUtil;
import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.model.VideoSize;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Logger;
import okhttp3.OkHttpClient;

/**
 * Created by admin on 13.08.2017.
 * phoenix
 */
public class ExoGifPlayer implements IGifPlayer {

    private int status;

    private final String url;

    private VideoSize size;

    private SimpleExoPlayer internalPlayer;

    private final ProxyConfig proxyConfig;

    public ExoGifPlayer(String url, ProxyConfig proxyConfig) {
        this.url = url;
        this.proxyConfig = proxyConfig;
        this.status = IStatus.INIT;
    }

    @Override
    public VideoSize getVideoSize() {
        return size;
    }

    @Override
    public void play() {
        if(supposedToBePlaying) return;

        supposedToBePlaying = true;

        switch (status) {
            case IStatus.PREPARED:
                AssertUtils.requireNonNull(this.internalPlayer);
                startPlayer(this.internalPlayer);
                break;
            case IStatus.INIT:
                preparePlayer();
                break;
            case IStatus.PREPARING:
                //do nothing
                break;
        }
    }

    private void preparePlayer() {
        this.setStatus(IStatus.PREPARING);

        internalPlayer = new SimpleExoPlayer.Builder(App.getInstance()).build();

        // DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        // DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        // DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(App.getInstance(), Util.getUserAgent(App.getInstance(), "exoplayer2example"), bandwidthMeterA);

//        Proxy proxy = null;
//        if (nonNull(proxyConfig)) {
//            proxy = new Proxy(Proxy.Type.HTTP, ProxyUtil.obtainAddress(proxyConfig));
//
//            if(proxyConfig.isAuthEnabled()){
//                Authenticator authenticator = new Authenticator() {
//                    public PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(proxyConfig.getUser(), proxyConfig.getPass().toCharArray());
//                    }
//                };
//
//                Authenticator.setDefault(authenticator);
//            } else {
//                Authenticator.setDefault(null);
//            }
//        }

        String userAgent = Util.getUserAgent(App.getInstance(), "phoenix-gif-exo-player");
        OkHttpDataSource.Factory factory = new OkHttpDataSource.Factory(new OkHttpClient.Builder().build()).setUserAgent(userAgent);

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(new MediaItem.Builder().setUri(url).build());
        internalPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        internalPlayer.addListener(Listener);
        internalPlayer.setPlayWhenReady(true);
        internalPlayer.setMediaSource(mediaSource);
        internalPlayer.prepare();
    }

    private void onInternalPlayerStateChanged(int state){
        if(state == Player.STATE_READY){
            setStatus(IStatus.PREPARED);
        }
    }

    private final Player.Listener Listener = new Player.Listener() {
        @Override
        public void onVideoSizeChanged(com.google.android.exoplayer2.video.VideoSize videoSize) {
            size = new VideoSize(videoSize.width, videoSize.height);
            ExoGifPlayer.this.onVideoSizeChanged();
        }

        @Override
        public void onRenderedFirstFrame() {

        }

        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            Logger.d("PhoenixExo", "onPlayerStateChanged, state: " + playbackState);
            onInternalPlayerStateChanged(playbackState);
        }
    };

    private void onVideoSizeChanged(){
        for(IVideoSizeChangeListener listener : videoSizeChangeListeners){
            listener.onVideoSizeChanged(this, this.size);
        }
    }

    private boolean supposedToBePlaying;

    private static void pausePlayer(SimpleExoPlayer internalPlayer){
        internalPlayer.setPlayWhenReady(false);
        internalPlayer.getPlaybackState();
    }
    private static void startPlayer(SimpleExoPlayer internalPlayer){
        internalPlayer.setPlayWhenReady(true);
        internalPlayer.getPlaybackState();
    }

    @Override
    public void pause() {
        if(!supposedToBePlaying) return;

        supposedToBePlaying = false;

        if (nonNull(internalPlayer)) {
            try {
                pausePlayer(this.internalPlayer);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (nonNull(internalPlayer)) {
            internalPlayer.setVideoSurfaceHolder(holder);
        }
    }

    @Override
    public void release() {
        if (nonNull(internalPlayer)) {
            try {
                internalPlayer.release();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void setStatus(int newStatus){
        final int oldStatus = this.status;

        if(this.status == newStatus){
            return;
        }

        this.status = newStatus;
        for(IStatusChangeListener listener : statusChangeListeners){
            listener.onPlayerStatusChange(this, oldStatus, newStatus);
        }
    }

    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);
    private final List<IStatusChangeListener> statusChangeListeners = new ArrayList<>(1);

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.add(listener);
    }

    @Override
    public void addStatusChangeListener(IStatusChangeListener listener) {
        this.statusChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.remove(listener);
    }

    @Override
    public void removeStatusChangeListener(IStatusChangeListener listener) {
        this.statusChangeListeners.remove(listener);
    }

    @Override
    public int getPlayerStatus() {
        return status;
    }
}