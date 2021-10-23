package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.LastReadId;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by ruslan.kolbasa on 03.10.2016.
 * phoenix
 */
public interface IBasicMessageListView extends IMvpView, IAttachmentsPlacesView, IAccountDependencyView, IToastView {
    void notifyMessagesUpAdded(int position, int count);
    void notifyDataChanged();
    void notifyMessagesDownAdded(int count);

    void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin);
    void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin);
    void disableVoicePlaying();

    void showActionMode(String title, Boolean canEdit, Boolean canPin);
    void finishActionMode();

    void displayMessages(@NonNull List<Message> mData, @NonNull LastReadId lastReadId);
}