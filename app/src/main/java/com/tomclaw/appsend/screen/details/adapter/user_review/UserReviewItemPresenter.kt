package com.tomclaw.appsend.screen.details.adapter.user_review

import java.text.DateFormat
import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class UserReviewItemPresenter(
    private val dateFormatter: DateFormat,
    private val listener: ItemListener,
) : ItemPresenter<UserReviewItemView, UserReviewItem> {

    override fun bindView(view: UserReviewItemView, item: UserReviewItem, position: Int) {
        view.setUserIcon(item.userIcon)
        view.setRating(item.score.toFloat())
        val date: String = dateFormatter.format(item.time * 1000)
        view.setDate(date)
        view.setComment(item.text)
        view.setOnClickListener { listener.onScoresClick() }
    }

}
