package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.mvp.view.IFwdsView;

/**
 * Created by r.kolbasa on 18.12.2017.
 * Phoenix-for-VK
 */
public class FwdsPresenter extends AbsMessageListPresenter<IFwdsView> {

    public FwdsPresenter(int accountId, List<Message> messages, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        getData().addAll(messages);
    }
}