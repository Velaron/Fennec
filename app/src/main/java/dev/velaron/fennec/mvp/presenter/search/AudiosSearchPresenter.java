package dev.velaron.fennec.mvp.presenter.search;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.domain.IAudioInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.fragment.search.criteria.AudioSearchCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.mvp.view.search.IAudioSearchView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.player.MusicPlaybackService;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

/**
 * Created by admin on 1/4/2018.
 * Phoenix-for-VK
 */
public class AudiosSearchPresenter extends AbsSearchPresenter<IAudioSearchView, AudioSearchCriteria, Audio, IntNextFrom> {

    private final IAudioInteractor audioInteractor;
    private final boolean audioAvailable;

    public AudiosSearchPresenter(int accountId, @Nullable AudioSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.audioInteractor = InteractorFactory.createAudioInteractor();
        this.audioAvailable = audioInteractor.isAudioPluginAvailable();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Audio>, IntNextFrom>> doSearch(int accountId, AudioSearchCriteria criteria, IntNextFrom startFrom) {
        final IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + 50);

        return audioInteractor.search(criteria.getQuery(), criteria.isOwn(), startFrom.getOffset())
                .map(audio -> Pair.Companion.create(audio, nextFrom));
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, (ArrayList<Audio>) data, position, false);
        PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    @Override
    boolean canSearch(AudioSearchCriteria criteria) {
        return audioAvailable;
    }

    private void onListGetError(Throwable t) {
        showError(getView(), Utils.getCauseIfRuntime(t));
    }

    @Override
    AudioSearchCriteria instantiateEmptyCriteria() {
        return new AudioSearchCriteria("", false);
    }

}