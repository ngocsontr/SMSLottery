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
package com.hally.lotsms.repository

import android.content.Context
import com.hally.lotsms.manager.ActiveConversationManager
import com.hally.lotsms.manager.KeyManager
import com.hally.lotsms.model.Attachment
import com.hally.lotsms.model.Lode
import com.hally.lotsms.util.Preferences
import io.realm.RealmResults
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LodeRepositoryImpl @Inject constructor(
        private val activeConversationManager: ActiveConversationManager,
        private val context: Context,
        private val messageIds: KeyManager,
        private val imageRepository: ImageRepository,
        private val prefs: Preferences,
        private val syncRepository: SyncRepository
) : LodeRepository {

    override fun getLodes(threadId: Long, query: String): RealmResults<Lode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLode(id: Long): Lode? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLodeForPart(id: Long): Lode? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUnreadCount(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUnreadUnseenLodes(threadId: Long): RealmResults<Lode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUnreadLodes(threadId: Long): RealmResults<Lode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markAllSeen() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markSeen(threadId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markRead(vararg threadIds: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markUnread(vararg threadIds: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendLode(subId: Int, threadId: Long, addresses: List<String>, body: String, attachments: List<Attachment>, delay: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendSms(Lode: Lode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancelDelayedSms(id: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertSentSms(subId: Int, threadId: Long, address: String, body: String, date: Long): Lode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Lode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markSending(id: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markSent(id: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markFailed(id: Long, resultCode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markDelivered(id: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markDeliveryFailed(id: Long, resultCode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteLodes(vararg LodeIds: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}