package dev.velaron.fennec.fragment;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.SendAttachmentsActivity;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.domain.IAudioInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.fragment.base.BaseFragment;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.player.MusicPlaybackService;
import dev.velaron.fennec.player.ui.PlayPauseButton;
import dev.velaron.fennec.player.ui.RepeatButton;
import dev.velaron.fennec.player.ui.RepeatingImageButton;
import dev.velaron.fennec.player.ui.ShuffleButton;
import dev.velaron.fennec.player.util.MusicUtils;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.view.CircleCounterButton;
import io.reactivex.disposables.CompositeDisposable;

import static dev.velaron.fennec.player.util.MusicUtils.isPlaying;
import static dev.velaron.fennec.player.util.MusicUtils.mService;
import static dev.velaron.fennec.player.util.MusicUtils.observeServiceBinding;
import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

public class AudioPlayerFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {

    // Message to refresh the time
    private static final int REFRESH_TIME = 1;

    // Play and pause button
    private PlayPauseButton mPlayPauseButton;

    // Repeat button
    private RepeatButton mRepeatButton;

    // Shuffle button
    private ShuffleButton mShuffleButton;

    // Current time
    private TextView mCurrentTime;

    // Total time
    private TextView mTotalTime;

    // Progress
    private SeekBar mProgress;

    // VK Additional action
    private CircleCounterButton ivAdd;
    private CircleCounterButton ivTranslate;

    private TextView tvTitle;
    private TextView tvSubtitle;

    private ImageView ivCover;

    // Broadcast receiver
    private PlaybackStatus mPlaybackStatus;

    // Handler used to update the current time
    private TimeHandler mTimeHandler;

    private long mPosOverride = -1;

    private long mStartSeekPos = 0;

    private long mLastSeekEventTime;

    private boolean mIsPaused = false;

    private boolean mFromTouch = false;

    public static Bundle buildArgs(int accountId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        return bundle;
    }

    public static AudioPlayerFragment newInstance(int accountId) {
        return newInstance(buildArgs(accountId));
    }

    public static AudioPlayerFragment newInstance(Bundle args) {
        AudioPlayerFragment fragment = new AudioPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String[] mPlayerProgressStrings;

    private IAudioInteractor mAudioInteractor;

    private int mAccountId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        AssertUtils.requireNonNull(getArguments());

        mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        mAudioInteractor = InteractorFactory.createAudioInteractor();

        mTimeHandler = new TimeHandler(this);
        mPlaybackStatus = new PlaybackStatus(this);
        mPlayerProgressStrings = getResources().getStringArray(R.array.player_progress_state);

        appendDisposable(observeServiceBinding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(ignore -> onServiceBindEvent()));
    }

    private void onServiceBindEvent() {
        updatePlaybackControls();
        updateNowPlayingInfo();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.audio_player_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.eq).setVisible(isEqualizerAvailable());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eq:
                startEffectsPanel();
                return true;

            case R.id.playlist:
                PlaceFactory.getPlaylistPlace().tryOpenWith(requireActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutRes;
        if (Utils.isLandscape(requireActivity()) && !Utils.is600dp(requireActivity())) {
            layoutRes = R.layout.fragment_player_land;
        } else {
            layoutRes = R.layout.fragment_player_port_new;
        }

        View root = inflater.inflate(layoutRes, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mPlayPauseButton = root.findViewById(R.id.action_button_play);
        mShuffleButton = root.findViewById(R.id.action_button_shuffle);
        mRepeatButton = root.findViewById(R.id.action_button_repeat);

        RepeatingImageButton mPreviousButton = root.findViewById(R.id.action_button_previous);
        RepeatingImageButton mNextButton = root.findViewById(R.id.action_button_next);

        ivCover = root.findViewById(R.id.cover);

        mCurrentTime = root.findViewById(R.id.audio_player_current_time);
        mTotalTime = root.findViewById(R.id.audio_player_total_time);
        mProgress = root.findViewById(android.R.id.progress);
        tvTitle = root.findViewById(R.id.audio_player_title);
        tvSubtitle = root.findViewById(R.id.audio_player_subtitle);

        //to animate running text
        tvTitle.setSelected(true);
        tvSubtitle.setSelected(true);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.phoenix_player));
            actionBar.setSubtitle(null);
        }

        mPreviousButton.setRepeatListener(mRewindListener);
        mNextButton.setRepeatListener(mFastForwardListener);
        mProgress.setOnSeekBarChangeListener(this);

        ivAdd = root.findViewById(R.id.audio_add);
        ivAdd.setOnClickListener(v -> onAddButtonClick());

        CircleCounterButton ivShare = root.findViewById(R.id.audio_share);
        ivShare.setOnClickListener(v -> shareAudio());

