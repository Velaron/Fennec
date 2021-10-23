package dev.velaron.fennec.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.appbar.MaterialToolbar;

import dev.velaron.fennec.R;

public class CenteredToolbar extends MaterialToolbar {

    private TextView tvTitle;
    private TextView tvSubtitle;

    public CenteredToolbar(Context context) {
        super(context);
        setupTextViews();
    }

    public CenteredToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupTextViews();
    }

    public CenteredToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupTextViews();
    }

    @Override
    public void setTitle(@StringRes int resId) {
        String s = getResources().getString(resId);
        setTitle(s);
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }

    @Override
    public void setSubtitle(@StringRes int resId) {
        String s = getResources().getString(resId);
        setSubtitle(s);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        if (subtitle == null) {
            return;
        }
        tvSubtitle.setVisibility(VISIBLE);
        tvSubtitle.setText(subtitle);
    }

    @Override
    public CharSequence getTitle() {
        return tvTitle.getText().toString();
    }

    @Override
    public CharSequence getSubtitle() {
        return tvSubtitle.getText().toString();
    }

    private void setupTextViews() {
        Typeface appFont;
        try {
            appFont = ResourcesCompat.getFont(getContext(), R.font.open_sans);
        } catch (Resources.NotFoundException e) {
            appFont = null;
        }
        tvTitle = new TextView(getContext());
        tvTitle.setSingleLine();
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setTextAppearance(getContext(), R.style.TextAppearance_Toolbar_Title);
        tvTitle.setTypeface(appFont);

        tvSubtitle = new TextView(getContext());
        tvSubtitle.setSingleLine();
        tvSubtitle.setEllipsize(TextUtils.TruncateAt.END);
        tvSubtitle.setTextAppearance(getContext(), R.style.TextAppearance_MaterialComponents_Caption);
        tvSubtitle.setTypeface(appFont);

        LinearLayout linear = new LinearLayout(getContext());
        linear.setGravity(Gravity.CENTER);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.addView(tvTitle);
        linear.addView(tvSubtitle);

        tvSubtitle.setVisibility(GONE);

        MaterialToolbar.LayoutParams lp = new MaterialToolbar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        linear.setLayoutParams(lp);

        addView(linear);
    }
}
