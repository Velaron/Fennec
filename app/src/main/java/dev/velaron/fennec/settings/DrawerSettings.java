package dev.velaron.fennec.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dev.velaron.fennec.model.SwitchableCategory;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static dev.velaron.fennec.model.SwitchableCategory.BOOKMARKS;
import static dev.velaron.fennec.model.SwitchableCategory.DOCS;
import static dev.velaron.fennec.model.SwitchableCategory.FEEDBACK;
import static dev.velaron.fennec.model.SwitchableCategory.FRIENDS;
import static dev.velaron.fennec.model.SwitchableCategory.GROUPS;
import static dev.velaron.fennec.model.SwitchableCategory.MUSIC;
import static dev.velaron.fennec.model.SwitchableCategory.NEWSFEED_COMMENTS;
import static dev.velaron.fennec.model.SwitchableCategory.PHOTOS;
import static dev.velaron.fennec.model.SwitchableCategory.VIDEOS;
import static dev.velaron.fennec.util.Utils.isEmpty;

/**
 * Created by admin on 01.12.2016.
 * phoenix
 */
class DrawerSettings implements ISettings.IDrawerSettings {

    private final Context app;
    private final PublishSubject<Object> publishSubject;

    DrawerSettings(Context context) {
        this.app = context.getApplicationContext();
        this.publishSubject = PublishSubject.create();
    }

    @Override
    public boolean isCategoryEnabled(@SwitchableCategory int category) {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean(keyForCategory(category), true);
    }

    @Override
    public void setCategoriesOrder(@SwitchableCategory int[] order, boolean[] active) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);

        for (int i = 0; i < order.length; i++) {
            @SwitchableCategory
            int category = order[i];
            preferences.edit()
                    .putBoolean(keyForCategory(category), active[i])
                    .apply();
        }

        StringBuilder builder = new StringBuilder();
        for (int i : order) {
            builder.append(i).append("-");
        }

        preferences.edit()
                .putString("drawer_categories_order", builder.toString().trim())
                .apply();

        publishSubject.onNext(Void.class);
    }

    @Override
    public int[] getCategoriesOrder() {
        @SwitchableCategory
        int[] all = {FRIENDS, FEEDBACK, NEWSFEED_COMMENTS, GROUPS, PHOTOS, VIDEOS, MUSIC, DOCS, BOOKMARKS};
        String line = PreferenceManager.getDefaultSharedPreferences(app).getString("drawer_categories_order", null);

        String[] parts = isEmpty(line) ? new String[0] : line.split("-");

        int[] positions = new int[parts.length];

        try {
            for (int i = 0; i < parts.length; i++) {
                positions[i] = Integer.parseInt(parts[i]);
            }
        } catch (Exception ignored) {/*ignore*/}

        for (int i = 0; i < positions.length; i++) {
            int category = positions[i];
            // категория "category" должна быть в положении "i"

            if (all[i] != category) {
                try {
                    int currentCategoryIndex = findCategoryIndex(all, category);
                    all[currentCategoryIndex] = all[i];
                    all[i] = category;
                } catch (Exception ignored){/*ignore*/}
            }
        }

        return all;
    }

    @Override
    public Observable<Object> observeChanges() {
        return publishSubject;
    }

    private static int findCategoryIndex(int[] array, @SwitchableCategory int category) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == category) {
                return i;
            }
        }

        throw new IllegalStateException("Invalid category " + category);
    }

    private static String keyForCategory(@SwitchableCategory int category) {
        return "drawer_category_" + category;
    }
}