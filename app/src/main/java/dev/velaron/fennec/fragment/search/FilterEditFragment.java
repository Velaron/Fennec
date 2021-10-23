package dev.velaron.fennec.fragment.search;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.SearchOptionsAdapter;
import dev.velaron.fennec.dialog.SelectChairsDialog;
import dev.velaron.fennec.dialog.SelectCityDialog;
import dev.velaron.fennec.dialog.SelectCountryDialog;
import dev.velaron.fennec.dialog.SelectFacultyDialog;
import dev.velaron.fennec.dialog.SelectSchoolClassesDialog;
import dev.velaron.fennec.dialog.SelectSchoolsDialog;
import dev.velaron.fennec.dialog.SelectUniversityDialog;
import dev.velaron.fennec.fragment.search.options.BaseOption;
import dev.velaron.fennec.fragment.search.options.DatabaseOption;
import dev.velaron.fennec.fragment.search.options.SimpleBooleanOption;
import dev.velaron.fennec.fragment.search.options.SimpleNumberOption;
import dev.velaron.fennec.fragment.search.options.SimpleTextOption;
import dev.velaron.fennec.fragment.search.options.SpinnerOption;
import dev.velaron.fennec.util.InputTextDialog;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

public class FilterEditFragment extends BottomSheetDialogFragment implements SearchOptionsAdapter.OptionClickListener {

    private ArrayList<BaseOption> mData;
    private SearchOptionsAdapter mAdapter;

