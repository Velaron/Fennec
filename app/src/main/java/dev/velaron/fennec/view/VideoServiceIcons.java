package dev.velaron.fennec.view;

import androidx.annotation.DrawableRes;

import dev.velaron.fennec.R;
import dev.velaron.fennec.model.VideoPlatform;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class VideoServiceIcons {

    @DrawableRes
    public static Integer getIconByType(String platfrom) {
        if(platfrom == null){
            return null;
        }

        switch (platfrom) {
            default:
                return null;
            case VideoPlatform.COUB:
                return R.drawable.logo_coub;
            case VideoPlatform.VIMEO:
                return R.drawable.logo_vimeo;
            case VideoPlatform.YOUTUBE:
                return R.drawable.logo_youtube_trans;
            case VideoPlatform.RUTUBE:
                return R.drawable.logo_rutube;
        }
    }
}