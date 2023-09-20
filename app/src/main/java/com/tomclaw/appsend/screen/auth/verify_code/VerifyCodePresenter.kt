package com.tomclaw.appsend.screen.auth.verify_code

import android.os.Bundle
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface VerifyCodePresenter {

    fun attachView(view: VerifyCodeView)

    fun detachView()

    fun attachRouter(router: VerifyCodeRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface VerifyCodeRouter {

        fun leaveScreen(success: Boolean)

    }

}

class VerifyCodePresenterImpl(
    private val email: String,
    private val registered: Boolean,
    private val resourceProvider: VerifyCodeResourceProvider,
    private val interactor: VerifyCodeInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : VerifyCodePresenter {

    private var view: VerifyCodeView? = null
    private var router: VerifyCodePresenter.VerifyCodeRouter? = null

    private var code: String = state?.getString(KEY_CODE).orEmpty()
    private var name: String = state?.getString(KEY_NAME).orEmpty()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: VerifyCodeView) {
        this.view = view

        view.setCodeSentDescription(resourceProvider.formatCodeSentDescription(email))

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: VerifyCodePresenter.VerifyCodeRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putString(KEY_CODE, code)
        putString(KEY_NAME, name)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

}

private const val KEY_CODE = "code"
private const val KEY_NAME = "name"
