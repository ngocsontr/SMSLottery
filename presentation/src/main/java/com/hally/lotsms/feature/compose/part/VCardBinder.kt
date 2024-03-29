/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hally.lotsms.feature.compose.part

import android.content.ContentUris
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.hally.lotsms.R
import com.hally.lotsms.common.Navigator
import com.hally.lotsms.common.util.Colors
import com.hally.lotsms.common.util.extensions.resolveThemeColor
import com.hally.lotsms.common.util.extensions.setBackgroundTint
import com.hally.lotsms.common.util.extensions.setTint
import com.hally.lotsms.extensions.isVCard
import com.hally.lotsms.extensions.mapNotNull
import com.hally.lotsms.feature.compose.BubbleUtils
import com.hally.lotsms.mapper.CursorToPartImpl
import com.hally.lotsms.model.Message
import com.hally.lotsms.model.MmsPart
import ezvcard.Ezvcard
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mms_vcard_list_item.view.*

class VCardBinder(
    private val context: Context,
    private val navigator: Navigator,
    private val theme: Colors.Theme
) : PartBinder {

    override val partLayout = R.layout.mms_vcard_list_item

    override fun canBindPart(part: MmsPart) = part.isVCard()

    override fun bindPart(
        view: View,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        val uri = ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)
        val bubble = BubbleUtils.getBubble(canGroupWithPrevious, canGroupWithNext, message.isMe())

        view.setOnClickListener { navigator.saveVcard(uri) }
        view.vCardBackground.setBackgroundResource(bubble)

        Observable.just(uri)
                .map(context.contentResolver::openInputStream)
                .mapNotNull { inputStream -> inputStream.use { Ezvcard.parse(it).first() } }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { vcard -> view.name?.text = vcard.formattedName.value }

        val params = view.vCardBackground.layoutParams as FrameLayout.LayoutParams
        if (!message.isMe()) {
            view.vCardBackground.layoutParams = params.apply { gravity = Gravity.START }
            view.vCardBackground.setBackgroundTint(theme.theme)
            view.vCardAvatar.setTint(theme.textPrimary)
            view.name.setTextColor(theme.textPrimary)
            view.label.setTextColor(theme.textTertiary)
        } else {
            view.vCardBackground.layoutParams = params.apply { gravity = Gravity.END }
            view.vCardBackground.setBackgroundTint(view.context.resolveThemeColor(R.attr.bubbleColor))
            view.vCardAvatar.setTint(view.context.resolveThemeColor(android.R.attr.textColorSecondary))
            view.name.setTextColor(view.context.resolveThemeColor(android.R.attr.textColorPrimary))
            view.label.setTextColor(view.context.resolveThemeColor(android.R.attr.textColorTertiary))
        }
    }

}