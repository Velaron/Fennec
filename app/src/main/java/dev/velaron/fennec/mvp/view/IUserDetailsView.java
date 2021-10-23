package dev.velaron.fennec.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.menu.AdvancedItem;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public interface IUserDetailsView extends IMvpView, IAccountDependencyView, IErrorView {
    void displayData(@NonNull List<AdvancedItem> items);

    void displayToolbarTitle(String title);

    void openOwnerProfile(int accountId, int ownerId, @Nullable Owner owner);
}