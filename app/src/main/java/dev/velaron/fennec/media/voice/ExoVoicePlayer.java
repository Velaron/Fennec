package dev.velaron.fennec.media.voice;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.util.Util;

import dev.velaron.fennec.App;
import dev.velaron.fennec.media.exo.ExoUtil;
import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.model.VoiceMessage;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Optional;
import okhttp3.OkHttpClient;

/**
 * Created by r.kolbasa on 28.11.2017.
 * Phoenix-for-VK
 */
public class ExoVoicePlayer implements IVoicePlayer {

    private final Context app;
    private final ProxyConfig proxyConfig;

    public ExoVoicePlayer(Context context, ProxyConfig config) {
        this.app = context.getApplicationContext();
        this.proxyConfig = config;
        this.status = STATUS_NO_PLAYBACK;
    }

    private SimpleExoPlayer exoPlayer;

    private int status;

    private AudioEntry playingEntry;

    @Override
    public boolean toggle(int id, VoiceMessage audio) {
        if (nonNull(playingEntry) && playingEntry.getId() == id) {
            setSupposedToBePlaying(!isSupposedToPlay());
            return false;
        }

        release();

        playingEntry = new AudioEntry(id, audio);
        supposedToBePlaying = true;

        preparePlayer();
        return true;
    }

    private void setStatus(int status) {
        if (this.status != status) {
            this.status = status;

            if (nonNull(statusListener)) {
                statusListener.onPlayerStatusChange(status);
            }
        }
    }

    private void preparePlayer() {
        setStatus(STATUS_PREPARING);

        exoPlayer = new SimpleExoPlayer.Builder(App.getInstance()).build();

        // DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        // DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        // DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(App.getInstance(), Util.getUserAgent(App.getInstance(), "exoplayer2example"), bandwidthMeterA);

//        Proxy proxy = null;
//        if (nonNull(proxyConfig)) {
//            proxy = new Proxy(Proxy.Type.HTTP, ProxyUtil.obtainAddress(proxyConfig));
//            if (proxyConfig.isAuthEnabled()) {
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

        String userAgent = Util.getUserAgent(app, "Phoenix-for-VK");
        OkHttpDataSource.Factory factory = new OkHttpDataSource.Factory(new OkHttpClient.Builder().build()).setUserAgent(userAgent);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:

        String url = playingEntry.getAudio().getLinkMp3();

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(new MediaItem.Builder().setUri(url).build());
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int playbackState) {
                onInternalPlayerStateChanged(playbackState);
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                onExoPlayerException(error);
            }
        });

        exoPlayer.setPlayWhenReady(supposedToBePlaying);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
    }

    private void onExoPlayerException(PlaybackException e){
        if(nonNull(errorListener)){
            errorListener.onPlayError(new PrepareException(e));
        }
    }

    private void onInternalPlayerStateChanged(int state) {
        Logger.d("ExoVoicePlayer", "onInternalPlayerStateChanged, state: " + state);

        switch (state){
            case Player.STATE_READY:
                setStatus(STATUS_PREPARED);
                break;
            case Player.STATE_ENDED:
                setSupposedToBePlaying(false);
                exoPlayer.seekTo(0);
                break;
        }
    }

    private void setSupposedToBePlaying(boolean supposedToBePlaying) {
        this.supposedToBePlaying = supposedToBePlaying;

        if(supposedToBePlaying){
            ExoUtil.startPlayer(exoPlayer);
        } else {
            ExoUtil.pausePlayer(exoPlayer);
        }
    }

    private boolean supposedToBePlaying;

    @Override
    public float getProgress() {
        if (Objects.isNull(exoPlayer)) {
            return 0f;
        }

        if (status != STATUS_PREPARED) {
            return 0f;
        }

        //long duration = playingEntry.getAudio().getDuration() * 1000;
        long duration = exoPlayer.getDuration();
        long position = exoPlayer.getCurrentPosition();
        return (float) position / (float) duration;
    }

    private IPlayerStatusListener statusListener;
    private IErrorListener errorListener;

    @Override
    public void setCallback(@Nullable IPlayerStatusListener listener) {
        this.statusListener = listener;
    }

    @Override
    public void setErrorListener(@Nullable IErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public Optional<Integer> getPlayingVoiceId() {
        return isNull(playingEntry) ? Optional.empty() : Optional.wrap(playingEntry.getId());
    }

    @Override
    public boolean isSupposedToPlay() {
        return supposedToBePlaying;
    }

    @Override
    public void stop() {
        if (nonNull(exoPlayer)) {
            exoPlayer.stop();
        }
    }

    @Override
    public void release() {
        if (nonNull(exoPlayer)) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }
}