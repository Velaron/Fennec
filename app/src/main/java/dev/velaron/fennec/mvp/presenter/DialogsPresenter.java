package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.RxUtils.dummy;
import static dev.velaron.fennec.util.RxUtils.ignore;
import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.indexOf;
import static dev.velaron.fennec.util.Utils.isEmpty;
import static dev.velaron.fennec.util.Utils.safeIsEmpty;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.R;
import dev.velaron.fennec.domain.IMessagesRepository;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.exception.UnauthorizedException;
import dev.velaron.fennec.longpoll.ILongpollManager;
import dev.velaron.fennec.longpoll.LongpollInstance;
import dev.velaron.fennec.model.Dialog;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Peer;
import dev.velaron.fennec.model.PeerUpdate;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.IDialogsView;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.util.Analytics;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Optional;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.ShortcutUtils;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 11.01.2017.
 * phoenix
 */
public class DialogsPresenter extends AccountDependencyPresenter<IDialogsView> {

    private static final int COUNT = 30;

    private static final String SAVE_DIALOGS_OWNER_ID = "save-dialogs-owner-id";

    private int dialogsOwnerId;

    private final ArrayList<Dialog> dialogs;
    private boolean endOfContent;

    private final IMessagesRepository messagesInteractor;
    private final ILongpollManager longpollManager;

    public DialogsPresenter(int accountId, int initialDialogsOwnerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        setSupportAccountHotSwap(true);

        dialogs = new ArrayList<>();

        if (nonNull(savedInstanceState)) {
            dialogsOwnerId = savedInstanceState.getInt(SAVE_DIALOGS_OWNER_ID);
        } else {
            dialogsOwnerId = initialDialogsOwnerId;
        }

        messagesInteractor = Repository.INSTANCE.getMessages();
        longpollManager = LongpollInstance.get();

        appendDisposable(messagesInteractor
                .observePeerUpdates()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPeerUpdate, ignore()));

        appendDisposable(messagesInteractor.observePeerDeleting()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(dialog -> onDialogDeleted(dialog.getAccountId(), dialog.getPeerId()), ignore()));

        appendDisposable(longpollManager.observeKeepAlive()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(ignore -> checkLongpoll(), ignore()));

