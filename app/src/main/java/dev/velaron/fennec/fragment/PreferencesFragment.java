package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.EnterPinActivity;
import dev.velaron.fennec.activity.ProxyManagerActivity;
import dev.velaron.fennec.api.Apis;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.api.interfaces.IAccountApis;
import dev.velaron.fennec.api.model.AttachmentsTokenCreator;
import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.api.model.LinkAttachmentToken;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPhotoAlbum;
import dev.velaron.fennec.link.LinkHelper;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.SwitchableCategory;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.service.KeepLongpollService;
import dev.velaron.fennec.settings.AppPrefs;
import dev.velaron.fennec.settings.AvatarStyle;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.NightMode;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.MaskTransformation;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.RoundTransformation;
import dev.velaron.fennec.util.Utils;

import static dev.velaron.fennec.util.Utils.isEmpty;
import static dev.velaron.fennec.util.Utils.safelyClose;
import static dev.velaron.fennec.util.Utils.safelyRecycle;

public class PreferencesFragment extends PreferenceFragmentCompat {

    public static final int APP_GROUP_ID = 72124992;

    public static final String FULL_APP_URL = "https://play.google.com/store/apps/details?id=biz.dealnote.phoenix";
    private static final String ALBUM_NAME = "Phoenix";
    private static final String APP_URL = "https://play.google.com/store/apps/details?id=dev.velaron.fennec";

    public static final String KEY_DEFAULT_CATEGORY = "default_category";
    public static final String KEY_AVATAR_STYLE = "avatar_style";
    private static final String KEY_APP_THEME = "app_theme";
    private static final String KEY_NIGHT_SWITCH = "night_switch";
    private static final String KEY_TALK_ABOUT = "talk_about";
    private static final String KEY_JOIN_APP_GROUP = "join_app_group";
    private static final String KEY_ADD_COMMENT = "add_comment";
    private static final String KEY_NOTIFICATION = "notifications";
    private static final String KEY_SECURITY = "security";
    private static final String KEY_DRAWER_ITEMS = "drawer_categories";

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    private static final int REQUEST_LIGHT_SIDEBAR_BACKGROUND = 117;
    private static final int REQUEST_DARK_SIDEBAR_BACKGROUND = 118;
    private static final int REQUEST_PIN_FOR_SECURITY = 120;

    public static Bundle buildArgs(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static PreferencesFragment newInstance(Bundle args) {
        PreferencesFragment fragment = new PreferencesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        if (!AppPrefs.isFullApp()) {
            disableOnlyFullAppPrefs();
        }

        if (!AppPrefs.FULL_APP) {
            PreferenceScreen screen = this.getPreferenceScreen();
            Preference getFullApp = new Preference(requireActivity());
            getFullApp.setTitle(R.string.get_full_app_title);
            getFullApp.setSummary(R.string.get_full_app_summary);
            getFullApp.setOnPreferenceClickListener(preference -> {
                LinkHelper.openLinkInBrowser(getContext(), FULL_APP_URL);
                return true;
            });

            screen.addPreference(getFullApp);
        }

        final ListPreference nightPreference = findPreference(KEY_NIGHT_SWITCH);
        final ListPreference themePreference = findPreference(KEY_APP_THEME);
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            requireActivity().recreate();
            return true;
        });

        nightPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            switch (Integer.parseInt(newValue.toString())) {
                case NightMode.DISABLE:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case NightMode.ENABLE:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case NightMode.AUTO:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
                    break;
                case NightMode.FOLLOW_SYSTEM:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }

