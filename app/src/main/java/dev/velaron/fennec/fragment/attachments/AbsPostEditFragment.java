package dev.velaron.fennec.fragment.attachments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.mvp.presenter.AbsPostEditPresenter;
import dev.velaron.fennec.mvp.view.IBasePostEditView;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.RoundTransformation;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 16.05.2017.
 * phoenix
 */
public abstract class AbsPostEditFragment<P extends AbsPostEditPresenter<V>, V extends IBasePostEditView>
        extends AbsAttachmentsEditFragment<P,V> implements IBasePostEditView {

    private CheckBox mFromGroupCheckBox;

    private CheckBox mFrindsOnlyCheckBox;

    private View mSignerRoot;
    private ImageView mSignerAvatar;
    private TextView mSignerName;
    private CheckBox mShowAuthorCheckbox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        AssertUtils.requireNonNull(root);

        View signatureRoot = inflater.inflate(R.layout.content_post_edit_under_body, getUnderBodyContainer(), false);

        mFromGroupCheckBox = signatureRoot.findViewById(R.id.check_from_group);
        mFromGroupCheckBox.setOnCheckedChangeListener((buttonView, checked) -> getPresenter().fireFromGroupChecked(checked));

        mFrindsOnlyCheckBox = signatureRoot.findViewById(R.id.check_friends_only);
        mFrindsOnlyCheckBox.setOnCheckedChangeListener((buttonView, checked) -> getPresenter().fireFriendsOnlyCheched(checked));

        mSignerRoot = signatureRoot.findViewById(R.id.signer_root);
        mSignerAvatar = signatureRoot.findViewById(R.id.signer_avatar);
        mSignerName = signatureRoot.findViewById(R.id.signer_name);

        mShowAuthorCheckbox = signatureRoot.findViewById(R.id.check_show_author);
        mShowAuthorCheckbox.setOnCheckedChangeListener((buttonView, checked) -> getPresenter().fireShowAuthorChecked(checked));

        getUnderBodyContainer().addView(signatureRoot);
        return root;
    }

    @Override
    public void setFromGroupChecked(boolean checked) {
        if(nonNull(mFromGroupCheckBox)){
            mFromGroupCheckBox.setChecked(checked);
        }
    }

    @Override
    public void setFriendsOnlyCheched(boolean cheched) {
        if(nonNull(mFrindsOnlyCheckBox)){
            mFrindsOnlyCheckBox.setChecked(cheched);
        }
    }

    @Override
    public void setFriendsOnlyOptionVisible(boolean visible) {
        if(nonNull(mFrindsOnlyCheckBox)){
            mFrindsOnlyCheckBox.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setFromGroupOptionVisible(boolean visible) {
        if(nonNull(mFromGroupCheckBox)){
            mFromGroupCheckBox.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displaySignerInfo(String fullName, String photo) {
        if(nonNull(mSignerAvatar)){
            PicassoInstance.with()
                    .load(photo)
                    .transform(new RoundTransformation())
                    .into(mSignerAvatar);
        }

        if(nonNull(mSignerName)){
            mSignerName.setText(fullName);
        }
    }

    @Override
    public void setAddSignatureOptionVisible(boolean visible) {
        if(nonNull(mShowAuthorCheckbox)){
            mShowAuthorCheckbox.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setShowAuthorChecked(boolean checked) {
        if(nonNull(mShowAuthorCheckbox)){
            mShowAuthorCheckbox.setChecked(checked);
        }
    }

    @Override
    public void setSignerInfoVisible(boolean isChecked) {
        if(nonNull(mSignerRoot)){
            mSignerRoot.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }
}