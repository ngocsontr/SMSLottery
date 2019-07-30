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

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import com.hally.lotsms.extensions.anyOf
import com.hally.lotsms.manager.ActiveConversationManager
import com.hally.lotsms.manager.KeyManager
import com.hally.lotsms.model.Conversation
import com.hally.lotsms.model.Lode
import com.hally.lotsms.util.Preferences
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import timber.log.Timber
import java.util.*
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

    override fun insertLode(lode: Lode): Lode? {
        val realm = Realm.getDefaultInstance()
        var managedLode: Lode? = null
        realm.executeTransaction { managedLode = realm.copyToRealmOrUpdate(lode) }
        realm.close()
        return managedLode
    }

    override fun getLodes(threadId: Long, query: String): RealmResults<Lode> {
        val then = Calendar.getInstance()
        then.add(Calendar.DAY_OF_YEAR, -1)
        return Realm.getDefaultInstance()
                .where(Lode::class.java)
                .equalTo("threadId", threadId)
                .let { if (query.isEmpty()) it else it.contains("body", query, Case.INSENSITIVE) }
//                .greaterThan("date", then.timeInMillis) // lấy giá trị trong ngày
                .sort("date")
                .findAllAsync()
    }

    override fun getLode(id: Long): Lode? {
        return Realm.getDefaultInstance()
                .where(Lode::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getLodeForPart(id: Long): Lode? {
        return Realm.getDefaultInstance()
                .where(Lode::class.java)
                .equalTo("parts.id", id)
                .findFirst()
    }

    override fun getUnreadCount(): Long {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .equalTo("read", false)
                .count()
    }

    /**
     * Retrieves the list of Lodes which should be shown in the notification
     * for a given conversation
     */
    override fun getUnreadUnseenLodes(threadId: Long): RealmResults<Lode> {
        return Realm.getDefaultInstance()
                .also { it.refresh() }
                .where(Lode::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun getUnreadLodes(threadId: Long): RealmResults<Lode> {
        return Realm.getDefaultInstance()
                .where(Lode::class.java)
                .equalTo("read", false)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAll()
    }

    override fun markAllSeen() {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Lode::class.java).equalTo("seen", false).findAll()
        realm.executeTransaction { messages.forEach { message -> message.seen = true } }
        realm.close()
    }

    override fun markSeen(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Lode::class.java)
                .equalTo("threadId", threadId)
                .equalTo("seen", false)
                .findAll()

        realm.executeTransaction {
            messages.forEach { message ->
                message.seen = true
            }
        }
        realm.close()
    }

    override fun markRead(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val messages = realm.where(Lode::class.java)
                    .anyOf("threadId", threadIds)
                    .beginGroup()
                    .equalTo("read", false)
                    .or()
                    .equalTo("seen", false)
                    .endGroup()
                    .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                }
            }
        }

        val values = ContentValues()
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.READ, true)

        threadIds.forEach { threadId ->
            try {
                val uri = ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
                context.contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
            } catch (exception: Exception) {
                Timber.w(exception)
            }
        }
    }

    override fun markUnread(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val conversation = realm.where(Conversation::class.java)
                    .anyOf("id", threadIds)
                    .equalTo("read", true)
                    .findAll()

            realm.executeTransaction {
                conversation.forEach { it.read = false }
            }
        }
    }

    override fun deleteLodes(vararg lodeIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val lodes = realm.where(Lode::class.java)
                    .anyOf("smsId", lodeIds)
                    .findAll()
            realm.executeTransaction { lodes.deleteAllFromRealm() }
        }
    }

    override fun deleteAllLode(threadid: Long?) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val lodes = if (threadid != null) realm.where(Lode::class.java)
                    .equalTo("threadId", threadid).findAll()
            else realm.where(Lode::class.java).findAll()
            realm.executeTransaction { lodes.deleteAllFromRealm() }
        }
    }

}