package dev.velaron.fennec.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.RecyclerMenuAdapter;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.Icon;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.Text;
import dev.velaron.fennec.model.WallEditorAttrs;
import dev.velaron.fennec.model.menu.AdvancedItem;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

import static dev.velaron.fennec.util.Utils.firstNonEmptyString;

/**
 * Created by admin on 3/27/2018.
 * Phoenix-for-VK
 * Запускается при публикации изображений/текста из других приложений
 */
public class PostPublishPrepareActivity extends AppCompatActivity implements RecyclerMenuAdapter.ActionListener {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private RecyclerMenuAdapter adapter;
    private RecyclerView recyclerView;
    private View proressView;

    private ArrayList<Uri> streams;
    private int accountId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get().ui().getMainTheme());
        setContentView(R.layout.activity_post_publish_prepare);

        adapter = new RecyclerMenuAdapter(R.layout.item_advanced_menu_alternative, Collections.emptyList());
        adapter.setActionListener(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        proressView = findViewById(R.id.progress_view);

        if (Objects.isNull(savedInstanceState)) {
            accountId = Settings.get().accounts().getCurrent();

            if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
                Toast.makeText(this, R.string.error_post_creation_no_auth, Toast.LENGTH_LONG).show();
                finish();
            }

            streams = ActivityUtils.checkLocalStreams(this);

            setLoading(true);
            IOwnersRepository interactor = Repository.INSTANCE.getOwners();
            compositeDisposable.add(interactor.getCommunitiesWhereAdmin(accountId, true, true, false)
                    .zipWith(interactor.getBaseOwnerInfo(accountId, accountId, IOwnersRepository.MODE_NET), (owners, owner) -> {
                        List<Owner> result = new ArrayList<>();
                        result.add(owner);
                        result.addAll(owners);
                        return result;
                    })
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onOwnersReceived, this::onOwnersGetError));
        }

        updateViews();
    }

    private void onOwnersGetError(Throwable throwable) {
        setLoading(false);
        Toast.makeText(this, firstNonEmptyString(throwable.getMessage(), throwable.toString()), Toast.LENGTH_LONG).show();

        finish();
    }

    private void onOwnersReceived(List<Owner> owners) {
        setLoading(false);

        if (owners.isEmpty()) {
            finish(); // wtf???
            return;
        }

        final Owner iam = owners.get(0);

        List<AdvancedItem> items = new ArrayList<>();

        for (Owner owner : owners) {
            WallEditorAttrs attrs = new WallEditorAttrs(owner, iam);

            items.add(new AdvancedItem(owner.getOwnerId(), new Text(owner.getFullName()))
                    .setIcon(Icon.fromUrl(owner.get100photoOrSmaller()))
                    .setSubtitle(new Text("@" + owner.getDomain()))
                    .setTag(attrs));
        }

        adapter.setItems(items);
    }

    private boolean loading;

    private void setLoading(boolean loading) {
        this.loading = loading;
        updateViews();
    }

    private void updateViews() {
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
        proressView.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onClick(AdvancedItem item) {
        WallEditorAttrs attrs = (WallEditorAttrs) item.getTag();

        Intent intent = PostCreateActivity.newIntent(this, accountId, attrs, streams);
        startActivity(intent);

        finish();
    }

    @Override
    public void onLongClick(AdvancedItem item) {

    }
}