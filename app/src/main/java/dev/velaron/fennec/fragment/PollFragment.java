package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.PollAnswersAdapter;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.mvp.presenter.PollPresenter;
import dev.velaron.fennec.mvp.view.IPollView;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.view.ProgressButton;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 19.12.2016.
 * phoenix
 */
public class PollFragment extends BaseMvpFragment<PollPresenter, IPollView>
        implements IPollView, PollAnswersAdapter.OnAnswerChangedCallback {

    public static Bundle buildArgs(int aid, Poll poll){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.POLL, poll);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static PollFragment newInstance(Bundle bundle) {
        PollFragment fragment = new PollFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private TextView mQuestion;
    private TextView mVotesCount;
    private PollAnswersAdapter mAnswersAdapter;
    private ProgressButton mButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_poll, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAnswersAdapter = new PollAnswersAdapter(requireActivity(), Collections.emptyList());
        mAnswersAdapter.setListener(this);

        View header = LayoutInflater.from(requireActivity()).inflate(R.layout.header_poll, recyclerView, false);
        mAnswersAdapter.addHeader(header);

        mQuestion = header.findViewById(R.id.title);
        mVotesCount = header.findViewById(R.id.votes_count);

        mButton = root.findViewById(R.id.vote);
        mButton.setOnClickListener(view -> getPresenter().fireButtonClick());

        recyclerView.setAdapter(mAnswersAdapter);
        return root;
    }

    @Override
    public void displayQuestion(String title) {
        if(nonNull(mQuestion)){
            mQuestion.setText(title);
        }
    }

    @Override
    public void displayType(boolean anonymous) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(nonNull(actionBar)){
            actionBar.setTitle(anonymous ? R.string.anonymous_poll : R.string.open_poll);
        }
    }

    @Override
    public void displayCreationTime(long unixtime) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(nonNull(actionBar)){
            String formattedDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date(unixtime * 1000));
            actionBar.setSubtitle(formattedDate);
        }
    }

    @Override
    public void displayVoteCount(int count) {
        if(nonNull(mVotesCount)){
            mVotesCount.setText(getString(R.string.votes_count, count));
        }
    }

    @Override
    public void displayVotesList(List<Poll.Answer> answers, boolean canCheck, boolean multiply, Set<Integer> checked) {
        if(nonNull(mAnswersAdapter)){
            mAnswersAdapter.setData(answers, canCheck, multiply, checked);
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if(nonNull(mButton)){
            mButton.changeState(loading);
        }
    }

    @Override
    public void setupButton(boolean voted) {
        if(nonNull(mButton)){
            mButton.setText(getString(voted ? R.string.remove_vote : R.string.add_vote));
        }
    }

    @Override
    public IPresenterFactory<PollPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            Poll poll = requireArguments().getParcelable(Extra.POLL);
            AssertUtils.requireNonNull(poll);
            return new PollPresenter(aid, poll, saveInstanceState);
        };
    }

    @Override
    public void onAnswerChanged(Set<Integer> checked) {
        getPresenter().fireVoteChecked(checked);
    }
}