            requireActivity().recreate();
            return true;
        });

        ListPreference prefPhotoPreview = findPreference("photo_preview_size");
        prefPhotoPreview.setOnPreferenceChangeListener((preference, newValue) -> {
            Settings.get().main().notifyPrefPreviewSizeChanged();
            return true;
        });

        ListPreference defCategory = findPreference(KEY_DEFAULT_CATEGORY);
        initStartPagePreference(defCategory);

        Preference talkPreference = findPreference(KEY_TALK_ABOUT);
        if (talkPreference != null) {
            talkPreference.setOnPreferenceClickListener(preference -> {
                postWallImage();
                return false;
            });
        }

        Preference joinGroupPreference = findPreference(KEY_JOIN_APP_GROUP);
        if (joinGroupPreference != null) {
            joinGroupPreference.setOnPreferenceClickListener(preference -> {
                //joinToAppGroup();
                openAppCommunity();
                return false;
            });
        }

        Preference comment = findPreference(KEY_ADD_COMMENT);
        if (comment != null) {
            comment.setOnPreferenceClickListener(preference -> {
                LinkHelper.openLinkInBrowser(getContext(), AppPrefs.FULL_APP ? FULL_APP_URL : APP_URL);
                return false;
            });
        }

        Preference notification = findPreference(KEY_NOTIFICATION);
        if (notification != null) {
            notification.setOnPreferenceClickListener(preference -> {
                if (Utils.hasOreo()) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("android.provider.extra.APP_PACKAGE", requireContext().getPackageName());
                    requireContext().startActivity(intent);
                } else {
                    PlaceFactory.getNotificationSettingsPlace().tryOpenWith(requireActivity());
                }
                return true;
            });
        }

        Preference security = findPreference(KEY_SECURITY);
        if (Objects.nonNull(security)) {
            security.setOnPreferenceClickListener(preference -> {
                onSecurityClick();
                return true;
            });
        }

        Preference drawerCategories = findPreference(KEY_DRAWER_ITEMS);
        if (drawerCategories != null) {
            drawerCategories.setOnPreferenceClickListener(preference -> {
                PlaceFactory.getDrawerEditPlace().tryOpenWith(requireActivity());
                return true;
            });
        }

        Preference avatarStyle = findPreference(KEY_AVATAR_STYLE);
        if (avatarStyle != null) {
            avatarStyle.setOnPreferenceClickListener(preference -> {
                showAvatarStyleDialog();
                return true;
            });
        }

        Preference version = findPreference("version");
        if (version != null) {
            version.setSummary(Utils.getAppVersionName(requireActivity()));
            version.setOnPreferenceClickListener(preference -> {
                openAboutUs();
                return true;
            });
        }

        findPreference("privacy_policy")
                .setOnPreferenceClickListener(preference -> {
                    LinkHelper.openLinkInBrowser(getContext(), Constants.PRIVACY_POLICY_LINK);
                    return true;
                });

        findPreference("show_logs")
                .setOnPreferenceClickListener(preference -> {
                    PlaceFactory.getLogsPlace().tryOpenWith(requireActivity());
                    return true;
                });

        findPreference("request_executor")
                .setOnPreferenceClickListener(preference -> {
                    PlaceFactory.getRequestExecutorPlace(getAccountId()).tryOpenWith(requireActivity());
                    return true;
                });

        findPreference("blacklist")
                .setOnPreferenceClickListener(preference -> {
                    PlaceFactory.getUserBlackListPlace(getAccountId()).tryOpenWith(requireActivity());
                    return true;
                });

        findPreference("proxy")
                .setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(requireActivity(), ProxyManagerActivity.class));
                    return true;
                });

        findPreference("source_code")
                .setOnPreferenceClickListener(preference -> {
                    LinkHelper.openLinkInBrowser(getContext(), getString(R.string.source_code_link));
                    return true;
                });

        CheckBoxPreference keepLongpoll = findPreference("keep_longpoll");
        keepLongpoll.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean keep = (boolean) newValue;
            if(keep){
                KeepLongpollService.start(preference.getContext());
            } else {
                KeepLongpollService.stop(preference.getContext());
            }
            return true;
        });
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
    }

    public static File getDrawerBackgroundFile(Context context, boolean light) {
        return new File(context.getFilesDir(), light ? "drawer_light.jpg" : "drawer_dark.jpg");
    }

    private void disableOnlyFullAppPrefs() {
        String fullOnly = " FULL ONLY ";
        int color = Utils.adjustAlpha(CurrentTheme.getColorSecondary(requireActivity()), 100);

        for (String name : AppPrefs.ONLY_FULL_APP_PREFS) {
            Preference preference = findPreference(name);
            if (preference != null) {
                preference.setEnabled(false);

                CharSequence summary = TextUtils.isEmpty(preference.getTitle()) ? "" : preference.getTitle();
                summary = fullOnly + " " + summary;

                Spannable spannable = SpannableStringBuilder.valueOf(summary);

                BackgroundColorSpan span = new BackgroundColorSpan(color);
                ForegroundColorSpan span1 = new ForegroundColorSpan(Color.WHITE);

                spannable.setSpan(span, 0, fullOnly.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(span1, 0, fullOnly.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                preference.setTitle(spannable);
            }
        }
    }

    private void onSecurityClick() {
        if (Settings.get().security().isUsePinForSecurity()) {
            startActivityForResult(new Intent(requireActivity(), EnterPinActivity.class), REQUEST_PIN_FOR_SECURITY);
        } else {
            PlaceFactory.getSecuritySettingsPlace().tryOpenWith(requireActivity());
        }
    }

    private void tryDeleteFile(@NonNull File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("Can't delete file " + file);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_DARK_SIDEBAR_BACKGROUND || requestCode == REQUEST_LIGHT_SIDEBAR_BACKGROUND)
                && resultCode == Activity.RESULT_OK && data != null) {
            changeDrawerBackground(requestCode, data);
            requireActivity().recreate();
        }

        if (requestCode == REQUEST_PIN_FOR_SECURITY && resultCode == Activity.RESULT_OK) {
            PlaceFactory.getSecuritySettingsPlace().tryOpenWith(requireActivity());
        }
    }

    private void changeDrawerBackground(int requestCode, Intent data) {
        ArrayList<LocalPhoto> photos = data.getParcelableArrayListExtra(Extra.PHOTOS);
        if (isEmpty(photos)) {
            return;
        }

        LocalPhoto photo = photos.get(0);

        boolean light = requestCode == REQUEST_LIGHT_SIDEBAR_BACKGROUND;

        File file = getDrawerBackgroundFile(requireActivity(), light);

        Bitmap original = null;
        Bitmap scaled = null;
        FileOutputStream fos = null;

        try {
            original = BitmapFactory.decodeFile(photo.getFullImageUri().getPath());
            scaled = getResizedBitmap(original, 600);

            tryDeleteFile(file);

            fos = new FileOutputStream(file);

            scaled.compress(Bitmap.CompressFormat.JPEG, 95, fos);

            fos.flush();
        } catch (IOException e) {
            Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        } finally {
            safelyRecycle(original);
            safelyRecycle(scaled);
            safelyClose(fos);
        }

        PicassoInstance.with().invalidate(file);
    }

    private Bitmap getResizedBitmap(Bitmap bm, int maxSize) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(0, 0, width, height), new RectF(0, 0, maxSize, maxSize), Matrix.ScaleToFit.CENTER);

        // RESIZE THE BIT MAP
        //matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        //java.lang.IllegalStateException: Can't compress a recycled bitmap
        if (bm != resizedBitmap) {
            bm.recycle();
        }

        return resizedBitmap;
    }

    private void openAboutUs() {
        View view = View.inflate(requireActivity(), R.layout.dialog_about_us, null);
        new MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .show();
    }

    private void resolveAvatarStyleViews(int style, ImageView circle, ImageView oval) {
        switch (style) {
            case AvatarStyle.CIRCLE:
                circle.setVisibility(View.VISIBLE);
                oval.setVisibility(View.INVISIBLE);
                break;
            case AvatarStyle.OVAL:
                circle.setVisibility(View.INVISIBLE);
                oval.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showAvatarStyleDialog() {
        int current = Settings.get()
                .ui()
                .getAvatarStyle();

        View view = View.inflate(requireActivity(), R.layout.dialog_avatar_style, null);
        ImageView ivCircle = view.findViewById(R.id.circle_avatar);
        ImageView ivOval = view.findViewById(R.id.oval_avatar);
        final ImageView ivCircleSelected = view.findViewById(R.id.circle_avatar_selected);
        final ImageView ivOvalSelected = view.findViewById(R.id.oval_avatar_selected);

        ivCircle.setOnClickListener(v -> resolveAvatarStyleViews(AvatarStyle.CIRCLE, ivCircleSelected, ivOvalSelected));
        ivOval.setOnClickListener(v -> resolveAvatarStyleViews(AvatarStyle.OVAL, ivCircleSelected, ivOvalSelected));

        resolveAvatarStyleViews(current, ivCircleSelected, ivOvalSelected);

        PicassoInstance.with()
                .load(R.drawable.cat)
                .transform(new RoundTransformation())
                .into(ivCircle);

        PicassoInstance.with()
                .load(R.drawable.cat)
                .transform(new MaskTransformation(requireActivity(), R.drawable.avatar_mask))
                .into(ivOval);

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.avatar_style_title)
                .setView(view)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    boolean circle = ivCircleSelected.getVisibility() == View.VISIBLE;
                    Settings.get()
                            .ui()
                            .storeAvatarStyle(circle ? AvatarStyle.CIRCLE : AvatarStyle.OVAL);
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private int getAccountId() {
        return requireArguments().getInt(Extra.ACCOUNT_ID);
    }

    private void openAppCommunity() {
        PlaceFactory.getOwnerWallPlace(getAccountId(), -APP_GROUP_ID, null).tryOpenWith(requireActivity());
    }

    private void postWallImage() {
        final int ownerId = -APP_GROUP_ID;

        final int accountId = Settings.get()
                .accounts()
                .getCurrent();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    postWallImageSync(accountId, ownerId);
                } catch (Exception e) {
                    return e.getMessage();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (isAdded()) {
                    Toast.makeText(requireActivity(), s == null ? getString(R.string.thank_you) : s, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void postWallImageSync(int accountId, int ownerId) {
        IAccountApis apies = Apis.get()
                .vkDefault(accountId);

        List<VKApiPhotoAlbum> albums = apies.photos()
                .getAlbums(ownerId, null, null, null, true, false)
                .blockingGet()
                .getItems();

        for (VKApiPhotoAlbum album : albums) {
            if (ALBUM_NAME.equalsIgnoreCase(album.title)) {
                List<VKApiPhoto> photos = apies.photos()
                        .get(ownerId, String.valueOf(album.id), null, null, null, 1)
                        .blockingGet()
                        .getItems();

                if (photos.size() > 0) {
                    VKApiPhoto photo = photos.get(0);

                    Collection<IAttachmentToken> tokens = new ArrayList<>();
                    tokens.add(AttachmentsTokenCreator.ofPhoto(photo.id, photo.owner_id, photo.access_key));
                    tokens.add(new LinkAttachmentToken(APP_URL));

                    String message = getString(R.string.app_name) + " #phoenixvk";

                    apies.wall()
                            .post(accountId, null, null, message, tokens, null, null,
                                    null, null, null, null, null, null, null, null)
                            .blockingGet();

                    apies.likes()
                            .add("photo", ownerId, photo.id, photo.access_key)
                            .blockingGet();
                }
            }
        }
    }

    private void initStartPagePreference(ListPreference lp) {
        ISettings.IDrawerSettings drawerSettings = Settings.get()
                .drawerSettings();

        ArrayList<String> enabledCategoriesName = new ArrayList<>();
        ArrayList<String> enabledCategoriesValues = new ArrayList<>();

        enabledCategoriesName.add(getString(R.string.last_closed_page));
        enabledCategoriesValues.add("last_closed");

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.FRIENDS)) {
            enabledCategoriesName.add(getString(R.string.friends));
            enabledCategoriesValues.add("1");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.DIALOGS)) {
            enabledCategoriesName.add(getString(R.string.dialogs));
            enabledCategoriesValues.add("2");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.FEED)) {
            enabledCategoriesName.add(getString(R.string.feed));
            enabledCategoriesValues.add("3");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.FEEDBACK)) {
            enabledCategoriesName.add(getString(R.string.drawer_feedback));
            enabledCategoriesValues.add("4");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.NEWSFEED_COMMENTS)) {
            enabledCategoriesName.add(getString(R.string.drawer_newsfeed_comments));
            enabledCategoriesValues.add("12");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.GROUPS)) {
            enabledCategoriesName.add(getString(R.string.groups));
            enabledCategoriesValues.add("5");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.PHOTOS)) {
            enabledCategoriesName.add(getString(R.string.photos));
            enabledCategoriesValues.add("6");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.VIDEOS)) {
            enabledCategoriesName.add(getString(R.string.videos));
            enabledCategoriesValues.add("7");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.MUSIC)) {
            enabledCategoriesName.add(getString(R.string.music));
            enabledCategoriesValues.add("8");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.DOCS)) {
            enabledCategoriesName.add(getString(R.string.attachment_documents));
            enabledCategoriesValues.add("9");
        }

        if (drawerSettings.isCategoryEnabled(SwitchableCategory.BOOKMARKS)) {
            enabledCategoriesName.add(getString(R.string.bookmarks));
            enabledCategoriesValues.add("10");
        }

        lp.setEntries(enabledCategoriesName.toArray(new CharSequence[enabledCategoriesName.size()]));
        lp.setEntryValues(enabledCategoriesValues.toArray(new CharSequence[enabledCategoriesValues.size()]));
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.PREFERENCES);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_SETTINGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }
}