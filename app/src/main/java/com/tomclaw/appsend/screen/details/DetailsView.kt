package com.tomclaw.appsend.screen.details

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder
import io.reactivex.rxjava3.core.Observable

interface DetailsView {

    fun showProgress()

    fun showContent()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun setAppIcon(url: String?)

    fun setAppLabel(label: String?)

    fun setAppPackage(packageName: String?)

    fun showUploader()

    fun hideUploader()

    fun setUploaderIcon(userIcon: UserIcon)

    fun setUploaderName(name: String)

}

class DetailsViewImpl(
    private val view: View
) : DetailsView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val blockingProgress: View = view.findViewById(R.id.blocking_progress)
    private val appIcon: ImageView = view.findViewById(R.id.app_icon)
    private val appLabel: TextView = view.findViewById(R.id.app_label)
    private val appPackage: TextView = view.findViewById(R.id.app_package)
    private val uploaderContainer: View = view.findViewById(R.id.uploader_container)
    private val uploaderIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.uploader_icon))
    private val uploaderName: TextView = view.findViewById(R.id.uploader_name)
//    private val error: TextView = view.findViewById(R.id.error_text)
//    private val retryButton: View = view.findViewById(R.id.button_retry)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
    }

    override fun setAppIcon(url: String?) {
        appIcon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholder = {
                with(it.get()) {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.app_placeholder)
                }
            }
        }
    }

    override fun setAppLabel(label: String?) {
        this.appLabel.bind(label)
    }

    override fun setAppPackage(packageName: String?) {
        this.appPackage.bind(packageName)
    }

    override fun showUploader() {
        uploaderContainer.show()
    }

    override fun hideUploader() {
        uploaderContainer.hide()
    }

    override fun setUploaderIcon(userIcon: UserIcon) {
        this.uploaderIcon.bind(userIcon)
    }

    override fun setUploaderName(name: String) {
        this.uploaderName.bind(name)
    }

    override fun showProgress() {
        blockingProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        blockingProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

}
