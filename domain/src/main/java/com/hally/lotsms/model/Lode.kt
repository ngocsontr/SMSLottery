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
package com.hally.lotsms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Lode : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    // LO DE
    var body: String = ""
    var lo: RealmList<Int> = RealmList()
    var degiainhat: RealmList<Int> = RealmList()
    var de: RealmList<Int> = RealmList()
    var xien: RealmList<Int> = RealmList()
    var bc: RealmList<Int> = RealmList()

    init {
        for (i in 0..99) {
            lo.add(i, 0)
            de.add(i, 0)
            degiainhat.add(i, 0)
        }
    }

    fun byType(type: String): RealmList<Int> {
        when (type.toLowerCase()) {
            "de giai nhat" -> return degiainhat
            "lo" -> return lo
            "de" -> return de
            "xien" -> return xien
            "bc" -> return bc
        }
        return RealmList()
    }

    var contentId: Long = 0
    var boxId: Int = 0
    var date: Long = 0
    var dateSent: Long = 0
    var seen: Boolean = false

    override fun toString(): String {
        return "Lode(id=$id, body='$body',\ndegiainhat=$degiainhat, \nlo=$lo, \nde=$de, \nxien=$xien, \nbc=$bc, contentId=$contentId, boxId=$boxId, date=$date, dateSent=$dateSent, seen=$seen)"
    }
}