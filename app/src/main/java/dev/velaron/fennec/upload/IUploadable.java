package dev.velaron.fennec.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.api.PercentagePublisher;
import dev.velaron.fennec.api.model.server.UploadServer;
import io.reactivex.Single;

public interface IUploadable<T> {
    Single<UploadResult<T>> doUpload(@NonNull Upload upload,
                                     @Nullable UploadServer initialServer,
                                     @Nullable PercentagePublisher listener);
}