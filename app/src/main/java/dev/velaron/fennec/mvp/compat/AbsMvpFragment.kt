package dev.velaron.fennec.mvp.compat

import android.os.Bundle
import android.view.View
import androidx.loader.app.LoaderManager
import dev.velaron.fennec.mvp.core.IMvpView
import dev.velaron.fennec.mvp.core.IPresenter
import dev.velaron.fennec.mvp.core.PresenterAction

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
abstract class AbsMvpFragment<P : IPresenter<V>, V : IMvpView> : androidx.fragment.app.Fragment(), ViewHostDelegate.IFactoryProvider<P, V> {

    private val delegate = ViewHostDelegate<P, V>()

    protected val presenter: P?
        get() = delegate.presenter

    protected val isPresenterPrepared: Boolean
        get() = delegate.isPresenterPrepared

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(requireActivity(), getViewHost(), this, LoaderManager.getInstance(this), savedInstanceState)
    }

    // Override in case of fragment not implementing IPresenter<View> interface
    protected fun getViewHost(): V = this as V

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delegate.onViewCreated()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delegate.onDestroyView()
    }

    override fun onDestroy() {
        delegate.onDestroy()
        super.onDestroy()
    }

    fun callPresenter(action: PresenterAction<P, V>) {
        delegate.callPresenter(action)
    }

    fun postPrenseterReceive(action: PresenterAction<P, V>) {
        delegate.postPrenseterReceive(action)
    }
}