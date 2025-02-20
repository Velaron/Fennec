package dev.velaron.fennec.db.impl;

import static dev.velaron.fennec.util.Objects.isNull;

import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;

import dev.velaron.fennec.crypt.KeyLocationPolicy;
import dev.velaron.fennec.db.interfaces.IAttachmentsStorage;
import dev.velaron.fennec.db.interfaces.ICommentsStorage;
import dev.velaron.fennec.db.interfaces.IDatabaseStore;
import dev.velaron.fennec.db.interfaces.IDialogsStorage;
import dev.velaron.fennec.db.interfaces.IDocsStorage;
import dev.velaron.fennec.db.interfaces.IFaveStorage;
import dev.velaron.fennec.db.interfaces.IFeedStorage;
import dev.velaron.fennec.db.interfaces.IFeedbackStorage;
import dev.velaron.fennec.db.interfaces.IKeysStorage;
import dev.velaron.fennec.db.interfaces.ILocalMediaStorage;
import dev.velaron.fennec.db.interfaces.IMessagesStorage;
import dev.velaron.fennec.db.interfaces.IOwnersStorage;
import dev.velaron.fennec.db.interfaces.IPhotoAlbumsStorage;
import dev.velaron.fennec.db.interfaces.IPhotosStorage;
import dev.velaron.fennec.db.interfaces.IRelativeshipStorage;
import dev.velaron.fennec.db.interfaces.IStickersStorage;
import dev.velaron.fennec.db.interfaces.IStorages;
import dev.velaron.fennec.db.interfaces.ITempDataStorage;
import dev.velaron.fennec.db.interfaces.ITopicsStore;
import dev.velaron.fennec.db.interfaces.IVideoAlbumsStorage;
import dev.velaron.fennec.db.interfaces.IVideoStorage;
import dev.velaron.fennec.db.interfaces.IWallStorage;

public class AppStorages extends ContextWrapper implements IStorages {

    private static AppStorages sStoresInstance;
    private final ITempDataStorage tempData = new TempDataStorage(this);
    private IOwnersStorage owners;
    private IFeedStorage feed;
    private IRelativeshipStorage relativeship;
    private IPhotosStorage photos;
    private IFaveStorage fave;
    private IWallStorage wall;
    private IMessagesStorage messages;
    private IDialogsStorage dialogs;
    private IFeedbackStorage feedback;
    private ILocalMediaStorage localMedia;
    private KeysPersistStorage keysPersist;
    private KeysRamStorage keysRam;
    private IAttachmentsStorage attachments;
    private volatile IVideoStorage video;
    private volatile IVideoAlbumsStorage videoAlbums;
    private volatile ICommentsStorage comments;
    private volatile IPhotoAlbumsStorage photoAlbums;
    private volatile ITopicsStore topics;
    private volatile IDocsStorage docs;
    private volatile IStickersStorage stickers;
    private volatile IDatabaseStore database;

    public AppStorages(Context base) {
        super(base);
    }

    public static AppStorages getInstance(Context baseContext){
        if(isNull(sStoresInstance)){
            synchronized (AppStorages.class){
                if(isNull(sStoresInstance)){
                    sStoresInstance = new AppStorages(baseContext.getApplicationContext());
                }
            }
        }

        return sStoresInstance;
    }

    @Override
    public ICommentsStorage comments() {
        if(isNull(comments)){
            synchronized (this){
                if(isNull(comments)){
                    comments = new CommentsStorage(this);
                }
            }
        }

        return comments;
    }

    @Override
    public IPhotoAlbumsStorage photoAlbums() {
        if(isNull(photoAlbums)){
            synchronized (this){
                if(isNull(photoAlbums)){
                    photoAlbums = new PhotoAlbumsStorage(this);
                }
            }
        }

        return photoAlbums;
    }

    @Override
    public ITopicsStore topics() {
        if(isNull(topics)){
            synchronized (this){
                if(isNull(topics)){
                    topics = new TopicsStorage(this);
                }
            }
        }

        return topics;
    }

    @Override
    public IDocsStorage docs() {
        if(isNull(docs)){
            synchronized (this){
                if(isNull(docs)){
                    docs = new DocsStorage(this);
                }
            }
        }

        return docs;
    }

    @Override
    public IStickersStorage stickers() {
        if(isNull(stickers)){
            synchronized (this){
                if(isNull(stickers)){
                    stickers = new StickersStorage(this);
                }
            }
        }

        return stickers;
    }

    @Override
    public IDatabaseStore database() {
        if(isNull(database)){
            synchronized (this){
                if(isNull(database)){
                    database = new DatabaseStorage(this);
                }
            }
        }
        return database;
    }

    @Override
    public ITempDataStorage tempStore() {
        return tempData;
    }

    public IVideoAlbumsStorage videoAlbums(){
        if(isNull(videoAlbums)){
            synchronized (this){
                if(isNull(videoAlbums)){
                    videoAlbums = new VideoAlbumsStorage(this);
                }
            }
        }

        return videoAlbums;
    }

    public IVideoStorage videos(){
        if(isNull(video)){
            synchronized (this){
                if(isNull(video)){
                    video = new VideoStorage(this);
                }
            }
        }

        return video;
    }

    @NonNull
    public synchronized IAttachmentsStorage attachments(){
        if(attachments == null){
            attachments = new AttachmentsStorage(this);
        }

        return attachments;
    }

    @NonNull
    public synchronized IKeysStorage keys(@KeyLocationPolicy int policy){
        switch (policy){
            case KeyLocationPolicy.PERSIST:
                if(isNull(keysPersist)){
                    keysPersist = new KeysPersistStorage(this);
                }

                return keysPersist;
            case KeyLocationPolicy.RAM:
                if(isNull(keysRam)){
                    keysRam = new KeysRamStorage();
                }

                return keysRam;
            default:
                throw new IllegalArgumentException("Unsupported key location policy");
        }
    }

    @NonNull
    public synchronized ILocalMediaStorage localPhotos(){
        if(localMedia == null){
            localMedia = new LocalMediaStorage(this);
        }

        return localMedia;
    }

    @NonNull
    public synchronized IFeedbackStorage notifications(){
        if(feedback == null){
            feedback = new FeedbackStorage(this);
        }

        return feedback;
    }

    @NonNull
    public synchronized IDialogsStorage dialogs(){
        if(dialogs == null){
            dialogs = new DialogsStorage(this);
        }

        return dialogs;
    }

    @NonNull
    public synchronized IMessagesStorage messages(){
        if(messages == null){
            messages = new MessagesStorage(this);
        }

        return messages;
    }

    @NonNull
    public synchronized IWallStorage wall(){
        if(wall == null){
            wall = new WallStorage(this);
        }

        return wall;
    }

    @NonNull
    public synchronized IFaveStorage fave(){
        if(fave == null){
            fave = new FaveStorage(this);
        }

        return fave;
    }

    @NonNull
    public synchronized IPhotosStorage photos(){
        if(photos == null){
            photos = new PhotosStorage(this);
        }

        return photos;
    }

    @NonNull
    public synchronized IRelativeshipStorage relativeship(){
        if(relativeship == null){
            relativeship = new RelativeshipStorage(this);
        }

        return relativeship;
    }

    @NonNull
    public synchronized IFeedStorage feed(){
        if(feed == null){
            feed = new FeedStorage(this);
        }

        return feed;
    }

    @NonNull
    public synchronized IOwnersStorage owners(){
        if(owners == null){
            owners = new OwnersStorage(this);
        }

        return owners;
    }
}
