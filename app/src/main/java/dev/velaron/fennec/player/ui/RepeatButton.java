/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package dev.velaron.fennec.player.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.widget.AppCompatImageButton;

import dev.velaron.fennec.R;
import dev.velaron.fennec.player.MusicPlaybackService;
import dev.velaron.fennec.player.util.MusicUtils;

public class RepeatButton extends AppCompatImageButton implements OnClickListener {

    public RepeatButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        MusicUtils.cycleRepeat();
        updateRepeatState();
    }

    public void updateRepeatState() {
        switch (MusicUtils.getRepeatMode()) {
            case MusicPlaybackService.REPEAT_ALL:
                setImageDrawable(getResources().getDrawable(R.drawable.repeat));
                break;
            case MusicPlaybackService.REPEAT_CURRENT:
                setImageDrawable(getResources().getDrawable(R.drawable.repeat_once));
                break;
            case MusicPlaybackService.REPEAT_NONE:
                setImageDrawable(getResources().getDrawable(R.drawable.repeat_off));
                break;
            default:
                break;
        }
    }
}