//        if (isAudioStreaming()) {
//            broadcastAudio();
//        }

        resolveAddButton();

        return root;
    }

    private void onAudioBroadcastButtonClick() {
        ivTranslate.setActive(!ivTranslate.isActive());

        Settings.get()
                .other()
                .setAudioBroadcastActive(ivTranslate.isActive());

        if (isAudioStreaming()) {
            broadcastAudio();
        }
    }

    private boolean isAudioStreaming() {
        return Settings.get()
                .other()
                .isAudioBroadcastActive();
    }

    private void onAddButtonClick() {
        Audio audio = MusicUtils.getCurrentAudio();
        if (audio == null) {
            return;
        }

        if (audio.getOwnerId() == mAccountId) {
            if (!audio.isDeleted()) {
                delete(mAccountId, audio);
            } else {
                restore(mAccountId, audio);
            }
        } else {
            add(mAccountId, audio);
        }
    }

    private void add(int accountId, Audio audio) {
        appendDisposable(mAudioInteractor.add(accountId, audio, null, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAudioAdded, t -> {/*TODO*/}));
    }

    @SuppressWarnings("unused")
    private void onAudioAdded(Audio result) {
        safeToast(R.string.added);
        resolveAddButton();
    }

    private void delete(final int accoutnId, Audio audio) {
        final int id = audio.getId();
        final int ownerId = audio.getOwnerId();

        appendDisposable(mAudioInteractor.delete(accoutnId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onAudioDeletedOrRestored(id, ownerId, true), t -> {/*TODO*/}));
    }

    private void restore(final int accountId, Audio audio) {
        final int id = audio.getId();
        final int ownerId = audio.getOwnerId();

        appendDisposable(mAudioInteractor.restore(accountId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onAudioDeletedOrRestored(id, ownerId, false), t -> {/*TODO*/}));
    }

    private void onAudioDeletedOrRestored(int id, int ownerId, boolean deleted) {
        if (deleted) {
            safeToast(R.string.deleted);
        } else {
            safeToast(R.string.restored);
        }

        Audio current = MusicUtils.getCurrentAudio();

        if (nonNull(current) && current.getId() == id && current.getOwnerId() == ownerId) {
            current.setDeleted(deleted);
        }

        resolveAddButton();
    }

    private void safeToast(int res) {
        if (isAdded()) {
            Toast.makeText(requireActivity(), res, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProgressChanged(final SeekBar bar, final int progress, final boolean fromuser) {
        if (!fromuser || mService == null) {
            return;
        }

        final long now = SystemClock.elapsedRealtime();
        if (now - mLastSeekEventTime > 250) {
            mLastSeekEventTime = now;

            refreshCurrentTime();

            if (!mFromTouch) {
                // refreshCurrentTime();
                mPosOverride = -1;
            }
        }

        mPosOverride = MusicUtils.duration() * progress / 1000;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartTrackingTouch(final SeekBar bar) {
        mLastSeekEventTime = 0;
        mFromTouch = true;

        mCurrentTime.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStopTrackingTouch(final SeekBar bar) {
        if (mPosOverride != -1) {
            MusicUtils.seek(mPosOverride);
            mPosOverride = -1;
        }

        mFromTouch = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        // Set the playback drawables
        updatePlaybackControls();
        // Current info
        updateNowPlayingInfo();

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter();
        // Play and pause changes
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
        // Shuffle and repeat changes
        filter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicPlaybackService.REPEATMODE_CHANGED);
        // Track changes
        filter.addAction(MusicPlaybackService.META_CHANGED);
        // Player prepared
        filter.addAction(MusicPlaybackService.PREPARED);
        // Update a list, probably the playlist fragment's
        filter.addAction(MusicPlaybackService.REFRESH);

        requireActivity().registerReceiver(mPlaybackStatus, filter);
        // Refresh the current time
        final long next = refreshCurrentTime();
        queueNextRefresh(next);

        MusicUtils.notifyForegroundStateChanged(requireActivity(), isPlaying());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        MusicUtils.notifyForegroundStateChanged(requireActivity(), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        mIsPaused = false;
        mTimeHandler.removeMessages(REFRESH_TIME);
        mBroadcastDisposable.dispose();

        // Unregister the receiver
        try {
            requireActivity().unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable ignored) {
            //$FALL-THROUGH$
        }
    }

    /**
     * Sets the track name, album name, and album art.
     */
    private void updateNowPlayingInfo() {
        String artist = MusicUtils.getArtistName();
        String trackName = MusicUtils.getTrackName();
        String coverUrl = MusicUtils.getAlbumCoverBig();

        if (tvTitle != null) {
            tvTitle.setText(artist == null ? null : artist.trim());
        }

        if (tvSubtitle != null) {
            tvSubtitle.setText(trackName == null ? null : trackName.trim());
        }

        if (coverUrl != null) {
            ivCover.setScaleType(ImageView.ScaleType.FIT_START);
            PicassoInstance.with().load(coverUrl).into(ivCover);
        } else {
            ivCover.setScaleType(ImageView.ScaleType.CENTER);
            ivCover.setImageResource(R.drawable.itunes);
            ivCover.getDrawable().setTint(CurrentTheme.getColorOnSurface(requireContext()));
        }

        resolveAddButton();

        Audio current = MusicUtils.getCurrentAudio();

        //handle VK actions
        if (current != null && isAudioStreaming()) {
            broadcastAudio();
        }

        // Set the total time
        resolveTotalTime();
        // Update the current time
        queueNextRefresh(1);
    }


    private void resolveTotalTime() {
        if (!isAdded() || mTotalTime == null) {
            return;
        }

        if (MusicUtils.isInitialized()) {
            mTotalTime.setText(MusicUtils.makeTimeString(requireActivity(), MusicUtils.duration() / 1000));
        }
    }

    /**
     * Sets the correct drawable states for the playback controls.
     */
    private void updatePlaybackControls() {
        if (!isAdded()) {
            return;
        }

        // Set the play and pause image
        if (nonNull(mPlayPauseButton)) {
            mPlayPauseButton.updateState();
        }

        // Set the shuffle image
        if (nonNull(mShuffleButton)) {
            mShuffleButton.updateShuffleState();
        }

        // Set the repeat image
        if (nonNull(mRepeatButton)) {
            mRepeatButton.updateRepeatState();
        }
    }

    private static final int REQUEST_EQ = 139;

    private void startEffectsPanel() {
        try {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            effects.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().getPackageName());
            effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicUtils.getAudioSessionId());
            effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
            startActivityForResult(effects, REQUEST_EQ);
        } catch (final ActivityNotFoundException ignored) {
            Toast.makeText(requireActivity(), "No system equalizer found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEqualizerAvailable() {
        Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        PackageManager manager = requireActivity().getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        return infos.size() > 0;
    }

    private void shareAudio() {
        Audio current = MusicUtils.getCurrentAudio();
        if (current == null) {
            return;
        }

        SendAttachmentsActivity.startForSendAttachments(requireActivity(), mAccountId, current);
    }

    private void resolveAddButton() {
        if (!isAdded()) return;

        Audio currentAudio = MusicUtils.getCurrentAudio();
        //ivAdd.setVisibility(currentAudio == null ? View.INVISIBLE : View.VISIBLE);
        if (currentAudio == null) {
            return;
        }

        boolean myAudio = currentAudio.getOwnerId() == mAccountId;
        int icon = myAudio && !currentAudio.isDeleted() ? R.drawable.delete : R.drawable.plus;
        ivAdd.setIcon(icon);
    }

    private CompositeDisposable mBroadcastDisposable = new CompositeDisposable();

    private void broadcastAudio() {
        mBroadcastDisposable.clear();

        Audio currentAudio = MusicUtils.getCurrentAudio();

        if (currentAudio == null) {
            return;
        }

        final int accountId = mAccountId;
        final Collection<Integer> targetIds = Collections.singleton(accountId);
        final int id = currentAudio.getId();
        final int ownerId = currentAudio.getOwnerId();

        mBroadcastDisposable.add(mAudioInteractor.sendBroadcast(accountId, ownerId, id, targetIds)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {/*ignore*/}, t -> {/*ignore*/}));
    }

    /**
     * @param delay When to update
     */
    private void queueNextRefresh(final long delay) {
        if (!mIsPaused) {
            final Message message = mTimeHandler.obtainMessage(REFRESH_TIME);

            mTimeHandler.removeMessages(REFRESH_TIME);
            mTimeHandler.sendMessageDelayed(message, delay);
        }
    }

    private void resolveControlViews() {
        if (!isAdded() || mProgress == null) return;

        boolean preparing = MusicUtils.isPreparing();
        boolean initialized = MusicUtils.isInitialized();
        mProgress.setEnabled(!preparing && initialized);
        //mProgress.setIndeterminate(preparing);
    }

    /**
     * Used to scan backwards in time through the curren track
     *
     * @param repcnt The repeat count
     * @param delta  The long press duration
     */
    private void scanBackward(final int repcnt, long delta) {
        if (mService == null) {
            return;
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos - delta;
            if (newpos < 0) {
                // move to previous track
                MusicUtils.previous(requireActivity());
                final long duration = MusicUtils.duration();
                mStartSeekPos += duration;
                newpos += duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }

            refreshCurrentTime();
        }
    }

    /**
     * Used to scan forwards in time through the curren track
     *
     * @param repcnt The repeat count
     * @param delta  The long press duration
     */
    private void scanForward(final int repcnt, long delta) {
        if (mService == null) {
            return;
        }

        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }

            long newpos = mStartSeekPos + delta;
            final long duration = MusicUtils.duration();
            if (newpos >= duration) {
                // move to next track
                MusicUtils.next();
                mStartSeekPos -= duration; // is OK to go negative
                newpos -= duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }

            refreshCurrentTime();
        }
    }

    private void refreshCurrentTimeText(final long pos) {
        mCurrentTime.setText(MusicUtils.makeTimeString(requireActivity(), pos / 1000));
    }

    private long refreshCurrentTime() {
        //Logger.d("refreshTime", String.valueOf(mService == null));

        if (mService == null) {
            return 500;
        }

        try {
            final long pos = mPosOverride < 0 ? MusicUtils.position() : mPosOverride;
            final long duration = MusicUtils.duration();

            if (pos >= 0 && duration > 0) {
                refreshCurrentTimeText(pos);
                final int progress = (int) (1000 * pos / MusicUtils.duration());

                mProgress.setProgress(progress);

                int bufferProgress = (int) ((float) MusicUtils.bufferPercent() * 10F);
                mProgress.setSecondaryProgress(bufferProgress);

                if (mFromTouch) {
                    return 500;
                } else if (MusicUtils.isPlaying()) {
                    mCurrentTime.setVisibility(View.VISIBLE);
                } else {
                    // blink the counter
                    final int vis = mCurrentTime.getVisibility();
                    mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                    return 500;
                }
            } else {
                mCurrentTime.setText("--:--");
                mProgress.setProgress(0);

                int current = mTotalTime.getTag() == null ? 0 : (int) mTotalTime.getTag();
                int next = current == mPlayerProgressStrings.length - 1 ? 0 : current + 1;

                mTotalTime.setTag(next);
                mTotalTime.setText(mPlayerProgressStrings[next]);
                return 500;
            }

            // calculate the number of milliseconds until the next full second,
            // so
            // the counter can be updated at just the right time
            final long remaining = duration - pos % duration;

            // approximate how often we would need to refresh the slider to
            // move it smoothly
            int width = mProgress.getWidth();
            if (width == 0) {
                width = 320;
            }

            final long smoothrefreshtime = duration / width;
            if (smoothrefreshtime > remaining) {
                return remaining;
            }

            if (smoothrefreshtime < 20) {
                return 20;
            }

            return smoothrefreshtime;
        } catch (final Exception ignored) {
        }

        return 500;
    }

    /**
     * Used to scan backwards through the track
     */
    private final RepeatingImageButton.RepeatListener mRewindListener = new RepeatingImageButton.RepeatListener() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onRepeat(final View v, final long howlong, final int repcnt) {
            scanBackward(repcnt, howlong);
        }
    };

    /**
     * Used to scan ahead through the track
     */
    private final RepeatingImageButton.RepeatListener mFastForwardListener = new RepeatingImageButton.RepeatListener() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onRepeat(final View v, final long howlong, final int repcnt) {
            scanForward(repcnt, howlong);
        }
    };

    /**
     * Used to update the current time string
     */
    private static final class TimeHandler extends Handler {

        private final WeakReference<AudioPlayerFragment> mAudioPlayer;

        /**
         * Constructor of <code>TimeHandler</code>
         */
        TimeHandler(final AudioPlayerFragment player) {
            mAudioPlayer = new WeakReference<>(player);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case REFRESH_TIME:
                    final long next = mAudioPlayer.get().refreshCurrentTime();
                    mAudioPlayer.get().queueNextRefresh(next);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Used to monitor the state of playback
     */
    private static final class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<AudioPlayerFragment> mReference;

        /**
         * Constructor of <code>PlaybackStatus</code>
         */
        public PlaybackStatus(final AudioPlayerFragment activity) {
            mReference = new WeakReference<>(activity);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            AudioPlayerFragment fragment = mReference.get();
            if (isNull(fragment) || isNull(action)) return;

            switch (action) {
                case MusicPlaybackService.META_CHANGED:
                    // Current info
                    fragment.updateNowPlayingInfo();
                    fragment.resolveControlViews();
                    break;
                case MusicPlaybackService.PLAYSTATE_CHANGED:
                    // Set the play and pause image
                    fragment.mPlayPauseButton.updateState();
                    fragment.resolveTotalTime();
                    fragment.resolveControlViews();
                    break;
                case MusicPlaybackService.REPEATMODE_CHANGED:
                case MusicPlaybackService.SHUFFLEMODE_CHANGED:
                    // Set the repeat image
                    fragment.mRepeatButton.updateRepeatState();
                    // Set the shuffle image
                    fragment.mShuffleButton.updateShuffleState();
                    break;
                case MusicPlaybackService.PREPARED:
                    fragment.updateNowPlayingInfo();
                    fragment.resolveControlViews();
                    break;
            }
        }
    }
}