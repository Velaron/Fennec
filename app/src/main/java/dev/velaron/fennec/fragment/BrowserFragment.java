package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.fragment.base.BaseFragment;
import dev.velaron.fennec.link.LinkHelper;
import dev.velaron.fennec.link.VkLinkParser;
import dev.velaron.fennec.link.types.AbsLink;
import dev.velaron.fennec.link.types.AwayLink;
import dev.velaron.fennec.link.types.PageLink;
import dev.velaron.fennec.listener.BackPressCallback;
import dev.velaron.fennec.util.Logger;

public class BrowserFragment extends BaseFragment implements BackPressCallback {

    public static final String TAG = BrowserFragment.class.getSimpleName();
    private static final String SAVE_TITLE = "save_title";
    protected WebView mWebView;
    private int mAccountId;
    private String title;
    private Bundle webState;

    public static Bundle buildArgs(int accountId, @NonNull String url) {
        Bundle args = new Bundle();
        args.putString(Extra.URL, url);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static BrowserFragment newInstance(Bundle args) {
        BrowserFragment fragment = new BrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);

        setRetainInstance(true);

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mWebView = root.findViewById(R.id.webview);

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.setWebViewClient(new VkLinkSupportWebClient());

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                BrowserFragment.this.title = title;
                refreshActionBar();
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true); // из-за этого не срабатывал метод
        // shouldOverrideUrlLoading в WebClient

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        } else if (webState != null) {
            mWebView.restoreState(webState);
            webState = null;
        } else {
            loadAtFirst();
        }

        return root;
    }

    protected void loadAtFirst() {
        String url = getArguments().getString(Extra.URL);
        Logger.d(TAG, "url: " + url);
        mWebView.loadUrl(url);
    }

    private void refreshActionBar() {
        if (!isAdded()) {
            return;
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.browser);
            actionBar.setSubtitle(title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshActionBar();

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        webState = new Bundle();
        mWebView.saveState(webState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_TITLE, title);
        mWebView.saveState(outState);
    }

    private void restoreFromInstanceState(@NonNull Bundle bundle) {
        if (mWebView != null) {
            mWebView.restoreState(bundle);
        }

        title = bundle.getString(SAVE_TITLE);
        Logger.d(TAG, "restoreFromInstanceState, bundle: " + bundle);
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return false;
        }

        return true;
    }

    private class VkLinkSupportWebClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Logger.d(TAG, "onLoadResource, url: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            AbsLink link = VkLinkParser.parse(url);
            Logger.d(TAG, "shouldOverrideUrlLoading, link: " + link + ", url: " + url);

            //link: null, url: https://vk.com/doc124456557_415878705

            if (link == null) {
                LinkHelper.openLinkInBrowser(requireActivity(), url);
                return true;
            }

            if (link instanceof PageLink) {
                view.loadUrl(url + "?api_view=0df43cdc43a25550c6beb7357c9d41");
                return true;
            }

            if (link instanceof AwayLink) {
                LinkHelper.openLinkInBrowser(requireActivity(), ((AwayLink) link).link);
                return true;
            }

            if (LinkHelper.openVKLink(requireActivity(), mAccountId, link)) {
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            title = view.getTitle();
            refreshActionBar();
        }


    }
}