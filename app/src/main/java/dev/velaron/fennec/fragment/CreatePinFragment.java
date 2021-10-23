package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.BackPressCallback;
import dev.velaron.fennec.mvp.presenter.CreatePinPresenter;
import dev.velaron.fennec.mvp.view.ICreatePinView;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.view.KeyboardView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by ruslan.kolbasa on 10-Jun-16.
 * mobilebankingandroid
 */
public class CreatePinFragment extends BaseMvpFragment<CreatePinPresenter, ICreatePinView>
        implements ICreatePinView, KeyboardView.OnKeyboardClickListener, BackPressCallback {

    private static final String TAG = CreatePinFragment.class.getSimpleName();

    public static CreatePinFragment newInstance(){
        return new CreatePinFragment();
    }

    private TextView mTitle;
    private View mValuesRoot;
    private View[] mValuesCircles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_pin, container, false);

        KeyboardView keyboardView = root.findViewById(R.id.keyboard);
        keyboardView.setOnKeyboardClickListener(this);

        mTitle = root.findViewById(R.id.pin_title_text);

        mValuesRoot = root.findViewById(R.id.value_root);
        mValuesCircles = new View[Constants.PIN_DIGITS_COUNT];
        mValuesCircles[0] = root.findViewById(R.id.pincode_digit_0_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[1] = root.findViewById(R.id.pincode_digit_1_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[2] = root.findViewById(R.id.pincode_digit_2_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[3] = root.findViewById(R.id.pincode_digit_3_root).findViewById(R.id.pincode_digit_circle);
        return root;
    }

    @Override
    public void displayTitle(@StringRes int titleRes) {
        if(Objects.nonNull(mTitle)){
            mTitle.setText(titleRes);
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
    public void sendSkipAndClose() {
        requireActivity().setResult(Activity.RESULT_CANCELED);
        requireActivity().finish();
    }

    private static final String EXTRA_PIN_VALUE = "pin_value";

    @Override
    public void sendSuccessAndClose(int[] values) {
        Intent data = new Intent();
        data.putExtra(EXTRA_PIN_VALUE, values);
        requireActivity().setResult(Activity.RESULT_OK, data);
        requireActivity().finish();
    }

    public static int[] extractValueFromIntent(Intent intent){
        return intent.getIntArrayExtra(EXTRA_PIN_VALUE);
    }

    @Override
    public IPresenterFactory<CreatePinPresenter> getPresenterFactory(@Nullable final Bundle saveInstanceState) {
        return () -> new CreatePinPresenter(saveInstanceState);
    }

    @Override
    public void onButtonClick(int number) {
        if(isPresenterPrepared()) getPresenter().fireDigitClick(number);
    }

    @Override
    public void onBackspaceClick() {
        if(isPresenterPrepared()) getPresenter().fireBackspaceClick();
    }

    @Override
    public void onFingerPrintClick() {
        if(isPresenterPrepared()) getPresenter().fireFingerPrintClick();
    }

    @Override
    public boolean onBackPressed() {
        return isPresenterPrepared() && getPresenter().fireBackButtonClick();
    }
}
