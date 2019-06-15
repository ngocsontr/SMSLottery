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
package com.hally.lotsms.injection

import com.hally.lotsms.common.QKApplication
import com.hally.lotsms.common.QkDialog
import com.hally.lotsms.common.util.QkChooserTargetService
import com.hally.lotsms.common.widget.*
import com.hally.lotsms.feature.backup.BackupController
import com.hally.lotsms.feature.compose.DetailedChipView
import com.hally.lotsms.feature.conversationinfo.injection.ConversationInfoComponent
import com.hally.lotsms.feature.settings.SettingsController
import com.hally.lotsms.feature.settings.about.AboutController
import com.hally.lotsms.feature.settings.swipe.SwipeActionsController
import com.hally.lotsms.feature.themepicker.injection.ThemePickerComponent
import com.hally.lotsms.feature.widget.WidgetAdapter
import com.hally.lotsms.injection.android.ActivityBuilderModule
import com.hally.lotsms.injection.android.BroadcastReceiverBuilderModule
import com.hally.lotsms.injection.android.ServiceBuilderModule
import com.hally.lotsms.util.ContactImageLoader
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class,
    BroadcastReceiverBuilderModule::class,
    ServiceBuilderModule::class])
interface AppComponent {

    fun conversationInfoBuilder(): ConversationInfoComponent.Builder
    fun themePickerBuilder(): ThemePickerComponent.Builder

    fun inject(application: QKApplication)

    fun inject(controller: AboutController)
    fun inject(controller: BackupController)
    fun inject(controller: SettingsController)
    fun inject(controller: SwipeActionsController)

    fun inject(dialog: QkDialog)

    fun inject(fetcher: ContactImageLoader.ContactImageFetcher)

    fun inject(service: WidgetAdapter)

    /**
     * This can't use AndroidInjection, or else it will crash on pre-marshmallow devices
     */
    fun inject(service: QkChooserTargetService)

    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)
    fun inject(view: QkTextView)

}