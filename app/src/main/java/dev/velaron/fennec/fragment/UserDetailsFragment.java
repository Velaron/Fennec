package dev.velaron.fennec.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.RecyclerMenuAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.User;
import dev.velaron.fennec.model.UserDetails;
import dev.velaron.fennec.model.menu.AdvancedItem;
import dev.velaron.fennec.mvp.presenter.UserDetailsPresenter;
import dev.velaron.fennec.mvp.view.IUserDetailsView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class UserDetailsFragment extends BaseMvpFragment<UserDetailsPresenter, IUserDetailsView> implements IUserDetailsView, RecyclerMenuAdapter.ActionListener {

    public static UserDetailsFragment newInstance(int accountId, @NonNull User user, @NonNull UserDetails details) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.USER, user);
        args.putParcelable("details", details);
        UserDetailsFragment fragment = new UserDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerMenuAdapter menuAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        menuAdapter = new RecyclerMenuAdapter(Collections.emptyList());
        menuAdapter.setActionListener(this);

        recyclerView.setAdapter(menuAdapter);
        return view;
    }

    @Override
    public void displayData(@NonNull List<AdvancedItem> items) {
        menuAdapter.setItems(items);
    }

    @Override
    public void displayToolbarTitle(String title) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void openOwnerProfile(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, owner).tryOpenWith(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public IPresenterFactory<UserDetailsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new UserDetailsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.USER),
                requireArguments().getParcelable("details"),
                saveInstanceState
        );
    }

    @Override
    public void onLongClick(AdvancedItem item) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            String title = item.getTitle().getText(requireContext());
            String subtitle = nonNull(item.getSubtitle()) ? item.getSubtitle().getText(requireContext()) : null;
            String details = Utils.joinNonEmptyStrings("\n", title, subtitle);

            ClipData clip = ClipData.newPlainText("Details", details);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(AdvancedItem item) {
        getPresenter().fireItemClick(item);
    }
}