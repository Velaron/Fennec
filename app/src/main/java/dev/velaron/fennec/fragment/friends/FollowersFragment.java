package dev.velaron.fennec.fragment.friends;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.fragment.AbsOwnersListFragment;
import dev.velaron.fennec.mvp.presenter.FollowersPresenter;
import dev.velaron.fennec.mvp.view.ISimpleOwnersView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class FollowersFragment extends AbsOwnersListFragment<FollowersPresenter, ISimpleOwnersView> implements ISimpleOwnersView {

    public static FollowersFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        FollowersFragment followersFragment = new FollowersFragment();
        followersFragment.setArguments(args);
        return followersFragment;
    }

    @Override
    public IPresenterFactory<FollowersPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FollowersPresenter(getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.USER_ID),
                saveInstanceState);
    }
}