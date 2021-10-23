package dev.velaron.fennec.api.impl;

import static dev.velaron.fennec.util.Utils.intValueIn;

import android.annotation.SuppressLint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dev.velaron.fennec.api.IServiceProvider;
import dev.velaron.fennec.api.IVkRetrofitProvider;
import dev.velaron.fennec.api.RetrofitWrapper;
import dev.velaron.fennec.api.TokenType;
import dev.velaron.fennec.api.interfaces.IAccountApi;
import dev.velaron.fennec.api.interfaces.IAccountApis;
import dev.velaron.fennec.api.interfaces.IAudioApi;
import dev.velaron.fennec.api.interfaces.IBoardApi;
import dev.velaron.fennec.api.interfaces.ICommentsApi;
import dev.velaron.fennec.api.interfaces.IDatabaseApi;
import dev.velaron.fennec.api.interfaces.IDocsApi;
import dev.velaron.fennec.api.interfaces.IFaveApi;
import dev.velaron.fennec.api.interfaces.IFriendsApi;
import dev.velaron.fennec.api.interfaces.IGroupsApi;
import dev.velaron.fennec.api.interfaces.ILikesApi;
import dev.velaron.fennec.api.interfaces.IMessagesApi;
import dev.velaron.fennec.api.interfaces.INewsfeedApi;
import dev.velaron.fennec.api.interfaces.INotificationsApi;
import dev.velaron.fennec.api.interfaces.IOtherApi;
import dev.velaron.fennec.api.interfaces.IPagesApi;
import dev.velaron.fennec.api.interfaces.IPhotosApi;
import dev.velaron.fennec.api.interfaces.IPollsApi;
import dev.velaron.fennec.api.interfaces.IStatusApi;
import dev.velaron.fennec.api.interfaces.IStoreApi;
import dev.velaron.fennec.api.interfaces.IUsersApi;
import dev.velaron.fennec.api.interfaces.IUtilsApi;
import dev.velaron.fennec.api.interfaces.IVideoApi;
import dev.velaron.fennec.api.interfaces.IWallApi;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
class VkApies implements IAccountApis {

    @SuppressLint("UseSparseArrays")
    private static volatile Map<Integer, VkApies> APIS = new HashMap<>(1);

    private final IMessagesApi messagesApi;
    private final IPhotosApi photosApi;
    private final IFriendsApi friendsApi;
    private final IDocsApi docsApi;
    private final IWallApi wallApi;
    private final INewsfeedApi newsfeedApi;
    private final ICommentsApi commentsApi;
    private final INotificationsApi notificationsApi;
    private final IVideoApi videoApi;
    private final IBoardApi boardApi;
    private final IUsersApi usersApi;
    private final IGroupsApi groupsApi;
    private final IAccountApi accountApi;
    private final IDatabaseApi databaseApi;
    private final IAudioApi audioApi;
    private final IStatusApi statusApi;
    private final ILikesApi likesApi;
    private final IPagesApi pagesApi;
    private final IStoreApi storeApi;
    private final IFaveApi faveApi;
    private final IPollsApi pollsApi;
    private final IUtilsApi utilsApi;
    private final IOtherApi otherApi;

