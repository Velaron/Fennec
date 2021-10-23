package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.ProxiesAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.mvp.presenter.ProxyManagerPresenter;
import dev.velaron.fennec.mvp.view.IProxyManagerView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public class ProxyManagerFragment extends BaseMvpFragment<ProxyManagerPresenter, IProxyManagerView>
        implements IProxyManagerView, ProxiesAdapter.ActionListener {

    public static ProxyManagerFragment newInstance() {
        Bundle args = new Bundle();
        ProxyManagerFragment fragment = new ProxyManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ProxiesAdapter mProxiesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_proxy_manager, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mProxiesAdapter = new ProxiesAdapter(Collections.emptyList(), this);
        recyclerView.setAdapter(mProxiesAdapter);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.proxies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            getPresenter().fireAddClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public IPresenterFactory<ProxyManagerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ProxyManagerPresenter(saveInstanceState);
    }

    @Override
    public void displayData(List<ProxyConfig> configs, ProxyConfig active) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.setData(configs, active);
        }
    }

    @Override
    public void notifyItemAdded(int position) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.notifyItemInserted(position + mProxiesAdapter.getHeadersCount());
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.notifyItemRemoved(position + mProxiesAdapter.getHeadersCount());
        }
    }

    @Override
    public void setActiveAndNotifyDataSetChanged(ProxyConfig config) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.setActive(config);
        }
    }

    @Override
    public void goToAddingScreen() {
        PlaceFactory.getProxyAddPlace().tryOpenWith(requireActivity());
    }

    @Override
    public void onDeleteClick(ProxyConfig config) {
        getPresenter().fireDeleteClick(config);
    }

    @Override
    public void onSetAtiveClick(ProxyConfig config) {
        getPresenter().fireActivateClick(config);
    }

    @Override
    public void onDisableClick(ProxyConfig config) {
        getPresenter().fireDisableClick(config);
    }
}