package dev.velaron.fennec.push;

import static dev.velaron.fennec.util.Utils.nonEmpty;
import static dev.velaron.fennec.util.Utils.safeIsEmpty;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.DrawableRes;

import com.squareup.picasso.Transformation;

import java.io.IOException;

import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.ImageHelper;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;

public class NotificationUtils {

    public static Single<Bitmap> loadRoundedImageRx(Context context, String url, @DrawableRes int ifErrorOrEmpty){
        final Context app = context.getApplicationContext();
        return Single.fromCallable(() -> loadRoundedImage(app, url, ifErrorOrEmpty));
    }

    public static Bitmap loadRoundedImage(Context context, String url, @DrawableRes int ifErrorOrEmpty) {
        final Context app = context.getApplicationContext();
        Transformation transformation = CurrentTheme.createTransformationForAvatar(app);

        int size = (int) Utils.dpToPx(64, app);

        if(nonEmpty(url)){
            try {
                return PicassoInstance.with()
                        .load(url)
                        .resize(size, size)
                        .centerCrop()
                        .transform(transformation)
                        .config(Bitmap.Config.RGB_565)
                        .get();
            } catch (IOException e) {
                return loadRoundedImageFromResources(app, ifErrorOrEmpty, transformation, size);
            }
        } else {
            return loadRoundedImageFromResources(app, ifErrorOrEmpty, transformation, size);
        }
    }

    private static Bitmap loadRoundedImageFromResources(Context context, @DrawableRes int res, Transformation transformation, int size) {
        try {
            return PicassoInstance.with()
                    .load(res)
                    .resize(size, size)
                    .transform(transformation)
                    .centerCrop()
                    .get();
        } catch (IOException e){
            e.printStackTrace();

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
            return ImageHelper.getRoundedBitmap(bitmap);
        }
    }

    public static int optInt(Bundle extras, String name, int defaultValue) {
        String value = extras.getString(name);

        try {
            return safeIsEmpty(value) ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int optInt(Bundle extras, String name) {
        return optInt(extras, name, 0);
    }

    public static void configOtherPushNotification(Notification notification) {
        int mask = Settings.get()
                .notifications()
                .getOtherNotificationMask();

        if (Utils.hasFlag(mask, ISettings.INotificationSettings.FLAG_LED)) {
            notification.ledARGB = 0xFF0000FF;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 100;
            notification.ledOffMS = 1000;
        }
        if (Utils.hasFlag(mask, ISettings.INotificationSettings.FLAG_VIBRO))
            notification.defaults |= Notification.DEFAULT_VIBRATE;

        if (Utils.hasFlag(mask, ISettings.INotificationSettings.FLAG_SOUND)) {
            notification.sound = Settings.get()
                    .notifications()
                    .getFeedbackRingtoneUri();
        }
    }
}