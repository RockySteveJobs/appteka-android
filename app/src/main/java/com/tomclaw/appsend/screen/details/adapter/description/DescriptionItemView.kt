package com.tomclaw.appsend.screen.details.adapter.description

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind


interface DescriptionItemView : ItemView {

    fun setText(value: String)

    fun setAppVersion(value: String)

    fun setVersionsCount(count: Int)

    fun setUploadDate(value: String)

    fun setChecksum(value: String)

    fun setSourceUrl(value: String?)

    fun setOnGooglePlayClickListener(listener: (() -> Unit)?)

    fun setOnVersionsClickListener(listener: (() -> Unit)?)

}

class DescriptionItemViewHolder(view: View) : BaseViewHolder(view), DescriptionItemView {

    private val context = view.context
    private val descriptionTitle: View = view.findViewById(R.id.description_title)
    private val description: TextView = view.findViewById(R.id.description)
    private val googlePlayButton: View = view.findViewById(R.id.google_play_button)
    private val appVersion: TextView = view.findViewById(R.id.app_version)
    private val versionsButton: View = view.findViewById(R.id.versions_button)
    private val versionsButtonText: TextView = view.findViewById(R.id.versions_button_text)
    private val uploadDate: TextView = view.findViewById(R.id.upload_date)
    private val checksum: TextView = view.findViewById(R.id.app_checksum)
    private val sourceUrlTitle: View = view.findViewById(R.id.app_source_url_title)
    private val sourceUrl: TextView = view.findViewById(R.id.app_source_url)

    private var googlePlayClickListener: (() -> Unit)? = null
    private var versionsClickListener: (() -> Unit)? = null

    init {
        googlePlayButton.setOnClickListener { googlePlayClickListener?.invoke() }
        versionsButton.setOnClickListener { versionsClickListener?.invoke() }
        sourceUrl.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun setText(value: String) {
        description.bind(value)
        descriptionTitle.visibility = description.visibility
    }

    override fun setAppVersion(value: String) {
        appVersion.bind(value)
    }

    override fun setVersionsCount(count: Int) {
        versionsButton.isVisible = count > 1
        versionsButtonText.text = context.resources.getQuantityString(
            R.plurals.other_versions_count,
            count,
            count
        )
    }

    override fun setUploadDate(value: String) {
        uploadDate.bind(value)
    }

    override fun setChecksum(value: String) {
        checksum.bind(value)
    }

    override fun setSourceUrl(value: String?) {
        sourceUrl.bind(value)
        sourceUrlTitle.visibility = sourceUrl.visibility
    }

    override fun setOnGooglePlayClickListener(listener: (() -> Unit)?) {
        this.googlePlayClickListener = listener
    }

    override fun setOnVersionsClickListener(listener: (() -> Unit)?) {
        this.versionsClickListener = listener
    }

    override fun onUnbind() {
        this.googlePlayClickListener = null
        this.versionsClickListener = null
    }

}
