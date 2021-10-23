package dev.velaron.fennec.dialog.base;

import androidx.fragment.app.DialogFragment;
import dev.velaron.fennec.util.ViewUtils;

public abstract class BaseDialogFragment extends DialogFragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewUtils.keyboardHide(requireActivity());
    }
}