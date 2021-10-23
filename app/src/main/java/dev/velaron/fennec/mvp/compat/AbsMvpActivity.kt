package dev.velaron.fennec.mvp.compat

import android.os.Bundle
import androidx.activity.ComponentActivity

import dev.velaron.fennec.mvp.core.IMvpView
import dev.velaron.fennec.mvp.core.IPresenter

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
abstract class AbsMvpActivity<P : IPresenter<V>, V : IMvpView> : ComponentActivity(), ViewHostDelegate.IFactoryProvider<P, V> {

    private val delegate = ViewHostDelegate<P, V>()

    protected val presenter: P?
        get() = delegate.presenter

    // Override in case of fragment not implementing IPresenter<View> interface
    protected fun getViewHost(): V = this as V

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
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

    override fun onDestroy() {
        delegate.onDestroyView()
        delegate.onDestroy()
        super.onDestroy()
    }
}