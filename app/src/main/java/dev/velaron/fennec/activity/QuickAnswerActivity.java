package dev.velaron.fennec.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Transformation;

import java.util.concurrent.TimeUnit;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.crypt.KeyLocationPolicy;
import dev.velaron.fennec.domain.IMessagesRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.listener.TextWatcherAdapter;
import dev.velaron.fennec.longpoll.NotificationHelper;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Peer;
import dev.velaron.fennec.model.SaveMessageBuilder;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.task.TextingNotifier;
import dev.velaron.fennec.util.AppTextUtils;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.RxUtils;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.ViewUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static dev.velaron.fennec.util.RxUtils.ignore;
import static dev.velaron.fennec.util.Utils.isEmpty;

public class QuickAnswerActivity extends AppCompatActivity {

    public static final String PARAM_BODY = "body";
    public static final String PARAM_MESSAGE_SENT_TIME = "message_sent_time";

    public static final String EXTRA_FOCUS_TO_FIELD = "focus_to_field";
    public static final String EXTRA_LIVE_DELAY = "live_delay";

    private EditText etText;
    private int peerId;
    private TextingNotifier notifier;
    private int accountId;
    private int messageId;

    private boolean messageIsRead;
    private IMessagesRepository messagesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get().ui().getMainTheme());

        this.messagesRepository = Repository.INSTANCE.getMessages();

        boolean focusToField = getIntent().getBooleanExtra(EXTRA_FOCUS_TO_FIELD, true);

        if (!focusToField) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        setTheme(R.style.QuickReply);

        messageId = getIntent().getExtras().getInt(Extra.MESSAGE_ID);
        accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
        peerId = getIntent().getExtras().getInt(Extra.PEER_ID);
        notifier = new TextingNotifier(accountId);

        setContentView(R.layout.activity_quick_answer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);
        }

        setSupportActionBar(toolbar);

        TextView tvMessage = findViewById(R.id.item_message_text);
        TextView tvTime = findViewById(R.id.item_message_time);
        etText = findViewById(R.id.activity_quick_answer_edit_text);

        ImageView ivAvatar = findViewById(R.id.avatar);

        ImageButton btnToDialog = findViewById(R.id.activity_quick_answer_to_dialog);
        ImageButton btnSend = findViewById(R.id.activity_quick_answer_send);

        String messageTime = AppTextUtils.getDateFromUnixTime(this, getIntent().getLongExtra(PARAM_MESSAGE_SENT_TIME, 0));
        final String title = getIntent().getStringExtra(Extra.TITLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        tvMessage.setText(getIntent().getStringExtra(PARAM_BODY), TextView.BufferType.SPANNABLE);
        tvTime.setText(messageTime);

        Transformation transformation = CurrentTheme.createTransformationForAvatar(this);
        final String imgUrl = getIntent().getStringExtra(Extra.IMAGE);
        if (ivAvatar != null) {
            ViewUtils.displayAvatar(ivAvatar, transformation, imgUrl, null);
        }

        etText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!messageIsRead) {
                    setMessageAsRead();
                    messageIsRead = true;
                }

                cancelFinishWithDelay();

                if (Objects.nonNull(notifier)) {
                    notifier.notifyAboutTyping(peerId);
                }
            }
        });

        btnSend.setOnClickListener(view -> send());
        btnToDialog.setOnClickListener(v -> {
            Intent intent = new Intent(QuickAnswerActivity.this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_OPEN_PLACE);

            Place chatPlace = PlaceFactory.getChatPlace(accountId, accountId, new Peer(peerId).setAvaUrl(imgUrl).setTitle(title));
            intent.putExtra(Extra.PLACE, chatPlace);
            startActivity(intent);
            finish();
        });

        boolean liveDelay = getIntent().getBooleanExtra(EXTRA_LIVE_DELAY, false);
        if (liveDelay) {
            finishWithDelay();
        }
    }

    private void finishWithDelay() {
        mLiveSubscription.add(Observable.just(new Object())
                .delay(1, TimeUnit.MINUTES)
                .subscribe(o -> finish()));
    }

    private CompositeDisposable mLiveSubscription = new CompositeDisposable();

    private void cancelFinishWithDelay() {
        mLiveSubscription.dispose();
    }

    @Override
    protected void onDestroy() {
        mLiveSubscription.dispose();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static Intent forStart(Context context, int accountId, int peerId, String body, int mid, long messageTime, String imgUrl, String title) {
        Intent intent = new Intent(context, QuickAnswerActivity.class);
        intent.putExtra(PARAM_BODY, body);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.MESSAGE_ID, mid);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.TITLE, title);
        intent.putExtra(PARAM_MESSAGE_SENT_TIME, messageTime);
        intent.putExtra(Extra.IMAGE, imgUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Отправка сообщения
     */
    private void send() {
        String trimmedtext = etText.getText().toString().trim();
        if (isEmpty(trimmedtext)) {
            Toast.makeText(QuickAnswerActivity.this, getString(R.string.text_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean requireEncryption = Settings.get()
                .security()
                .isMessageEncryptionEnabled(accountId, peerId);

        @KeyLocationPolicy
        int policy = KeyLocationPolicy.PERSIST;

        if (requireEncryption) {
            policy = Settings.get()
                    .security()
                    .getEncryptionLocationPolicy(accountId, peerId);
        }

        final SaveMessageBuilder builder = new SaveMessageBuilder(accountId, peerId)
                .setBody(trimmedtext)
                .setRequireEncryption(requireEncryption)
                .setKeyLocationPolicy(policy);

        compositeDisposable.add(messagesRepository.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onMessageSaved, this::onSavingError));
    }

    private void onSavingError(Throwable throwable) {
        Utils.showRedTopToast(this, throwable.toString());
    }

    @SuppressWarnings("unused")
    private void onMessageSaved(Message message) {
        NotificationHelper.tryCancelNotificationForPeer(this, accountId, peerId);
        messagesRepository.runSendingQueue();
        finish();
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private void setMessageAsRead() {
        compositeDisposable.add(messagesRepository.markAsRead(accountId, peerId, messageId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), ignore()));
    }
}