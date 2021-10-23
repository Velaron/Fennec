package dev.velaron.fennec.fragment.friends;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.fragment.AbsOwnersListFragment;
import dev.velaron.fennec.mvp.presenter.MutualFriendsPresenter;
import dev.velaron.fennec.mvp.view.ISimpleOwnersView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class MutualFriendsFragment extends AbsOwnersListFragment<MutualFriendsPresenter, ISimpleOwnersView> implements ISimpleOwnersView {

    private static final String EXTRA_TARGET_ID = "targetId";

    public static MutualFriendsFragment newInstance(int accountId, int targetId){
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TARGET_ID, targetId);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        MutualFriendsFragment friendsFragment = new MutualFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @Override
    public IPresenterFactory<MutualFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new MutualFriendsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(EXTRA_TARGET_ID),
                saveInstanceState
        );
    }
}