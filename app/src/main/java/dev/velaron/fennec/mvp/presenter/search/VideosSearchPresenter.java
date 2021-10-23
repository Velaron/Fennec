package dev.velaron.fennec.mvp.presenter.search;

import static dev.velaron.fennec.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.domain.IVideosInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.fragment.search.criteria.VideoSearchCriteria;
import dev.velaron.fennec.fragment.search.nextfrom.IntNextFrom;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.view.search.IVideosSearchView;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public class VideosSearchPresenter extends AbsSearchPresenter<IVideosSearchView, VideoSearchCriteria, Video, IntNextFrom> {

    private final IVideosInteractor videosInteractor;

    public VideosSearchPresenter(int accountId, @Nullable VideoSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.videosInteractor = InteractorFactory.createVideosInteractor();
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
    Single<Pair<List<Video>, IntNextFrom>> doSearch(int accountId, VideoSearchCriteria criteria, IntNextFrom startFrom) {
        final int offset = startFrom.getOffset();
        final IntNextFrom nextFrom = new IntNextFrom(offset + 50);
        return videosInteractor.seacrh(accountId, criteria, 50, offset)
                .map(videos -> Pair.Companion.create(videos, nextFrom));
    }

    @Override
    VideoSearchCriteria instantiateEmptyCriteria() {
        return new VideoSearchCriteria("");
    }

    @Override
    boolean canSearch(VideoSearchCriteria criteria) {
        return nonEmpty(criteria.getQuery());
    }
}