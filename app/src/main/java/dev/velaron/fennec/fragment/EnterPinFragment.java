package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.mvp.presenter.EnterPinPresenter;
import dev.velaron.fennec.mvp.view.IEnterPinView;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.view.KeyboardView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public class EnterPinFragment extends BaseMvpFragment<EnterPinPresenter, IEnterPinView>
        implements IEnterPinView, KeyboardView.OnKeyboardClickListener {

    public static EnterPinFragment newInstance(){
        Bundle bundle = new Bundle();
        EnterPinFragment fragment = new EnterPinFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ImageView mAvatar;
    private View mValuesRoot;
    private View[] mValuesCircles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_enter_pin, container, false);

        KeyboardView keyboardView = root.findViewById(R.id.keyboard);
        keyboardView.setOnKeyboardClickListener(this);

        mAvatar = root.findViewById(R.id.avatar);
        mValuesRoot = root.findViewById(R.id.value_root);

        mValuesCircles = new View[Constants.PIN_DIGITS_COUNT];
        mValuesCircles[0] = root.findViewById(R.id.pincode_digit_0_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[1] = root.findViewById(R.id.pincode_digit_1_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[2] = root.findViewById(R.id.pincode_digit_2_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[3] = root.findViewById(R.id.pincode_digit_3_root).findViewById(R.id.pincode_digit_circle);
        return root;
    }

    @Override
    public IPresenterFactory<EnterPinPresenter> getPresenterFactory(@Nullable final Bundle saveInstanceState) {
        return () -> new EnterPinPresenter(saveInstanceState);
    }

    @Override
    public void displayPin(int[] values, int noValueConstant) {
        if(Objects.isNull(mValuesCircles)) return;

        if(values.length != mValuesCircles.length){
            throw new IllegalStateException("Invalid pin length, view: " + mValuesCircles.length + ", target: " + values.length);
        }

        for(int i = 0; i < mValuesCircles.length; i++){
            boolean visible = values[i] != noValueConstant;
            mValuesCircles[i].setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void sendSuccessAndClose() {
        if(isAdded()){
            requireActivity().setResult(Activity.RESULT_OK);
            requireActivity().finish();
        }
    }

    @Override
    public void displayErrorAnimation() {
        if(Objects.nonNull(mValuesRoot)){
            Animation animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.anim_invalid_pin);
            mValuesRoot.startAnimation(animation);
        }
    }

    @Override
    public void displayAvatarFromUrl(@NonNull String url) {
        if(Objects.nonNull(mAvatar)){
            PicassoInstance.with()
                    .load(url)
                    .error(R.drawable.ic_avatar_unknown)
                    .transform(CurrentTheme.createTransformationForAvatar(requireActivity()))
                    .into(mAvatar);
        }
    }

    @Override
    public void displayDefaultAvatar() {
        if(Objects.nonNull(mAvatar)){
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(CurrentTheme.createTransformationForAvatar(requireActivity()))
                    .into(mAvatar);
        }
    }

    @Override
    public void onButtonClick(int number) {
        getPresenter().onNumberClicked(number);
    }

    @Override
    public void onBackspaceClick() {
        getPresenter().onBackspaceClicked();
    }

    @Override
    public void onFingerPrintClick() {
        getPresenter().onFingerprintClicked();
    }
}
