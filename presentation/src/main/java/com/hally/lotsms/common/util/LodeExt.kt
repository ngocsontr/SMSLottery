/*"
" Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
"
" This file is part of QKSMS.
"
" QKSMS is free software: you can redistribute it and/or modify
" it under the terms of the GNU General  License as published by
" the Free Software Foundation, either version 3 of the License, or
" (at your option) any later version.
"
" QKSMS is distributed in the hope that it will be useful,
" but WITHOUT ANY WARRANTY; without even the implied warranty of
" MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
" GNU General  License for more details.
"
" You should have received a copy of the GNU General  License
" along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
"*/
package com.hally.lotsms.common.util

import com.hally.lotsms.common.LodeDialog
import java.text.DecimalFormat

fun <E> List<E>.getSoTrungThuong(targets: Array<Int>): Int {
    var result = 0
    forEachIndexed { index, it ->
        result += when {
            it is List<*> -> it.getSoTrungThuong(targets)
            targets.contains(index) -> it as Int
            else -> 0
        }
    }
    return result
}

fun <E> List<E>.getTongDanh(): Int {
    var result = 0
    forEach {
        result += if (it is List<*>) it.getTongDanh()
        else it as Int
    }
    return result
}


fun String.removeText(): String {
    // tra nay day cac so phan tach = dau SPACE
    return this.replace("[^0-9]".toRegex(), LodeDialog.SPACE).removeSpace()
}

fun String.removeSpace(): String {
    // remove duplicate SPACE
    return this.replace("\\s+".toRegex(), LodeDialog.SPACE).trim()
}

fun <E> MutableList<E>.toText(): String {
    val builder = StringBuffer()
    this.forEach { builder.append(it.toString() + " ") }
    return builder.toString().trim()
}

fun Number.format(): String {
    return DecimalFormat("#,###.#").format(this)
}
