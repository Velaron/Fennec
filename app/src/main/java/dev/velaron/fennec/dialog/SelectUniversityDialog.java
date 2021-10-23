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
import dev.velaron.fennec.adapter.vkdatabase.UniversitiesAdapter;
import dev.velaron.fennec.dialog.base.AccountDependencyDialogFragment;
import dev.velaron.fennec.domain.IDatabaseInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.listener.TextWatcherAdapter;
import dev.velaron.fennec.model.database.University;
import dev.velaron.fennec.util.RxUtils;

public class SelectUniversityDialog extends AccountDependencyDialogFragment implements UniversitiesAdapter.Listener {

    private static final int COUNT_PER_REQUEST = 1000;
    private static final int RUN_SEACRH_DELAY = 1000;

    public static SelectUniversityDialog newInstance(int aid, int countryId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectUniversityDialog selectCityDialog = new SelectUniversityDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    private int mAccountId;
    private int countryId;
    private IDatabaseInteractor mDatabaseInteractor;

    private ArrayList<University> mData;
    private RecyclerView mRecyclerView;
    private UniversitiesAdapter mAdapter;
    private String filter;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.countryId = getArguments().getInt(Extra.COUNTRY_ID);
        this.mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
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
                mHandler.removeCallbacks(runSearchRunnable);
                mHandler.postDelayed(runSearchRunnable, RUN_SEACRH_DELAY);
            }
        });

        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        return root;
    }

    private Runnable runSearchRunnable = () -> request(0);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new UniversitiesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }
    }

    private void request(int offset) {
        appendDisposable(mDatabaseInteractor.getUniversities(mAccountId, filter, null, countryId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(universities -> onDataReceived(offset, universities), t -> {/*todo*/}));
    }

    private void onDataReceived(int offset, List<University> universities) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(universities);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runSearchRunnable);
    }

    @Override
    public void onClick(University university) {
        Intent intent = new Intent();
        intent.putExtra(Extra.UNIVERSITY, university);
        intent.putExtra(Extra.ID, university.getId());
        intent.putExtra(Extra.TITLE, university.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}