package dev.velaron.fennec.db.interfaces;

import dev.velaron.fennec.crypt.KeyLocationPolicy;

/**
 * Created by ruslan.kolbasa on 25.11.2016.
 * phoenix
 */
public interface IStorages {

    ITempDataStorage tempStore();

    IVideoAlbumsStorage videoAlbums();

    IVideoStorage videos();

    IAttachmentsStorage attachments();

    IKeysStorage keys(@KeyLocationPolicy int policy);

    ILocalMediaStorage localPhotos();

    IFeedbackStorage notifications();

    IDialogsStorage dialogs();

    IMessagesStorage messages();

    IWallStorage wall();

    IFaveStorage fave();

    IPhotosStorage photos();

    IRelativeshipStorage relativeship();

    IFeedStorage feed();

    IOwnersStorage owners();

    ICommentsStorage comments();

    IPhotoAlbumsStorage photoAlbums();

    ITopicsStore topics();

    IDocsStorage docs();

    IStickersStorage stickers();

    IDatabaseStore database();
}