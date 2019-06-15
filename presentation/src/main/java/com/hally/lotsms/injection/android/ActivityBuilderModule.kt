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
package com.hally.lotsms.injection.android

import com.hally.lotsms.feature.backup.BackupActivity
import com.hally.lotsms.feature.blocked.BlockedActivity
import com.hally.lotsms.feature.blocked.BlockedActivityModule
import com.hally.lotsms.feature.compose.ComposeActivity
import com.hally.lotsms.feature.compose.ComposeActivityModule
import com.hally.lotsms.feature.conversationinfo.ConversationInfoActivity
import com.hally.lotsms.feature.gallery.GalleryActivity
import com.hally.lotsms.feature.gallery.GalleryActivityModule
import com.hally.lotsms.feature.main.MainActivity
import com.hally.lotsms.feature.main.MainActivityModule
import com.hally.lotsms.feature.notificationprefs.NotificationPrefsActivity
import com.hally.lotsms.feature.notificationprefs.NotificationPrefsActivityModule
import com.hally.lotsms.feature.plus.PlusActivity
import com.hally.lotsms.feature.plus.PlusActivityModule
import com.hally.lotsms.feature.qkreply.QkReplyActivity
import com.hally.lotsms.feature.qkreply.QkReplyActivityModule
import com.hally.lotsms.feature.scheduled.ScheduledActivity
import com.hally.lotsms.feature.scheduled.ScheduledActivityModule
import com.hally.lotsms.feature.settings.SettingsActivity
import com.hally.lotsms.injection.scope.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BlockedActivityModule::class])
    abstract fun bindBlockedActivity(): BlockedActivity

}