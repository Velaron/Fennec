package dev.velaron.fennec.fragment.sheet;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.velaron.fennec.mvp.compat.ViewHostDelegate;
import dev.velaron.fennec.mvp.core.IMvpView;
import dev.velaron.fennec.mvp.core.IPresenter;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public abstract class AbsPresenterBottomSheetFragment<P extends IPresenter<V>, V extends IMvpView>
        extends BottomSheetDialogFragment implements ViewHostDelegate.IFactoryProvider<P,V> {

    private final ViewHostDelegate<P, V> delegate = new ViewHostDelegate<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate.onCreate(requireActivity(), getPresenterViewHost(), this, getLoaderManager(), savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fireViewCreated();
    }

    public void fireViewCreated(){
        delegate.onViewCreated();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        delegate.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        delegate.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        delegate.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        delegate.onDestroyView();
    }

    @Override
    public void onDestroy() {
        delegate.onDestroy();
        super.onDestroy();
    }

    // Override in case of fragment not implementing IPresenter<View> interface
    @SuppressWarnings("unchecked")
    @NonNull
    protected V getPresenterViewHost() {
        return (V) this;
    }

    protected P getPresenter(){
        return delegate.getPresenter();
    }
}