package dev.velaron.fennec.mvp.presenter.photo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.db.Stores;
import dev.velaron.fennec.db.serialize.Serializers;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.TmpSource;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 25.09.2016.
 * phoenix
 */
public class TmpGalleryPagerPresenter extends PhotoPagerPresenter {

    private final TmpSource source;

    public TmpGalleryPagerPresenter(int accountId, @NonNull TmpSource source, int index,
                                    @Nullable Bundle savedInstanceState) {
        super(new ArrayList<>(0), accountId, savedInstanceState);
        this.source = source;
        setCurrentIndex(index);

        loadDataFromDatabase();
    }

    @Override
    void initPhotosData(@NonNull ArrayList<Photo> initialData, @Nullable Bundle savedInstanceState) {
        super.mPhotos = initialData;
    }

    @Override
    void savePhotosState(@NonNull Bundle outState) {
        // no saving
    }

    private void loadDataFromDatabase() {
        changeLoadingNowState(true);
        appendDisposable(Stores.getInstance()
                .tempStore()
                .getData(source.getOwnerId(), source.getSourceId(), Serializers.PHOTOS_SERIALIZER)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialLoadingFinished, Analytics::logUnexpectedError));
    }

    private void onInitialLoadingFinished(List<Photo> photos) {
        changeLoadingNowState(false);

        getData().addAll(photos);

        refreshPagerView();
        resolveButtonsBarVisible();
        resolveToolbarVisibility();
        refreshInfoViews();
    }
}