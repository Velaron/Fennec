package dev.velaron.fennec.mvp.compat

import android.content.Context
import android.os.Bundle
import dev.velaron.fennec.mvp.core.IMvpView
import dev.velaron.fennec.mvp.core.IPresenter
import dev.velaron.fennec.mvp.core.IPresenterFactory
import dev.velaron.fennec.mvp.core.PresenterAction
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
class ViewHostDelegate<P : IPresenter<V>, V : IMvpView> {

    private var lastKnownPresenterState: Bundle? = null

    private var viewCreated: Boolean = false

    var presenter: P? = null

    private var viewReference: WeakReference<V?> = WeakReference(null)

    private val onReceivePresenterActions = ArrayList<PresenterAction<P, V>>()

    val isPresenterPrepared: Boolean
        get() = presenter != null

    fun onCreate(context: Context,
                 view: V,
                 factoryProvider: IFactoryProvider<P, V>,
                 loaderManager: androidx.loader.app.LoaderManager,
                 savedInstanceState: Bundle?) {
        this.viewReference = WeakReference(view)

        if (savedInstanceState != null) {
            this.lastKnownPresenterState = savedInstanceState.getBundle(SAVE_PRESENTER_STATE)
        }

        val app = context.applicationContext
        val loader = loaderManager.initLoader(LOADER_ID, lastKnownPresenterState, object : androidx.loader.app.LoaderManager.LoaderCallbacks<P> {
            override fun onCreateLoader(id: Int, args: Bundle?): androidx.loader.content.Loader<P> {
                return SimplePresenterLoader(app, factoryProvider.getPresenterFactory(args))
            }

            override fun onLoadFinished(loader: androidx.loader.content.Loader<P>, data: P) {

            }

            override fun onLoaderReset(loader: androidx.loader.content.Loader<P>) {
                presenter = null
            }
        })

        presenter = (loader as SimplePresenterLoader<P, V>).get()
        presenter?.run {
            attachViewHost(view)

            val copy = ArrayList(onReceivePresenterActions)
            onReceivePresenterActions.clear()
            for (action in copy) {
                action.call(this)
            }
        }
    }

    fun onDestroy() {
        viewReference = WeakReference(null)
        presenter?.detachViewHost()
    }

    fun onViewCreated() {
        if (viewCreated) {
            return
        }

        viewCreated = true
        presenter?.createView(viewReference.get()!!)
    }

    fun onDestroyView() {
        viewCreated = false
        presenter?.destroyView()
    }

    fun callPresenter(action: PresenterAction<P, V>) {
        presenter?.run {
            action.call(this)
        }
    }

    fun postPrenseterReceive(action: PresenterAction<P, V>) {
        presenter?.run {
            action.call(this)
        } ?: run {
            onReceivePresenterActions.add(action)
        }
    }

    fun onResume() {
        presenter?.resumeView()
    }

    fun onPause() {
        presenter?.pauseView()
    }

    fun onSaveInstanceState(outState: Bundle) {
        presenter?.run {
            lastKnownPresenterState = Bundle()
            saveState(lastKnownPresenterState!!)
        }

        outState.putBundle(SAVE_PRESENTER_STATE, lastKnownPresenterState)
    }

    interface IFactoryProvider<P : IPresenter<V>, V : IMvpView> {
        fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<P>
    }

    companion object {
        private const val SAVE_PRESENTER_STATE = "save-presenter-state"
        private const val LOADER_ID = 101
    }
}