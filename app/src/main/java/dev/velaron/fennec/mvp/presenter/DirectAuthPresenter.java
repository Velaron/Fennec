package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.getCauseIfRuntime;
import static dev.velaron.fennec.util.Utils.nonEmpty;
import static dev.velaron.fennec.util.Utils.trimmedNonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Injection;
import dev.velaron.fennec.api.ApiVersion;
import dev.velaron.fennec.api.Auth;
import dev.velaron.fennec.api.CaptchaNeedException;
import dev.velaron.fennec.api.NeedValidationException;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.LoginResponse;
import dev.velaron.fennec.model.Captcha;
import dev.velaron.fennec.mvp.presenter.base.RxSupportPresenter;
import dev.velaron.fennec.mvp.view.IDirectAuthView;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.RxUtils;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public class DirectAuthPresenter extends RxSupportPresenter<IDirectAuthView> {

    private final INetworker networker;

    private Captcha requieredCaptcha;
    private boolean requireSmsCode;
    private boolean requireAppCode;

    private boolean loginNow;

    private String username;
    private String pass;
    private String smsCode;
    private String captcha;
    private String appCode;

    public DirectAuthPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.networker = Injection.provideNetworkInterfaces();
    }

    public void fireLoginClick() {
        doLogin(false);
    }

    private void doLogin(boolean forceSms) {
        getView().hideKeyboard();

        final String trimmedUsername = nonEmpty(username) ? username.trim() : "";
        final String trimmedPass = nonEmpty(pass) ? pass.trim() : "";
        final String captchaSid = Objects.nonNull(requieredCaptcha) ? requieredCaptcha.getSid() : null;
        final String captchaCode = nonEmpty(captcha) ? captcha.trim() : null;

        final String code;

        if(requireSmsCode){
            code = (nonEmpty(smsCode) ? smsCode.trim() : null);
        } else if(requireAppCode){
            code = (nonEmpty(appCode) ? appCode.trim() : null);
        } else {
            code = null;
        }

        setLoginNow(true);
        appendDisposable(networker.vkDirectAuth()
                .directLogin("password", Constants.API_ID, Constants.SECRET,
                        trimmedUsername, trimmedPass, ApiVersion.CURRENT, true,
                        Auth.getScope(), code, captchaSid, captchaCode, forceSms)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLoginResponse, t -> onLoginError(getCauseIfRuntime(t))));
    }

    private void onLoginError(Throwable t) {
        t.printStackTrace();

        setLoginNow(false);

        this.requieredCaptcha = null;
        this.requireAppCode = false;
        this.requireSmsCode = false;

        if (t instanceof CaptchaNeedException) {
            String sid = ((CaptchaNeedException) t).getSid();
            String img = ((CaptchaNeedException) t).getImg();
            this.requieredCaptcha = new Captcha(sid, img);
        } else if (t instanceof NeedValidationException) {
            String type = ((NeedValidationException) t).getValidationType();

            if ("2fa_sms".equalsIgnoreCase(type)) {
                requireSmsCode = true;
            } else if("2fa_app".equalsIgnoreCase(type)){
                requireAppCode = true;
            }
        } else {
            showError(getView(), t);
        }

        resolveCaptchaViews();
        resolveSmsRootVisibility();
        resolveAppCodeRootVisibility();
        resolveButtonLoginState();

        if (Objects.nonNull(requieredCaptcha)) {
            callView(IDirectAuthView::moveFocusToCaptcha);
        } else if (requireSmsCode) {
            callView(IDirectAuthView::moveFocusToSmsCode);
        } else if(requireAppCode){
            callView(IDirectAuthView::moveFocusToAppCode);
        }
    }

    @OnGuiCreated
    private void resolveSmsRootVisibility() {
        if (isGuiReady()) {
            getView().setSmsRootVisible(requireSmsCode);
        }
    }

    @OnGuiCreated
    private void resolveAppCodeRootVisibility(){
        if (isGuiReady()) {
            getView().setAppCodeRootVisible(requireAppCode);
        }
    }

    @OnGuiCreated
    private void resolveCaptchaViews() {
        if (isGuiReady()) {
            getView().setCaptchaRootVisible(Objects.nonNull(requieredCaptcha));

            if (Objects.nonNull(requieredCaptcha)) {
                getView().displayCaptchaImage(requieredCaptcha.getImg());
            }
        }
    }

    private void onLoginResponse(LoginResponse response) {
        setLoginNow(false);

        if (nonEmpty(response.access_token) && response.user_id > 0) {
            callView(view -> view.returnSuccessToParent(response.user_id, response.access_token));
        }
    }

    private void setLoginNow(boolean loginNow) {
        this.loginNow = loginNow;
        resolveLoadingViews();
    }

    @OnGuiCreated
    private void resolveLoadingViews() {
        if (isGuiReady()) {
            getView().displayLoading(loginNow);
        }
    }

    public void fireLoginViaWebClick() {
        getView().returnLoginViaWebAction();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveButtonLoginState();
    }

    private void resolveButtonLoginState() {
        if (isGuiResumed()) {
            getView().setLoginButtonEnabled(trimmedNonEmpty(username)
                    && nonEmpty(pass)
                    && (Objects.isNull(requieredCaptcha) || trimmedNonEmpty(captcha))
                    && (!requireSmsCode || trimmedNonEmpty(smsCode))
                    && (!requireAppCode || trimmedNonEmpty(appCode)));
        }
    }

    public void fireLoginEdit(CharSequence sequence) {
        this.username = sequence.toString();
        resolveButtonLoginState();
    }

    public void firePasswordEdit(CharSequence s) {
        this.pass = s.toString();
        resolveButtonLoginState();
    }

    public void fireSmsCodeEdit(CharSequence sequence) {
        this.smsCode = sequence.toString();
        resolveButtonLoginState();
    }

    public void fireCaptchaEdit(CharSequence s) {
        this.captcha = s.toString();
        resolveButtonLoginState();
    }

    public void fireButtonSendCodeViaSmsClick() {
        doLogin(true);
    }

    public void fireAppCodeEdit(CharSequence s) {
        this.appCode = s.toString();
        resolveButtonLoginState();
    }
}