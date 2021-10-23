package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.velaron.fennec.model.LoadMoreState;
import dev.velaron.fennec.model.Message;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public interface IMessagesLookView extends IBasicMessageListView, IErrorView {

    void focusTo(int index);
    void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState);

    void forwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void showDeleteForAllDialog(ArrayList<Integer> ids);
}
