package dev.velaron.fennec.domain;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.domain.impl.AccountsInteractor;
import dev.velaron.fennec.domain.impl.AudioInteractor;
import dev.velaron.fennec.domain.impl.BoardInteractor;
import dev.velaron.fennec.domain.impl.CommunitiesInteractor;
import dev.velaron.fennec.domain.impl.DatabaseInteractor;
import dev.velaron.fennec.domain.impl.DialogsInteractor;
import dev.velaron.fennec.domain.impl.DocsInteractor;
import dev.velaron.fennec.domain.impl.FaveInteractor;
import dev.velaron.fennec.domain.impl.FeedInteractor;
import dev.velaron.fennec.domain.impl.FeedbackInteractor;
import dev.velaron.fennec.domain.impl.GroupSettingsInteractor;
import dev.velaron.fennec.domain.impl.LikesInteractor;
import dev.velaron.fennec.domain.impl.NewsfeedInteractor;
import dev.velaron.fennec.domain.impl.PhotosInteractor;
import dev.velaron.fennec.domain.impl.PollInteractor;
import dev.velaron.fennec.domain.impl.RelationshipInteractor;
import dev.velaron.fennec.domain.impl.StickersInteractor;
import dev.velaron.fennec.domain.impl.UtilsInteractor;
import dev.velaron.fennec.domain.impl.VideosInteractor;
import dev.velaron.fennec.plugins.AudioPluginConnector;
import dev.velaron.fennec.settings.Settings;

/**
 * Created by Ruslan Kolbasa on 26.06.2017.
 * phoenix
 */
public class InteractorFactory {

    public static INewsfeedInteractor createNewsfeedInteractor(){
        return new NewsfeedInteractor(Injection.provideNetworkInterfaces(), Repository.INSTANCE.getOwners());
    }

    public static IStickersInteractor createStickersInteractor(){
        return new StickersInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().stickers());
    }

    public static IPollInteractor createPollInteractor(){
        return new PollInteractor(Injection.provideNetworkInterfaces());
    }

    public static IDocsInteractor createDocsInteractor(){
        return new DocsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().docs());
    }

    public static ILikesInteractor createLikesInteractor(){
        return new LikesInteractor(Injection.provideNetworkInterfaces());
    }

    public static IFeedbackInteractor createFeedbackInteractor(){
        return new FeedbackInteractor(Injection.provideStores(), Injection.provideNetworkInterfaces(), Repository.INSTANCE.getOwners());
    }

    public static IDatabaseInteractor createDatabaseInteractor(){
        return new DatabaseInteractor(Injection.provideStores().database(), Injection.provideNetworkInterfaces());
    }

    public static ICommunitiesInteractor createCommunitiesInteractor(){
        return new CommunitiesInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IBoardInteractor createBoardInteractor(){
        return new BoardInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
    }

    public static IUtilsInteractor createUtilsInteractor(){
        return new UtilsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
    }

    public static IRelationshipInteractor createRelationshipInteractor(){
        return new RelationshipInteractor(Injection.provideStores(), Injection.provideNetworkInterfaces());
    }

    public static IFeedInteractor createFeedInteractor(){
        return new FeedInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Settings.get().other(), Repository.INSTANCE.getOwners());
    }

    public static IGroupSettingsInteractor createGroupSettingsInteractor(){
        return new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners(), Repository.INSTANCE.getOwners());
    }

    public static IDialogsInteractor createDialogsInteractor(){
        return new DialogsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IVideosInteractor createVideosInteractor(){
        return new VideosInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IAccountsInteractor createAccountInteractor(){
        return new AccountsInteractor(
                Injection.provideNetworkInterfaces(),
                Injection.provideSettings().accounts(),
                Injection.provideBlacklistRepository(),
                Repository.INSTANCE.getOwners()
        );
    }

    public static IPhotosInteractor createPhotosInteractor(){
        return new PhotosInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IFaveInteractor createFaveInteractor(){
        return new FaveInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
    }

    public static IAudioInteractor createAudioInteractor() {
        return new AudioInteractor(Injection.provideNetworkInterfaces(),
                new AudioPluginConnector(Injection.provideApplicationContext()));
    }
}