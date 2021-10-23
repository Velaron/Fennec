package dev.velaron.fennec.mvp.view;

import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import dev.velaron.fennec.model.IdOption;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.mvp.view.base.IAccountDependencyView;

/**
 * Created by admin on 17.06.2017.
 * phoenix
 */
public interface ICommunityBanEditView extends IMvpView, IAccountDependencyView, IErrorView, IProgressView, IToastView {
    void displayUserInfo(Owner user);
    void displayBanStatus(int adminId, String adminName, long endDate);
    void displayBlockFor(String blockFor);
    void displayReason(String reason);

    void diplayComment(String comment);
    void setShowCommentChecked(boolean checked);

    void goBack();

    void displaySelectOptionDialog(int requestCode, List<IdOption> options);

    void openProfile(int accountId, Owner owner);
}