package dev.velaron.fennec.adapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import dev.velaron.fennec.util.Objects;

public abstract class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<WeakReference<Fragment>> fragments;

    public MyFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>(3);
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment findFragmentByPosition(int position){
        WeakReference<Fragment> weak = fragments.get(position);
        return Objects.isNull(weak) ? null : weak.get();
    }
}
