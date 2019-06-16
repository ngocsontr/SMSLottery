/*"
" Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
"
" This file is part of QKSMS.
"
" QKSMS is free software: you can redistribute it and/or modify
" it under the terms of the GNU General Public License as published by
" the Free Software Foundation, either version 3 of the License, or
" (at your option) any later version.
"
" QKSMS is distributed in the hope that it will be useful,
" but WITHOUT ANY WARRANTY; without even the implied warranty of
" MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
" GNU General Public License for more details.
"
" You should have received a copy of the GNU General Public License
" along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
"*/
package com.hally.lotsms.common.util

import android.content.Context
import com.hally.lotsms.repository.ConversationRepository
import com.hally.lotsms.repository.MessageRepository
import com.hally.lotsms.util.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LodeUtil @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val conversationRepo: ConversationRepository,
        private val messageRepo: MessageRepository) {

    fun chot(text: CharSequence?) {


    }

    val De = listOf(
            listOf("Kép bằng", "De kep bang", "00 11 22 33 44 55 66 77 88 99"),
            listOf("Kép lệch", "De kep lech", "05 50 16 61 27 72 38 83 49 94"),
            listOf("sát kép bằng", "De sat kep bang", "01 10 12 21 23 32 34 43 54 45 65 56 67 76 78 87 89 98 "),
            listOf("sát kép lệch", "De sat kep lech", "04 06 49 51 15 17 60 62 26 28 71 73 37 39 82 84 48 50 93 95"),
            listOf("Dàn đề chẵn chẵn", "De chan chan", "00 22 44 66 88 02 20 04 40 06 60 08 80 24 42 26 62 28 82 46 64 48 84 68 86"),
            listOf("Dàn đề lẻ lẻ", "De le le", "11 33 55 77 99 13 31 15 51 17 71 19 91 35 53 37 73 39 93 57 75 59 95 79 97"),
            listOf("Dàn đề chẵn lẻ", "de chan le", "01 03 05 07 09 21 23 25 27 29 41 43 45 47 49 61 63 65 67 69 81 83 85 87 89"),
            listOf("Dàn đề lẻ chẵn", "de le chan", "10 12 14 16 18 30 32 34 36 38 50 52 54 56 58 70 72 74 76 78 90 92 94 96 98"),
            listOf("Bộ 01", "boj 01", "01 10 06 60 51 15 56 65"),
            listOf("Bộ 02", "boj 02", "02 20 07 70 52 25 57 75"),
            listOf("Bộ 03", "boj 03", "03 30 08 80 53 35 58 85"),
            listOf("Bộ 04", "boj 04", "04 40 09 90 54 45 59 95"),
            listOf("Bộ 12", "boj 12", "12 21 17 71 62 26 67 76"),
            listOf("Bộ 13", "boj 13", "13 31 18 81 63 36 68 86"),
            listOf("Bộ 14", "boj 14", "14 41 19 91 64 46 69 96"),
            listOf("Bộ 23", "boj 23", "23 32 28 82 73 37 78 87"),
            listOf("Bộ 24", "boj 24", "24 42 29 92 74 47 79 97"),
            listOf("Bộ 34", "boj 34", "34 43 39 93 84 48 89 98"),
            listOf("Bộ 00", "boj 00", "00 55 05 50"),
            listOf("Bộ 11", "boj 11", "11 66 16 61"),
            listOf("Bộ 22", "boj 22", "22 77 27 72"),
            listOf("Bộ 33", "boj 33", "33 88 38 83"),
            listOf("Bộ 44", "boj 44", "44 99 49 94"),
            listOf("Đầu 0", "De dau 0", "00 01 02 03 04 05 06 07 08 09"),
            listOf("Đầu 1", "de dau 1", "10 11 12 13 14 15 16 17 18 19"),
            listOf("Đầu 2", "De dau 2", "20 21 22 23 24 25 26 27 28 29"),
            listOf("Đầu 3", "de dau 3", "30 31 32 33 34 35 36 37 38 39"),
            listOf("Đầu 4", "de dau 4", "40 41 42 43 44 45 46 47 48 49"),
            listOf("Đầu 5", "de dau 5", "50 51 52 53 54 55 56 57 58 59"),
            listOf("Đầu 6", "De dau 6", "60 61 62 63 64 65 66 67 68 69"),
            listOf("Đầu 7", "de dau 7", "70 71 72 73 74 75 76 77 78 79"),
            listOf("Đầu 8", "de dau 8", "80 81 82 83 84 85 86 87 88 89"),
            listOf("Đầu 9", "de dau 9", "90 91 92 93 94 95 96 97 98 99"),
            listOf("Đít 0", "De dit 0", "00 10 20 30 40 50 60 70 80 90"),
            listOf("Đít 1", "de dit 1", "01 11 21 31 41 51 61 71 81 91"),
            listOf("Đít 2", "de dit 2", "02 12 22 32 42 52 62 72 82 92"),
            listOf("Đít 3", "de dit 3", "03 13 23 33 43 53 63 73 83 93"),
            listOf("Đít 4", "de dit 4", "04 14 24 34 44 54 64 74 84 94"),
            listOf("Đít 5", "De dit 5", "05 15 25 35 45 55 65 75 85 95"),
            listOf("Đít 6", "De dit 6", "06 16 26 36 46 56 66 76 86 96"),
            listOf("Đít 7", "De dit 7", "07 17 27 37 47 57 67 77 87 97"),
            listOf("Đít 8", "de dit 8", "08 18 28 38 48 58 68 78 88 98"),
            listOf("Đít 9", "de dit 9", "09 19 29 39 49 59 69 79 89 99"),
            listOf("Chạm 0", "De cham 0", "01 10 02 20 03 30 04 40 05 50 06 60 07 70 08 80 09 90 00"),
            listOf("Chạm 1", "De cham 1 ", "01 10 12 21 13 31 14 41 15 51 16 61 17 71 18 81 19 91 11"),
            listOf("Chạm 2", "De cham 2", "02 20 12 21 23 32 24 42 25 52 26 62 27 72 28 82 29 92 22"),
            listOf("Chạm 3", "De cham 3", "03 30 13 31 23 32 33 43 34 53 35 63 36 73 37 83 38 93 39"),
            listOf("Chạm 4", "De cham 4", "04 40 14 41 24 42 34 43 44 54 45 64 46 74 47 84 48 94 49"),
            listOf("Chạm 5", "De cham 5", "05 50 15 51 25 52 35 53 45 54 55 65 56 75 57 85 58 95 59"),
            listOf("Chạm 6", "De cham 6", "06 60 16 61 26 62 36 63 46 64 56 65 66 76 67 86 68 96 69"),
            listOf("Chạm 7", "De cham 7", "07 70 17 71 27 72 37 73 47 74 57 75 67 76 77 87 78 97 79"),
            listOf("Chạm 8", "De cham 8", "08 80 18 81 28 82 38 83 48 84 58 85 68 86 78 87 88 98 89"),
            listOf("Chạm 9", "De cham 9", "09 90 19 91 29 92 39 93 49 94 59 95 69 96 79 97 89 98 99"),
            listOf("Tổng 0", "De tong 0", "19 91 28 82 37 73 46 64 55 00"),
            listOf("Tổng 1", "De tong 1", "01 10 29 92 38 83 47 74 56 65"),
            listOf("Tổng 2", "De tong 2", "02 20 39 93 48 84 57 75 11 66"),
            listOf("Tổng 3", "De tong 3", "03 30 12 21 49 94 58 85 67 76"),
            listOf("Tổng 4", "De tong 4", "04 40 13 31 59 95 68 86 22 77"),
            listOf("Tổng 5", "De tong 5", "05 50 14 41 23 32 69 96 78 87"),
            listOf("Tổng 6", "De tong 6", "06 60 15 51 24 42 79 97 33 88"),
            listOf("Tổng 7", "De tong 7", "07 70 16 61 25 52 34 43 89 98"),
            listOf("Tổng 8", "De tong 8", "08 80 17 71 26 62 35 53 44 99"),
            listOf("Tổng 9", "De tong 9", "09 90 18 81 27 72 36 63 45 54"))

}