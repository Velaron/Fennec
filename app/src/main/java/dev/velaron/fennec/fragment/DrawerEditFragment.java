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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.DrawerCategotiesAdapter;
import dev.velaron.fennec.model.DrawerCategory;
import dev.velaron.fennec.mvp.presenter.DrawerEditPresenter;
import dev.velaron.fennec.mvp.view.IDrawerEditView;
import dev.velaron.fennec.util.Objects;
import biz.dealnote.mvp.compat.AbsMvpFragment;
import biz.dealnote.mvp.core.IPresenterFactory;

public class DrawerEditFragment extends AbsMvpFragment<DrawerEditPresenter, IDrawerEditView> implements IDrawerEditView {

    public static DrawerEditFragment newInstance() {
        Bundle args = new Bundle();
        DrawerEditFragment fragment = new DrawerEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private DrawerCategotiesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialog_drawers_categories, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder h1, RecyclerView.ViewHolder h2) {
                int fromPosition = h1.getAdapterPosition();
                int toPosition = h2.getAdapterPosition();

                getPresenter().fireItemMoved(fromPosition, toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        mAdapter = new DrawerCategotiesAdapter(Collections.emptyList());
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.drawer_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_save){
            getPresenter().fireSaveClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(R.string.drawer_edit_title);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public IPresenterFactory<DrawerEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new DrawerEditPresenter(saveInstanceState);
    }

    @Override
    public void displayData(List<DrawerCategory> data) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(data);
        }
    }

    @Override
    public void goBackAndApplyChanges() {
        requireActivity().onBackPressed();
    }
}