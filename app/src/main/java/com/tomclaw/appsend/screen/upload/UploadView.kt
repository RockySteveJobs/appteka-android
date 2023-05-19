package com.tomclaw.appsend.screen.upload

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.appsend.util.svgToDrawable
import io.reactivex.rxjava3.core.Observable

interface UploadView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showError()

    fun hideError()

    fun showCategories(items: List<CategoryItem>)

    fun showVersionsDialog(items: List<VersionItem>)

    fun showUploadProgress()

    fun resetUploadProgress()

    fun setUploadProgress(value: Int)

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun categorySelectedClicks(): Observable<CategoryItem>

    fun categoryClearedClicks(): Observable<Unit>

    fun versionClicks(): Observable<VersionItem>

}

class UploadViewImpl(
    view: View,
    private val preferences: UploadPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : UploadView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val flipper: ViewFlipper = view.findViewById(R.id.flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: View = view.findViewById(R.id.error)
    private val progress: View = view.findViewById(R.id.overlay_progress)
    private val retryButton: View = view.findViewById(R.id.retry_button)
    private val uploadProgress: ProgressBar = view.findViewById(R.id.upload_progress)
    private val uploadPercent: TextView = view.findViewById(R.id.upload_percent)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val categorySelectedRelay = PublishRelay.create<CategoryItem>()
    private val categoryClearedRelay = PublishRelay.create<Unit>()
    private val versionRelay = PublishRelay.create<VersionItem>()

    private val layoutManager: LinearLayoutManager

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        retryButton.setOnClickListener { retryRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
    }

    override fun showProgress() {
        progress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        flipper.displayedChild = CHILD_CONTENT
        progress.hideWithAlphaAnimation(animateFully = false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun showError() {
        error.show()
    }

    override fun hideError() {
        error.hide()
    }

    override fun showCategories(items: List<CategoryItem>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
            .apply {
                addItem(0, R.string.category_not_defined, R.drawable.ic_category)
            }
            .apply {
                for (item in items) {
                    val title = item.title
                    val icon = svgToDrawable(item.icon, context.resources)
                    addItem(item.id, title, icon)
                }
            }
            .setItemClickListener { item ->
                val categoryItem = items.find {
                    it.id == item.itemId
                } ?: run {
                    categoryClearedRelay.accept(Unit)
                    return@setItemClickListener
                }
                categorySelectedRelay.accept(categoryItem)
            }
            .createDialog()
            .show()
    }

    override fun showVersionsDialog(items: List<VersionItem>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        var bottomSheet: Dialog? = null
        bottomSheet = BottomSheetBuilder(context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .apply {
                for (item in items) {
                    val icon = if (item.compatible) {
                        if (item.newer) {
                            setIconTintColorResource(R.color.newer_color)
                            setItemTextColorResource(R.color.newer_text_color)
                            R.drawable.ic_new
                        } else {
                            setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
                            setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
                            R.drawable.ic_download_circle
                        }
                    } else {
                        setIconTintColorResource(R.color.incompatible_color)
                        setItemTextColorResource(R.color.incompatible_text_color)
                        R.drawable.ic_alert_circle
                    }
                    addItem(item.versionId, item.title, icon)
                }
            }
            .setItemClickListener { item ->
                items.find {
                    it.versionId == item.itemId
                }?.let {
                    bottomSheet?.hide()
                    versionRelay.accept(it)
                }
            }
            .createDialog()
            .apply { show() }
    }

    override fun showUploadProgress() {
        flipper.displayedChild = CHILD_UPLOAD
    }

    override fun resetUploadProgress() {
        showUploadProgress()
        uploadProgress.progress = 0
        uploadPercent.bind(context.getString(R.string.percent, 0))
    }

    override fun setUploadProgress(value: Int) {
        showUploadProgress()
        uploadProgress.setProgressWithAnimation(value, 500)
        uploadPercent.bind(context.getString(R.string.percent, value))
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun categorySelectedClicks(): Observable<CategoryItem> = categorySelectedRelay

    override fun categoryClearedClicks(): Observable<Unit> = categoryClearedRelay

    override fun versionClicks(): Observable<VersionItem> = versionRelay

    @SuppressLint("AnimatorKeep")
    fun ProgressBar.setProgressWithAnimation(progress: Int, duration: Long = 1500) {
        val objectAnimator = ObjectAnimator.ofInt(this, "progress", progress)
        objectAnimator.duration = duration
        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.start()
    }

}

private const val DURATION_MEDIUM = 300L
private const val CHILD_CONTENT = 0
private const val CHILD_UPLOAD = 1
