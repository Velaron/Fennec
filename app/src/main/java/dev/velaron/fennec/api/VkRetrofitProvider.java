package dev.velaron.fennec.api;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.velaron.fennec.api.adapters.AttachmentsDtoAdapter;
import dev.velaron.fennec.api.adapters.AttachmentsEntryDtoAdapter;
import dev.velaron.fennec.api.adapters.BooleanAdapter;
import dev.velaron.fennec.api.adapters.ChatDtoAdapter;
import dev.velaron.fennec.api.adapters.ChatUserDtoAdapter;
import dev.velaron.fennec.api.adapters.ChatsInfoAdapter;
import dev.velaron.fennec.api.adapters.CommentDtoAdapter;
import dev.velaron.fennec.api.adapters.CommunityDtoAdapter;
import dev.velaron.fennec.api.adapters.CustomCommentsResponseAdapter;
import dev.velaron.fennec.api.adapters.FeedbackDtoAdapter;
import dev.velaron.fennec.api.adapters.FeedbackUserArrayDtoAdapter;
import dev.velaron.fennec.api.adapters.GroupSettingsAdapter;
import dev.velaron.fennec.api.adapters.LikesListAdapter;
import dev.velaron.fennec.api.adapters.LongpollUpdateAdapter;
import dev.velaron.fennec.api.adapters.MessageDtoAdapter;
import dev.velaron.fennec.api.adapters.NewsAdapter;
import dev.velaron.fennec.api.adapters.NewsfeedCommentDtoAdapter;
import dev.velaron.fennec.api.adapters.PhotoAlbumDtoAdapter;
import dev.velaron.fennec.api.adapters.PhotoDtoAdapter;
import dev.velaron.fennec.api.adapters.PostDtoAdapter;
import dev.velaron.fennec.api.adapters.PostSourceDtoAdapter;
import dev.velaron.fennec.api.adapters.PrivacyDtoAdapter;
import dev.velaron.fennec.api.adapters.SchoolClazzDtoAdapter;
import dev.velaron.fennec.api.adapters.SearchDialogsResponseAdapter;
import dev.velaron.fennec.api.adapters.ToticDtoAdapter;
import dev.velaron.fennec.api.adapters.UserDtoAdapter;
import dev.velaron.fennec.api.adapters.VideoDtoAdapter;
import dev.velaron.fennec.api.model.ChatUserDto;
import dev.velaron.fennec.api.model.GroupSettingsDto;
import dev.velaron.fennec.api.model.VKApiChat;
import dev.velaron.fennec.api.model.VKApiComment;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiMessage;
import dev.velaron.fennec.api.model.VKApiNews;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPhotoAlbum;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.api.model.VKApiTopic;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.VkApiAttachments;
import dev.velaron.fennec.api.model.VkApiPostSource;
import dev.velaron.fennec.api.model.VkApiPrivacy;
import dev.velaron.fennec.api.model.database.SchoolClazzDto;
import dev.velaron.fennec.api.model.feedback.UserArray;
import dev.velaron.fennec.api.model.feedback.VkApiBaseFeedback;
import dev.velaron.fennec.api.model.longpoll.AbsLongpollEvent;
import dev.velaron.fennec.api.model.response.ChatsInfoResponse;
import dev.velaron.fennec.api.model.response.CustomCommentsResponse;
import dev.velaron.fennec.api.model.response.LikesListResponse;
import dev.velaron.fennec.api.model.response.NewsfeedCommentsResponse;
import dev.velaron.fennec.api.model.response.SearchDialogsResponse;
import dev.velaron.fennec.settings.IProxySettings;
import dev.velaron.fennec.settings.Settings;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ruslan Kolbasa on 21.07.2017.
 * phoenix
 */
public class VkRetrofitProvider implements IVkRetrofitProvider {

