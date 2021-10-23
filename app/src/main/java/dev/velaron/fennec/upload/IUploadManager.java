package dev.velaron.fennec.upload;

import java.util.List;

import androidx.annotation.NonNull;
import dev.velaron.fennec.util.Optional;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface IUploadManager {
    Single<List<Upload>> get(int accountId, @NonNull UploadDestination destination);

    void enqueue(@NonNull List<UploadIntent> intents);

    void cancel(int id);

    void cancelAll(int accountId, @NonNull UploadDestination destination);

    Optional<Upload> getCurrent();

    Flowable<int[]> observeDeleting(boolean includeCompleted);

    Flowable<List<Upload>> observeAdding();

    Flowable<Upload> obseveStatus();

    Flowable<Pair<Upload, UploadResult<?>>> observeResults();

    Flowable<List<IProgressUpdate>> observeProgress();

    interface IProgressUpdate {
        int getId();
        int getProgress();
    }
}