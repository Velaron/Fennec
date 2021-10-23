package dev.velaron.fennec.fragment.friends;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.fragment.AbsOwnersListFragment;
import dev.velaron.fennec.mvp.presenter.OnlineFriendsPresenter;
import dev.velaron.fennec.mvp.view.ISimpleOwnersView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class OnlineFriendsFragment extends AbsOwnersListFragment<OnlineFriendsPresenter, ISimpleOwnersView> implements ISimpleOwnersView {

    public static OnlineFriendsFragment newInstance(int accoutnId, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.USER_ID, userId);
        bundle.putInt(Extra.ACCOUNT_ID, accoutnId);
        OnlineFriendsFragment friendsFragment = new OnlineFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @Override
    public IPresenterFactory<OnlineFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new OnlineFriendsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.USER_ID),
                saveInstanceState);
    }
}