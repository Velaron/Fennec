package dev.velaron.fennec;

import dev.velaron.fennec.db.column.GroupColumns;
import dev.velaron.fennec.db.column.UserColumns;

public class Constants {

    public static final String PRIVACY_POLICY_LINK = "https://github.com/PhoenixDevTeam/Phoenix-for-VK/wiki/Privacy-policy";

    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    public static final int PHOENIX_LITE_API_ID = 4894723;
    public static final int PHOENIX_FULL_API_ID = 4994316;

    public static final int API_ID = BuildConfig.VK_API_APP_ID;
    public static final String SENDER_ID = BuildConfig.FCM_SENDER_ID;
    public static final String SECRET = BuildConfig.VK_CLIENT_SECRET;

    public static final String MAIN_OWNER_FIELDS = UserColumns.API_FIELDS + "," + GroupColumns.API_FIELDS;

    public static final String SERVICE_TOKEN = BuildConfig.SERVICE_TOKEN;

    public static final String PHOTOS_PATH = "Phoenix";
    public static final int PIN_DIGITS_COUNT = 4;

    public static final String PICASSO_TAG = "picasso_tag";
}