    private VkApies(int accountId, boolean useCustomToken, String customAccessToken, IVkRetrofitProvider provider) {
        IServiceProvider retrofitProvider = new IServiceProvider() {
            @Override
            public <T> Single<T> provideService(int accountId, Class<T> serviceClass, int... tokenTypes) {
                return provideRetrofit(accountId, tokenTypes).map(retrofit -> retrofit.create(serviceClass));
            }

            Single<RetrofitWrapper> provideRetrofit(int aid, int... tokenPolicy) {
                if (useCustomToken) {
                    return provider.provideCustomRetrofit(aid, customAccessToken);
                }

                boolean isCommunity = aid < 0;

                if (isCommunity) {
                    if (intValueIn(TokenType.COMMUNITY, tokenPolicy)) {
                        return provider.provideNormalRetrofit(aid);
                    } else if (intValueIn(TokenType.SERVICE, tokenPolicy)) {
                        return provider.provideServiceRetrofit();
                    } else {
                        return Single.error(new UnsupportedOperationException("Unsupported account_id: " + aid + " with token_policy: " + Arrays.toString(tokenPolicy)));
                    }
                } else {
                    if (intValueIn(TokenType.USER, tokenPolicy)) {
                        return provider.provideNormalRetrofit(aid);
                    } else if (intValueIn(TokenType.SERVICE, tokenPolicy)) {
                        return provider.provideServiceRetrofit();
                    } else {
                        return Single.error(new UnsupportedOperationException("Unsupported account_id: " + aid + " with token_policy: " + Arrays.toString(tokenPolicy)));
                    }
                }
            }
        };

        this.accountApi = new AccountApi(accountId, retrofitProvider);
        this.audioApi = new AudioApi(accountId, retrofitProvider);
        this.boardApi = new BoardApi(accountId, retrofitProvider);
        this.commentsApi = new CommentsApi(accountId, retrofitProvider);
        this.databaseApi = new DatabaseApi(accountId, retrofitProvider);
        this.docsApi = new DocsApi(accountId, retrofitProvider);
        this.faveApi = new FaveApi(accountId, retrofitProvider);
        this.friendsApi = new FriendsApi(accountId, retrofitProvider);
        this.groupsApi = new GroupsApi(accountId, retrofitProvider);
        this.likesApi = new LikesApi(accountId, retrofitProvider);
        this.messagesApi = new MessagesApi(accountId, retrofitProvider);
        this.newsfeedApi = new NewsfeedApi(accountId, retrofitProvider);
        this.notificationsApi = new NotificationsApi(accountId, retrofitProvider);
        this.pagesApi = new PagesApi(accountId, retrofitProvider);
        this.photosApi = new PhotosApi(accountId, retrofitProvider);
        this.pollsApi = new PollsApi(accountId, retrofitProvider);
        this.statusApi = new StatusApi(accountId, retrofitProvider);
        this.storeApi = new StoreApi(accountId, retrofitProvider);
        this.usersApi = new UsersApi(accountId, retrofitProvider);
        this.utilsApi = new UtilsApi(accountId, retrofitProvider);
        this.videoApi = new VideoApi(accountId, retrofitProvider);
        this.wallApi = new WallApi(accountId, retrofitProvider);
        this.otherApi = new OtherApi(accountId, provider);
    }

    public static VkApies create(int accountId, String accessToken, IVkRetrofitProvider provider) {
        return new VkApies(accountId, true, accessToken, provider);
    }

    public static synchronized VkApies get(int accountId, IVkRetrofitProvider provider) {
        VkApies apies = APIS.get(accountId);
        if (apies == null) {
            apies = new VkApies(accountId, false, null, provider);
            APIS.put(accountId, apies);
        }

        return apies;
    }

    @Override
    public IMessagesApi messages() {
        return messagesApi;
    }

    @Override
    public IPhotosApi photos() {
        return photosApi;
    }

    @Override
    public IFriendsApi friends() {
        return friendsApi;
    }

    @Override
    public IWallApi wall() {
        return wallApi;
    }

    @Override
    public IDocsApi docs() {
        return docsApi;
    }

    @Override
    public INewsfeedApi newsfeed() {
        return newsfeedApi;
    }

    @Override
    public ICommentsApi comments() {
        return commentsApi;
    }

    @Override
    public INotificationsApi notifications() {
        return notificationsApi;
    }

    @Override
    public IVideoApi video() {
        return videoApi;
    }

    @Override
    public IBoardApi board() {
        return boardApi;
    }

    @Override
    public IUsersApi users() {
        return usersApi;
    }

    @Override
    public IGroupsApi groups() {
        return groupsApi;
    }

    @Override
    public IAccountApi account() {
        return accountApi;
    }

    @Override
    public IDatabaseApi database() {
        return databaseApi;
    }

    @Override
    public IAudioApi audio() {
        return audioApi;
    }

    @Override
    public IStatusApi status() {
        return statusApi;
    }

    @Override
    public ILikesApi likes() {
        return likesApi;
    }

    @Override
    public IPagesApi pages() {
        return pagesApi;
    }

    @Override
    public IStoreApi store() {
        return storeApi;
    }

    @Override
    public IFaveApi fave() {
        return faveApi;
    }

    @Override
    public IPollsApi polls() {
        return pollsApi;
    }

    @Override
    public IUtilsApi utils() {
        return utilsApi;
    }

    @Override
    public IOtherApi other() {
        return otherApi;
    }
}