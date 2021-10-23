package dev.velaron.fennec.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.mvp.presenter.CommunityMembersPresenter;
import dev.velaron.fennec.mvp.view.ICommunityMembersView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityControlMembersFragment extends BaseMvpFragment<CommunityMembersPresenter, ICommunityMembersView>
        implements ICommunityMembersView {

    public static CommunityControlMembersFragment newInstance(int accountId, int groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        CommunityControlMembersFragment fragment = new CommunityControlMembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public IPresenterFactory<CommunityMembersPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityMembersPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }
}