    public static FilterEditFragment newInstance(int accountId, ArrayList<BaseOption> options) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelableArrayList(Extra.LIST, options);
        FilterEditFragment fragment = new FilterEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private int mAccountId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.mData = getArguments().getParcelableArrayList(Extra.LIST);
    }

    private TextView mEmptyText;

    private void resolveEmptyTextVisibility(){
        if(Objects.nonNull(mEmptyText)){
            mEmptyText.setVisibility(Utils.isEmpty(mData) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View root = View.inflate(requireActivity(), R.layout.sheet_filter_edirt, null);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search_options);

        MenuItem saveItem = toolbar.getMenu().add(R.string.save);
        saveItem.setIcon(R.drawable.check);
        saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        saveItem.setOnMenuItemClickListener(menuItem -> {
            onSaveClick();
            return true;
        });

        mEmptyText = root.findViewById(R.id.empty_text);

        RecyclerView mRecyclerView = root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new SearchOptionsAdapter(mData);
        mAdapter.setOptionClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        resolveEmptyTextVisibility();

        dialog.setContentView(root);
    }

    private void onSaveClick(){
        Intent data = new Intent();
        data.putParcelableArrayListExtra(Extra.LIST, mData);

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        dismiss();
    }

    @Override
    public void onSpinnerOptionClick(final SpinnerOption spinnerOption) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(spinnerOption.title)
                .setItems(spinnerOption.createAvailableNames(requireActivity()), (dialog, which) -> {
                    spinnerOption.value = spinnerOption.available.get(which);
                    mAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    spinnerOption.value = null;
                    mAdapter.notifyDataSetChanged();
                })
                .setPositiveButton(R.string.button_cancel, null)
                .show();
    }

    private static final int REQUEST_CODE_COUTRY = 126;
    private static final int REQUEST_CODE_CITY = 127;
    private static final int REQUEST_CODE_UNIVERSITY = 128;
    private static final int REQUEST_CODE_FACULTY = 129;
    private static final int REQUEST_CODE_CHAIR = 130;
    private static final int REQUEST_CODE_SCHOOL = 131;
    private static final int REQUEST_CODE_SCHOOL_CLASS = 132;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_COUTRY:
            case REQUEST_CODE_CITY:
            case REQUEST_CODE_UNIVERSITY:
            case REQUEST_CODE_FACULTY:
            case REQUEST_CODE_CHAIR:
            case REQUEST_CODE_SCHOOL:
            case REQUEST_CODE_SCHOOL_CLASS:
                Bundle extras = data.getExtras();

                int key = extras.getInt(Extra.KEY);
                Integer id = extras.containsKey(Extra.ID) ? extras.getInt(Extra.ID) : null;
                String title = extras.containsKey(Extra.TITLE) ? extras.getString(Extra.TITLE) : null;

                mergeDatabaseOptionValue(key, id == null ? null : new DatabaseOption.Entry(id, title));
                break;
        }
    }

    private void mergeDatabaseOptionValue(int key, DatabaseOption.Entry value) {
        for (BaseOption option : mData) {
            if (option.key == key && option instanceof DatabaseOption) {
                DatabaseOption databaseOption = (DatabaseOption) option;
                databaseOption.value = value;
                resetChildDependensies(databaseOption.childDependencies);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void resetChildDependensies(int... childs) {
        if (childs != null) {
            boolean changed = false;
            for (int key : childs) {
                for (BaseOption option : mData) {
                    if (option.key == key) {
                        option.reset();
                        changed = true;
                    }
                }
            }

            if (changed) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDatabaseOptionClick(DatabaseOption databaseOption) {
        BaseOption dependency = findDependencyByKey(databaseOption.parentDependencyKey);

        switch (databaseOption.type) {
            case DatabaseOption.TYPE_COUNTRY:
                SelectCountryDialog selectCountryDialog = new SelectCountryDialog();
                selectCountryDialog.setTargetFragment(this, REQUEST_CODE_COUTRY);

                Bundle args = new Bundle();
                args.putInt(Extra.KEY, databaseOption.key);
                args.putInt(Extra.ACCOUNT_ID, mAccountId);
                selectCountryDialog.setArguments(args);
                selectCountryDialog.show(requireFragmentManager(), "countries");
                break;

            case DatabaseOption.TYPE_CITY:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showCitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_UNIVERSITY:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showUniversitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_FACULTY:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int universityId = ((DatabaseOption) dependency).value.id;
                    showFacultiesDialog(databaseOption, universityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_CHAIR:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int facultyId = ((DatabaseOption) dependency).value.id;
                    showChairsDialog(databaseOption, facultyId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int cityId = ((DatabaseOption) dependency).value.id;
                    showSchoolsDialog(databaseOption, cityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL_CLASS:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showSchoolClassesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onSimpleNumberOptionClick(final SimpleNumberOption option) {
        new InputTextDialog.Builder(requireActivity())
                .setTitleRes(option.title)
                .setAllowEmpty(true)
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setValue(option.value == null ? null : String.valueOf(option.value))
                .setCallback(newValue -> {
                    option.value = getIntFromEditable(newValue);
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private Integer getIntFromEditable(String line) {
        if (line == null || TextUtils.getTrimmedLength(line) == 0) {
            return null;
        }

        try {
            return Integer.valueOf(line);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void onSimpleTextOptionClick(final SimpleTextOption option) {
        new InputTextDialog.Builder(requireActivity())
                .setTitleRes(option.title)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setValue(option.value)
                .setAllowEmpty(true)
                .setCallback(newValue -> {
                    option.value = newValue;
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    @Override
    public void onSimpleBooleanOptionChanged(SimpleBooleanOption option) {

    }

    @Override
    public void onOptionCleared(BaseOption option) {
        resetChildDependensies(option.childDependencies);
    }

    private void showCitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectCityDialog selectCityDialog = SelectCityDialog.newInstance(mAccountId, countryId, args);
        selectCityDialog.setTargetFragment(this, REQUEST_CODE_CITY);
        selectCityDialog.show(requireFragmentManager(), "cities");
    }

    private void showUniversitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectUniversityDialog dialog = SelectUniversityDialog.newInstance(mAccountId, countryId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_UNIVERSITY);
        dialog.show(requireFragmentManager(), "universities");
    }

    private void showSchoolsDialog(DatabaseOption databaseOption, int cityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolsDialog dialog = SelectSchoolsDialog.newInstance(mAccountId, cityId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_SCHOOL);
        dialog.show(requireFragmentManager(), "schools");
    }

    private void showFacultiesDialog(DatabaseOption databaseOption, int universityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectFacultyDialog dialog = SelectFacultyDialog.newInstance(mAccountId, universityId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_FACULTY);
        dialog.show(requireFragmentManager(), "faculties");
    }

    private void showChairsDialog(DatabaseOption databaseOption, int facultyId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectChairsDialog dialog = SelectChairsDialog.newInstance(mAccountId, facultyId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_CHAIR);
        dialog.show(requireFragmentManager(), "chairs");
    }

    private void showSchoolClassesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolClassesDialog dialog = SelectSchoolClassesDialog.newInstance(mAccountId, countryId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_SCHOOL_CLASS);
        dialog.show(requireFragmentManager(), "school-classes");
    }

    private BaseOption findDependencyByKey(int key) {
        if (key == BaseOption.NO_DEPENDENCY) {
            return null;
        }

        for (BaseOption baseOption : mData) {
            if (baseOption.key == key) {
                return baseOption;
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}