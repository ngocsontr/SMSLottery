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

import com.hally.lotsms.model.Lode
import io.realm.RealmResults

interface LodeRepository {

    fun getLodes(threadId: Long, query: String = ""): RealmResults<Lode>

    fun getLode(id: Long): Lode?

    fun getLodeForPart(id: Long): Lode?

    fun getUnreadCount(): Long

    /**
     * Retrieves the list of Lodes which should be shown in the notification
     * for a given conversation
     */
    fun getUnreadUnseenLodes(threadId: Long): RealmResults<Lode>

    /**
     * Retrieves the list of Lodes which should be shown in the quickreply popup
     * for a given conversation
     */
    fun getUnreadLodes(threadId: Long): RealmResults<Lode>

    fun markAllSeen()

    fun markSeen(threadId: Long)

    fun markRead(vararg threadIds: Long)

    fun markUnread(vararg threadIds: Long)

    fun deleteLodes(vararg LodeIds: Long)

    fun deleteAllLode(threadid: Long?)

    fun insertLode(lode: Lode): Lode?
}