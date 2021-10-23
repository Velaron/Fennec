package dev.velaron.fennec.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.vkdatabase.SchoolClassesAdapter;
import dev.velaron.fennec.dialog.base.AccountDependencyDialogFragment;
import dev.velaron.fennec.domain.IDatabaseInteractor;
import dev.velaron.fennec.domain.InteractorFactory;
import dev.velaron.fennec.model.database.SchoolClazz;
import dev.velaron.fennec.util.RxUtils;

public class SelectSchoolClassesDialog extends AccountDependencyDialogFragment implements SchoolClassesAdapter.Listener {

    public static SelectSchoolClassesDialog newInstance(int aid, int countryId, Bundle additional){
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectSchoolClassesDialog selectCityDialog = new SelectSchoolClassesDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    private int mAccountId;
    private int countryId;
    private IDatabaseInteractor mDatabaseInteractor;

    private ArrayList<SchoolClazz> mData;
    private RecyclerView mRecyclerView;
    private SchoolClassesAdapter mAdapter;

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

        View root = inflater.inflate(R.layout.dialog_simple_recycler_view, container, false);
        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean firstRun = false;
        if(mData == null){
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new SchoolClassesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if(firstRun){
            request();
        }
    }

    private void request(){
        appendDisposable(mDatabaseInteractor.getSchoolClasses(mAccountId, countryId)
        .compose(RxUtils.applySingleIOToMainSchedulers())
        .subscribe(this::onDataReceived, t -> {/*todo*/}));
    }

    private void onDataReceived(List<SchoolClazz> clazzes){
        mData.clear();
        mData.addAll(clazzes);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(SchoolClazz schoolClazz) {
        Intent intent = new Intent();
        intent.putExtra(Extra.SCHOOL_CLASS, schoolClazz);
        intent.putExtra(Extra.ID, schoolClazz.getId());
        intent.putExtra(Extra.TITLE, schoolClazz.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}