package dev.velaron.fennec.mvp.compat

import android.content.Context
import androidx.loader.content.Loader

import dev.velaron.fennec.mvp.core.IMvpView
import dev.velaron.fennec.mvp.core.IPresenter
import dev.velaron.fennec.mvp.core.IPresenterFactory

class SimplePresenterLoader<P : IPresenter<V>, V : IMvpView> constructor(context: Context, var factory: IPresenterFactory<P>) : androidx.loader.content.Loader<P>(context) {

    private var f: IPresenterFactory<P>? = factory

    private var presenter: P? = null

    fun get(): P {
        if (presenter == null) {
            presenter = factory.create()
            f = null
        }

        return presenter!!
    }

    override fun onReset() {
        super.onReset()
        presenter?.destroy()
        presenter = null
    }
}