        loadCachedThenActualData();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_DIALOGS_OWNER_ID, dialogsOwnerId);
    }

    @Override
    public void onGuiCreated(@NonNull IDialogsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(dialogs);

        // only for user dialogs
        viewHost.setCreateGroupChatButtonVisible(dialogsOwnerId > 0);
    }

    private void onDialogsFisrtResponse(List<Dialog> data) {
        setNetLoadnigNow(false);

        endOfContent = false;
        dialogs.clear();
        dialogs.addAll(data);

        safeNotifyDataSetChanged();

        try {
            appendDisposable(InteractorFactory.createStickersInteractor()
                    .getAndStore(getAccountId())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(dummy(), ignore()));
        } catch (Exception ignored) {
            /*ignore*/
        }
    }

    private void onDialogsGetError(Throwable t) {
        Throwable cause = getCauseIfRuntime(t);

        cause.printStackTrace();

        setNetLoadnigNow(false);

        if (cause instanceof UnauthorizedException) {
            return;
        }

        showError(getView(), cause);
    }

    private boolean netLoadnigNow;

    private void setNetLoadnigNow(boolean netLoadnigNow) {
        this.netLoadnigNow = netLoadnigNow;
        resolveRefreshingView();
    }

    private void requestAtLast() {
        if (netLoadnigNow) {
            return;
        }

        setNetLoadnigNow(true);

        netDisposable.add(messagesInteractor.getDialogs(dialogsOwnerId, COUNT, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDialogsFisrtResponse, this::onDialogsGetError));

        resolveRefreshingView();
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();

    private void requestNext() {
        if (netLoadnigNow) {
            return;
        }

        Integer lastMid = getLastDialogMessageId();
        if (isNull(lastMid)) {
            return;
        }

        setNetLoadnigNow(true);
        netDisposable.add(messagesInteractor.getDialogs(dialogsOwnerId, COUNT, lastMid)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onNextDialogsResponse,
                        throwable -> onDialogsGetError(getCauseIfRuntime(throwable))));
    }

    private void onNextDialogsResponse(List<Dialog> data) {
        setNetLoadnigNow(false);
        endOfContent = isEmpty(dialogs);

        int startSize = dialogs.size();
        dialogs.addAll(data);

        if (isGuiReady()) {
            getView().notifyDataAdded(startSize, data.size());
        }
    }

    private void onDialogRemovedSuccessfully(int accountId, int peeId) {
        callView(v -> v.showSnackbar(R.string.deleted, true));
        onDialogDeleted(accountId, peeId);
    }

    private void removeDialog(final int peeId) {
        final int accountId = dialogsOwnerId;

        appendDisposable(messagesInteractor.deleteDialog(accountId, peeId, 0, 10000)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onDialogRemovedSuccessfully(accountId, peeId), t -> showError(getView(), t)));
    }

    private void resolveRefreshingView() {
        // on resume only !!!
        if (isGuiResumed()) {
            getView().showRefreshing(cacheNowLoading || netLoadnigNow);
        }
    }

    private boolean cacheNowLoading;
    private CompositeDisposable cacheLoadingDisposable = new CompositeDisposable();

    private void loadCachedThenActualData() {
        cacheNowLoading = true;
        resolveRefreshingView();

        cacheLoadingDisposable.add(messagesInteractor.getCachedDialogs(dialogsOwnerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, ignored -> {
                    ignored.printStackTrace();
                    onCachedDataReceived(Collections.emptyList());
                }));
    }

    private void onCachedDataReceived(List<Dialog> data) {
        cacheNowLoading = false;

        dialogs.clear();
        dialogs.addAll(data);

        safeNotifyDataSetChanged();
        resolveRefreshingView();

        requestAtLast();
    }

    private void onPeerUpdate(List<PeerUpdate> updates) {
        for (PeerUpdate update : updates) {
            if (update.getAccountId() == dialogsOwnerId) {
                onDialogUpdate(update);
            }
        }
    }

    private void onDialogUpdate(PeerUpdate update) {
        if (dialogsOwnerId != update.getAccountId()) {
            return;
        }

        final int accountId = update.getAccountId();
        final int peerId = update.getPeerId();

        if (update.getLastMessage() != null) {
            List<Integer> id = Collections.singletonList(update.getLastMessage().getMessageId());
            appendDisposable(messagesInteractor.findCachedMessages(accountId, id)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(messages -> {
                        if (messages.isEmpty()) {
                            onDialogDeleted(accountId, peerId);
                        } else {
                            onActualMessagePeerMessageReceived(accountId, peerId, update, Optional.wrap(messages.get(0)));
                        }
                    }, ignore()));
        } else {
            onActualMessagePeerMessageReceived(accountId, peerId, update, Optional.empty());
        }
    }

    private void onActualMessagePeerMessageReceived(int accountId, int peerId, PeerUpdate update, Optional<Message> messageOptional) {
        if (accountId != dialogsOwnerId) {
            return;
        }

        int index = indexOf(dialogs, peerId);
        if (index != -1) {
            Dialog dialog = dialogs.get(index);

            if (update.getReadIn() != null) {
                dialog.setInRead(update.getReadIn().getMessageId());
            }

            if (update.getReadOut() != null) {
                dialog.setOutRead(update.getReadOut().getMessageId());
            }

            if (update.getUnread() != null) {
                dialog.setUnreadCount(update.getUnread().getCount());
            }

            if (messageOptional.nonEmpty()) {
                Message message = messageOptional.get();
                dialog.setLastMessageId(message.getId());
                dialog.setMessage(message);

                if (dialog.isChat()) {
                    dialog.setInterlocutor(message.getSender());
                }
            }

            if(update.getTitle() != null){
                dialog.setTitle(update.getTitle().getTitle());
            }

            Collections.sort(dialogs, COMPARATOR);
        }

        safeNotifyDataSetChanged();
    }

    private void onDialogDeleted(int accountId, int peerId) {
        if (dialogsOwnerId != accountId) {
            return;
        }

        int index = indexOf(dialogs, peerId);
        if (index != -1) {
            dialogs.remove(index);
            safeNotifyDataSetChanged();
        }
    }

    private void safeNotifyDataSetChanged() {
        if (isGuiReady()) {
            getView().notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyed() {
        cacheLoadingDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        checkLongpoll();
    }

    private void checkLongpoll() {
        if (isGuiResumed() && getAccountId() != ISettings.IAccountsSettings.INVALID_ID) {
            longpollManager.keepAlive(dialogsOwnerId);
        }
    }

    private static final Comparator<Dialog> COMPARATOR = (rhs, lhs) -> Integer.compare(lhs.getLastMessageId(), rhs.getLastMessageId());

    public void fireRefresh() {
        cacheLoadingDisposable.dispose();
        cacheNowLoading = false;

        netDisposable.clear();
        netLoadnigNow = false;

        requestAtLast();
    }

    public void fireSearchClick() {
        AssertUtils.assertPositive(dialogsOwnerId);
        getView().goToSearch(getAccountId());
    }

    public void fireDialogClick(Dialog dialog) {
        this.openChat(dialog);
    }

    private void openChat(Dialog dialog) {
        getView().goToChat(getAccountId(),
                dialogsOwnerId,
                dialog.getPeerId(),
                dialog.getDisplayTitle(getApplicationContext()),
                dialog.getImageUrl());
    }

    public void fireDialogAvatarClick(Dialog dialog) {
        if (Peer.isUser(dialog.getPeerId()) || Peer.isGroup(dialog.getPeerId())) {
            getView().goToOwnerWall(super.getAccountId(), Peer.toOwnerId(dialog.getPeerId()), dialog.getInterlocutor());
        } else {
            this.openChat(dialog);
        }
    }

    private boolean canLoadMore() {
        return !cacheNowLoading && !endOfContent && !netLoadnigNow && !dialogs.isEmpty();
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    private Integer getLastDialogMessageId() {
        try {
            return dialogs.get(dialogs.size() - 1).getLastMessageId();
        } catch (Exception e) {
            return null;
        }
    }

    public void fireNewGroupChatTitleEntered(List<User> users, String title) {
        final String targetTitle = safeIsEmpty(title) ? getTitleIfEmpty(users) : title;
        final int accountId = super.getAccountId();

        appendDisposable(messagesInteractor.createGroupChat(accountId, Utils.idsListOf(users), targetTitle)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(chatid -> onGroupChatCreated(chatid, targetTitle), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onGroupChatCreated(int chatId, String title) {
        callView(view -> view.goToChat(getAccountId(), dialogsOwnerId, Peer.fromChatId(chatId), title, null));
    }

    public void fireUsersForChatSelected(@NonNull ArrayList<User> users) {
        if (users.size() == 1) {
            User user = users.get(0);
            // Post?
            getView().goToChat(getAccountId(), dialogsOwnerId, Peer.fromUserId(user.getId()), user.getFullName(), user.getMaxSquareAvatar());
        } else if (users.size() > 1) {
            getView().showEnterNewGroupChatTitle(users);
        }
    }

    private static String getTitleIfEmpty(@NonNull Collection<User> users) {
        return Utils.join(users, ", ", User::getFirstName);
    }

    public void fireRemoveDialogClick(Dialog dialog) {
        removeDialog(dialog.getPeerId());
    }

    public void fireCreateShortcutClick(Dialog dialog) {
        AssertUtils.assertPositive(dialogsOwnerId);

        final Context app = getApplicationContext();

        appendDisposable(ShortcutUtils
                .createChatShortcutRx(app, dialog.getImageUrl(), getAccountId(),
                        dialog.getPeerId(), dialog.getDisplayTitle(app))
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onShortcutCreated, throwable -> safeShowError(getView(), throwable.getMessage())));
    }

    private void onShortcutCreated() {
        if (isGuiReady()) {
            getView().showSnackbar(R.string.success, true);
        }
    }

    public void fireNotificationsSettingsClick(Dialog dialog) {
        AssertUtils.assertPositive(dialogsOwnerId);
        getView().showNotificationSettings(getAccountId(), dialog.getPeerId());
    }

    @Override
    protected void afterAccountChange(int oldAid, int newAid) {
        super.afterAccountChange(oldAid, newAid);

        // если на экране диалоги группы, то ничего не трогаем
        if (dialogsOwnerId < 0 && dialogsOwnerId != ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        dialogsOwnerId = newAid;

        cacheLoadingDisposable.clear();
        cacheNowLoading = false;

        netDisposable.clear();
        netLoadnigNow = false;

        loadCachedThenActualData();

        longpollManager.forceDestroy(oldAid);
        checkLongpoll();
    }

    public void fireAddToLauncherShortcuts(Dialog dialog) {
        AssertUtils.assertPositive(dialogsOwnerId);

        Peer peer = new Peer(dialog.getId())
                .setAvaUrl(dialog.getImageUrl())
                .setTitle(dialog.getDisplayTitle(getApplicationContext()));

        Completable completable = ShortcutUtils.addDynamicShortcut(getApplicationContext(), dialogsOwnerId, peer);

        appendDisposable(completable
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> safeShowToast(getView(), R.string.success, false), Analytics::logUnexpectedError));
    }

    public void fireContextViewCreated(IDialogsView.IContextView contextView) {
        contextView.setCanDelete(true);
        contextView.setCanAddToHomescreen(dialogsOwnerId > 0);
        contextView.setCanAddToShortcuts(dialogsOwnerId > 0);
        contextView.setCanConfigNotifications(dialogsOwnerId > 0);
    }

    public void fireOptionViewCreated(IDialogsView.IOptionView view) {
        view.setCanSearch(dialogsOwnerId > 0);
    }
}