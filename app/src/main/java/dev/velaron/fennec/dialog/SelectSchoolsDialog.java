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
import dev.velaron.fennec.adapter.vkdatabase.SchoolsAdapter;
import dev.velaron.fennec.dialog.base.AccountDependencyDialogFragment;
import dev.velaron.fennec.domain.IDatabaseInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.listener.TextWatcherAdapter;
import dev.velaron.fennec.model.database.School;
import dev.velaron.fennec.util.RxUtils;

public class SelectSchoolsDialog extends AccountDependencyDialogFragment implements SchoolsAdapter.Listener {

    private static final int COUNT_PER_REQUEST = 1000;
    private static final int RUN_SEACRH_DELAY = 1000;

    public static SelectSchoolsDialog newInstance(int aid, int cityId, Bundle additional){
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.CITY_ID, cityId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectSchoolsDialog selectCityDialog = new SelectSchoolsDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    private int mAccountId;
    private int cityId;
    private IDatabaseInteractor mDatabaseInteractor;

    private ArrayList<School> mData;
    private RecyclerView mRecyclerView;
    private SchoolsAdapter mAdapter;
    private String filter;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.cityId = getArguments().getInt(Extra.CITY_ID);
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
        if(mData == null){
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new SchoolsAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if(firstRun){
            request(0);
        }
    }

    private void request(int offset){
        appendDisposable(mDatabaseInteractor.getSchools(mAccountId, cityId, filter, COUNT_PER_REQUEST, offset)
        .compose(RxUtils.applySingleIOToMainSchedulers())
        .subscribe(schools -> onDataReceived(offset, schools), t -> {/*todo*/}));
    }

    private void onDataReceived(int offset, List<School> schools){
        if(offset == 0){
            mData.clear();
        }

        mData.addAll(schools);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runSearchRunnable);
    }

    @Override
    public void onClick(School school) {
        Intent intent = new Intent();
        intent.putExtra(Extra.SCHOOL, school);
        intent.putExtra(Extra.ID, school.getId());
        intent.putExtra(Extra.TITLE, school.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}