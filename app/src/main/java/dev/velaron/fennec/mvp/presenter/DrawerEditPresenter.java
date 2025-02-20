package dev.velaron.fennec.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.mvp.core.AbsPresenter;
import dev.velaron.fennec.R;
import dev.velaron.fennec.model.DrawerCategory;
import dev.velaron.fennec.model.SwitchableCategory;
import dev.velaron.fennec.mvp.view.IDrawerEditView;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.Settings;

/**
 * Created by Ruslan Kolbasa on 20.07.2017.
 * phoenix
 */
public class DrawerEditPresenter extends AbsPresenter<IDrawerEditView> {

    private final List<DrawerCategory> data;

    public DrawerEditPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.data = createInitialData();
    }

    @StringRes
    private static int getTitleResCategory(@SwitchableCategory int type) {
        switch (type) {
            case SwitchableCategory.FRIENDS:
                return R.string.friends;
            case SwitchableCategory.DIALOGS:
                return R.string.dialogs;
            case SwitchableCategory.FEED:
                return R.string.feed;
            case SwitchableCategory.FEEDBACK:
                return R.string.drawer_feedback;
            case SwitchableCategory.NEWSFEED_COMMENTS:
                return R.string.drawer_newsfeed_comments;
            case SwitchableCategory.GROUPS:
                return R.string.groups;
            case SwitchableCategory.PHOTOS:
                return R.string.photos;
            case SwitchableCategory.VIDEOS:
                return R.string.videos;
            case SwitchableCategory.MUSIC:
                return R.string.music;
            case SwitchableCategory.DOCS:
                return R.string.documents;
            case SwitchableCategory.BOOKMARKS:
                return R.string.bookmarks;
            case SwitchableCategory.SEARCH:
                return R.string.search;
        }

        throw new IllegalArgumentException();
    }

    private ArrayList<DrawerCategory> createInitialData() {
        ArrayList<DrawerCategory> categories = new ArrayList<>();

        ISettings.IDrawerSettings settings = Settings.get().drawerSettings();

        @SwitchableCategory
        int[] items = settings.getCategoriesOrder();

        for(int category : items){
            DrawerCategory c = new DrawerCategory(category, getTitleResCategory(category));
            c.setChecked(settings.isCategoryEnabled(category));
            categories.add(c);
        }

        return categories;
    }

    @Override
    public void onGuiCreated(@NonNull IDrawerEditView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(data);
    }

    private void save() {
        @SwitchableCategory
        int[] postions = new int[data.size()];
        boolean[] active = new boolean[data.size()];

        for (int i = 0; i < data.size(); i++) {
            DrawerCategory category = data.get(i);

            postions[i] = category.getKey();
            active[i] = category.isChecked();
        }

        Settings.get().drawerSettings().setCategoriesOrder(postions, active);
    }

    public void fireSaveClick() {
        save();
        getView().goBackAndApplyChanges();
    }

    public void fireItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
    }
}