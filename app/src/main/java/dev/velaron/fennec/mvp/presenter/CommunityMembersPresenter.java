package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import dev.velaron.fennec.mvp.presenter.base.AccountDependencyPresenter;
import dev.velaron.fennec.mvp.view.ICommunityMembersView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityMembersPresenter extends AccountDependencyPresenter<ICommunityMembersView> {

    private final int groupId;

    public CommunityMembersPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
    }
}