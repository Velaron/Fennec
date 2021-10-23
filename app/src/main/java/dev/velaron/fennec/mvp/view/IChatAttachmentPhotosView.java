package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.TmpSource;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public interface IChatAttachmentPhotosView extends IBaseChatAttachmentsView<Photo> {
    void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index);
}