    private static final Gson VKGSON = new GsonBuilder()
            .registerTypeAdapter(VkApiAttachments.Entry.class, new AttachmentsEntryDtoAdapter())
            .registerTypeAdapter(VKApiPhoto.class, new PhotoDtoAdapter())
            .registerTypeAdapter(boolean.class, new BooleanAdapter())
            .registerTypeAdapter(VkApiPrivacy.class, new PrivacyDtoAdapter())
            .registerTypeAdapter(VKApiPhotoAlbum.class, new PhotoAlbumDtoAdapter())
            .registerTypeAdapter(VkApiAttachments.class, new AttachmentsDtoAdapter())
            .registerTypeAdapter(VKApiPost.class, new PostDtoAdapter())
            .registerTypeAdapter(VkApiPostSource.class, new PostSourceDtoAdapter())
            .registerTypeAdapter(VKApiUser.class, new UserDtoAdapter())
            .registerTypeAdapter(VKApiCommunity.class, new CommunityDtoAdapter())
            .registerTypeAdapter(VkApiBaseFeedback.class, new FeedbackDtoAdapter())
            .registerTypeAdapter(VKApiComment.class, new CommentDtoAdapter())
            .registerTypeAdapter(VKApiVideo.class, new VideoDtoAdapter())
            .registerTypeAdapter(UserArray.class, new FeedbackUserArrayDtoAdapter())
            .registerTypeAdapter(VKApiMessage.class, new MessageDtoAdapter())
            .registerTypeAdapter(VKApiNews.class, new NewsAdapter())
            .registerTypeAdapter(AbsLongpollEvent.class, new LongpollUpdateAdapter())
            .registerTypeAdapter(ChatsInfoResponse.class, new ChatsInfoAdapter())
            .registerTypeAdapter(VKApiChat.class, new ChatDtoAdapter())
            .registerTypeAdapter(ChatUserDto.class, new ChatUserDtoAdapter())
            .registerTypeAdapter(SchoolClazzDto.class, new SchoolClazzDtoAdapter())
            .registerTypeAdapter(LikesListResponse.class, new LikesListAdapter())
            .registerTypeAdapter(SearchDialogsResponse.class, new SearchDialogsResponseAdapter())
            .registerTypeAdapter(NewsfeedCommentsResponse.Dto.class, new NewsfeedCommentDtoAdapter())
            .registerTypeAdapter(VKApiTopic.class, new ToticDtoAdapter())
            .registerTypeAdapter(GroupSettingsDto.class, new GroupSettingsAdapter())
            .registerTypeAdapter(CustomCommentsResponse.class, new CustomCommentsResponseAdapter())
            .create();

    private static final GsonConverterFactory GSON_CONVERTER_FACTORY = GsonConverterFactory.create(VKGSON);
    private static final RxJava2CallAdapterFactory RX_ADAPTER_FACTORY = RxJava2CallAdapterFactory.create();

    private final IProxySettings proxyManager;
    private final IVkMethodHttpClientFactory clientFactory;

    public VkRetrofitProvider(IProxySettings proxySettings, IVkMethodHttpClientFactory clientFactory) {
        this.proxyManager = proxySettings;
        this.clientFactory = clientFactory;
        this.proxyManager.observeActive()
                .subscribe(optional -> onProxySettingsChanged());
    }

    private void onProxySettingsChanged(){
        synchronized (retrofitCacheLock){
            for(Map.Entry<Integer, RetrofitWrapper> entry : retrofitCache.entrySet()){
                entry.getValue().cleanup();
            }

            retrofitCache.clear();
        }
    }

    @SuppressLint("UseSparseArrays")
    private Map<Integer, RetrofitWrapper> retrofitCache = Collections.synchronizedMap(new HashMap<>(1));

    private final Object retrofitCacheLock = new Object();

    @Override
    public Single<RetrofitWrapper> provideNormalRetrofit(int accountId) {
        return Single.fromCallable(() -> {
            RetrofitWrapper retrofit;

            synchronized (retrofitCacheLock){
                retrofit = retrofitCache.get(accountId);

                if (nonNull(retrofit)) {
                    return retrofit;
                }

                OkHttpClient client = clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy());
                retrofit = createDefaultVkApiRetrofit(client);
                retrofitCache.put(accountId, retrofit);
            }

            return retrofit;
        });
    }

    @Override
    public Single<RetrofitWrapper> provideCustomRetrofit(int accountId, String token) {
        return Single.fromCallable(() -> {
            OkHttpClient client = clientFactory.createCustomVkHttpClient(accountId, token, VKGSON, proxyManager.getActiveProxy());
            return createDefaultVkApiRetrofit(client);
        });
    }

    private volatile RetrofitWrapper serviceRetrofit;

    private final Object serviceRetrofitLock = new Object();

    @Override
    public Single<RetrofitWrapper> provideServiceRetrofit() {
        return Single.fromCallable(() -> {
            if (isNull(serviceRetrofit)) {
                synchronized (serviceRetrofitLock) {
                    if (isNull(serviceRetrofit)) {
                        OkHttpClient client = clientFactory.createServiceVkHttpClient(VKGSON, proxyManager.getActiveProxy());
                        serviceRetrofit = createDefaultVkApiRetrofit(client);
                    }
                }
            }

            return serviceRetrofit;
        });
    }

    @Override
    public Single<OkHttpClient> provideNormalHttpClient(int accountId) {
        return Single.fromCallable(() -> clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy()));
    }

    private RetrofitWrapper createDefaultVkApiRetrofit(OkHttpClient okHttpClient) {
        return RetrofitWrapper.wrap(new Retrofit.Builder()
                .baseUrl(Settings.get().other().getApiDomain())
                .addConverterFactory(GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(okHttpClient)
                .build());
    }
}