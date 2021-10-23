package dev.velaron.fennec.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.vkdatabase.CitiesAdapter;
import dev.velaron.fennec.dialog.base.AccountDependencyDialogFragment;
import dev.velaron.fennec.domain.IDatabaseInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.listener.TextWatcherAdapter;
import dev.velaron.fennec.model.City;
import dev.velaron.fennec.util.RxUtils;

public class SelectCityDialog extends AccountDependencyDialogFragment implements CitiesAdapter.Listener {

    private static final int COUNT_PER_REQUEST = 1000;
    private static final int RUN_SEACRH_DELAY = 1000;

    public static SelectCityDialog newInstance(int aid, int countryId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectCityDialog selectCityDialog = new SelectCityDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    private int accountId;
    private int countryId;
    private ArrayList<City> mData;
    private RecyclerView mRecyclerView;
    private CitiesAdapter mAdapter;
    private String filter;
    private Handler mHandler = new Handler();
    private IDatabaseInteractor databaseInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.accountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.databaseInteractor = InteractorFactory.createDatabaseInteractor();
        this.countryId = getArguments().getInt(Extra.COUNTRY_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View root = inflater.inflate(R.layout.dialog_country_or_city_select, container, false);

        EditText input = root.findViewById(R.id.input);
        input.setText(filter);
        input.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                filter = s.toString();
                mHandler.removeCallbacks(mRunSearchRunnable);
                mHandler.postDelayed(mRunSearchRunnable, RUN_SEACRH_DELAY);
            }
        });

        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        return root;
    }

    private Runnable mRunSearchRunnable = () -> request(0);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new CitiesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }
    }

    private void request(int offset) {
        appendDisposable(databaseInteractor.getCities(accountId, countryId, filter, true, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(cities -> onRequestFinised(cities, offset), this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        // TODO: 04.10.2017
    }

    private void onRequestFinised(List<City> cities, int offset) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(cities);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunSearchRunnable);
    }

    @Override
    public void onClick(City city) {
        Intent intent = new Intent();
        intent.putExtra(Extra.CITY, city);
        intent.putExtra(Extra.ID, city.getId());
        intent.putExtra(Extra.TITLE, city.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}