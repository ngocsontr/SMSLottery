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
import com.hally.lotsms.model.Lode
import com.hally.lotsms.model.Message
import com.hally.lotsms.repository.ConversationRepository
import com.hally.lotsms.repository.LodeRepository
import com.hally.lotsms.repository.MessageRepository
import com.hally.lotsms.util.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LodeUtil @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val lodeRepo: LodeRepository,
        private val conversationRepo: ConversationRepository,
        private val messageRepo: MessageRepository) {

    fun chot(text: Message, lode: Lode) {


    }


    companion object {
        val TEST = "Đề 22.66.88x150k\n" +
                "00.11x100k\n" +
                "33.44.55.77.99x60k\n" +
                "\n" +
                "Lo 01 10 12 13x50đ\n" +
                "De 01 10 12 13x50n\n" +
                "\n" +
                "Ghi e lô 33 20d\n" +
                "38 20d\n" +
                "Đề 33 ,38,83 50k nhé\n" +
                "\n" +
                "Đề : 696 191 464 141 x 50k\n" +
                "\n" +
                "Lô 323 x 10d\n" +
                "\n" +
                "De chan chan x28n.t2\n" +
                "\n" +
                "Lo 80x40 đ\n" +
                "\n" +
                "Lô 16-141 điểm\n" +
                "\n" +
                "Lô 86.89 x60d 90.25x40d\n" +
                "Dê kép bang x70n\n" +
                "Đề 66.22 x70n t1\n" +
                "\n" +
                "lô 36.63 x141đ\n" +
                "\n" +
                "Lo 34.43x10d\n" +
                "\n"
//                "Lô 030 x 201 đ\n" +
//                "Đề 030.060 x 255k\n" +
//                "Đề 93,53,49,25,26,27,94,15,17,71,68,86 x 75K. Đề 12,21 x 170N. Đề 15,51 x 100N. Lô 15,51 x 90d.\n" +
//                "\n" +
//                "Lô 01.10x191 đ\n" +
//                "\n" +
//                "Lô 56x40d 10.65x20d t5\n" +
//                "\n" +
//                "lô 17x30d ..đề 686.595.x50n.đề kép bằng x 40n.t5\n" +
//                "\n" +
//                "Đề 80 x 100k\n" +
//                "26 x 30k\n" +
//                "51 x 20k\n" +
//                "09 x 50k\n" +
//                "Lô 040 , 07 , 232 , 383 , 484 x 5d\n" +
//                "23 , 38 x 10d\n" +
//                "\n" +
//                "Lô 494.373x 20d .t1\n" +
//                "\n" +
//                "Lô 32 , 39 x 50d\n" +
//                "\n" +
//                "De chạm 6x25.de 606 262x35.t3\n" +
//                "Đề 60 số :00 01 04 06 08 09 10 13 15 17 18 19 22 24 26 27 28 29 31 33 35 36 37 38 40 42 44 45 46 47 49 51 53 54 55 56 59 60 62 63 64 65 68 71 72 73 74 77 79 80 81 82 87 86 88 90 91 92 95 99 x 25kt9\n" +
//                "De chạm 6x25.de 606 262x35.t3\n" +
//                "\n" +
//                "De chan lẻ x 70n.t4\n" +
//                "De 131.060.040x30\n" +
//                "\n" +
//                "Lô 45-85/65d, 16-97/40d\n" +
//                "\n" +
//                "XQuây 4: 45-85-16-97/50k.t5\n" +
//                "\n" +
//                "Lô 89 98 97 79 68 86 x10d đề 68 86 79 89 45 x180n t6\n" +
//                "\n" +
//                "Đề 33 x 500k ( bờm )\n" +
//                "\n" +
//                "Đề : 343 11 66 484 393 898 x 20k\n" +
//                "\n" +
//                "Lo 47x10d.\n" +
//                "Đề 87 78 83 69 96 58 x 80n t7\n" +
//                "\n" +
//                "Đề 585/150k.t13\n" +
//                "\n" +
//                "L 57x60d, 393x40d\n" +
//                "\n" +
//                "Đề:đầu:3x30n đuôi3x30n\n" +
//                "\n" +
//                "Đề\n" +
//                "Kép x60\n" +
//                "22-77-11 x40\n" +
//                "66-55-00-44-99 x30\n" +
//                "33-88 x20\n" +
//                "Kép lệch x35\n" +
//                "272-050 x35\n" +
//                "494-383-161 x20\n" +
//                "\n" +
//                "Lô\n" +
//                "080-363-121-090-44-020 x15\n" +
//                "\n" +
//                "Lô 51/40d.t7\n" +
//                "\n" +
//                "Lô 75.76.77 x10đ đe 55x25n t8\n" +
//                "\n" +
//                "de 949x30k .131x30k\n" +
//                "\n" +
//                "Đề bộ 49x50n bộ 66x50n\n" +
//                "\n" +
//                "Ghi con đề 26x80k\n" +
//                "262,020x30k\n" +
//                "\n" +
//                "Đề chạm 0.3.5 x 7n. 00 33 55 88 99 070 090 010 545 x 70n. T9\n" +
//                "\n" +
//                "Đề 797 x 60k\n" +
//                "\n" +
//                "De 191,343,171x70k\n" +
//                "88,101x170k,686x500k\n" +
//                "020,080,454x100k\n" +
//                "242,595,565x70k.393x100.t10\n"

        val SIGNX = 'x'
        val SIGNAL = arrayOf('x', '*', '/', 'd', 'n', 'k')
        val VietNamChar = arrayOf("aAeEoOuUiIdDyY",
                "áàạảãâấầậẩẫăắằặẳẵ", "ÁÀẠẢÃÂẤẦẬẨẪĂẮẰẶẲẴ", "éèẹẻẽêếềệểễ", "ÉÈẸẺẼÊẾỀỆỂỄ", "óòọỏõôốồộổỗơớờợởỡ",
                "ÓÒỌỎÕÔỐỒỘỔỖƠỚỜỢỞỠ", "úùụủũưứừựửữ", "ÚÙỤỦŨƯỨỪỰỬỮ", "íìịỉĩ", "ÍÌỊỈĨ", "đ", "Đ", "ýỳỵỷỹ", "ÝỲỴỶỸ")

        fun removeVietnamese(s: String): String {
//        Toast.makeText(activity, "Xóa Vietnamese!!", Toast.LENGTH_LONG).show()
            var str = s.trim()
            str = str.toLowerCase()

            str = str.replace("bộ", "boj")
            //Thay thế và lọc dấu từng char
            for (i in 1 until VietNamChar.size) {
                for (j in 0 until VietNamChar[i].length)
                    str = str.replace(VietNamChar[i][j], VietNamChar[0][i - 1])
            }
            return str
        }

        val MA_LENH = listOf(
                listOf("kép bằng", "kep bang", "00 11 22 33 44 55 66 77 88 99"),
                listOf("kép lệch", "kep lech", "05 50 16 61 27 72 38 83 49 94"),
                listOf("sát kép bằng", "sat kep bang", "01 10 12 21 23 32 34 43 54 45 65 56 67 76 78 87 89 98 "),
                listOf("sát kép lệch", "sat kep lech", "04 06 49 51 15 17 60 62 26 28 71 73 37 39 82 84 48 50 93 95"),
                listOf("dàn chẵn chẵn", "chan chan", "00 22 44 66 88 02 20 04 40 06 60 08 80 24 42 26 62 28 82 46 64 48 84 68 86"),
                listOf("dàn lẻ lẻ", "le le", "11 33 55 77 99 13 31 15 51 17 71 19 91 35 53 37 73 39 93 57 75 59 95 79 97"),
                listOf("dàn chẵn lẻ", "chan le", "01 03 05 07 09 21 23 25 27 29 41 43 45 47 49 61 63 65 67 69 81 83 85 87 89"),
                listOf("dàn lẻ chẵn", "le chan", "10 12 14 16 18 30 32 34 36 38 50 52 54 56 58 70 72 74 76 78 90 92 94 96 98"),
                listOf("Dàn nhỏ nhỏ", "nho nho", "00 11 22 33 44 01 10 02 20 03 30 04 40 12 21 13 31 14 41 23 32 24 42 34 43"),
                listOf("Dàn to to", "to to", "55 66 77 88 99 56 65 57 75 58 85 59 95 67 76 68 86 69 96 78 87 79 97 89 98"),
                listOf("Dàn nhỏ to ", "nho to", "05 06 07 08 09 15 16 17 18 19 25 26 27 28 29 35 36 37 38 39 45 46 47 48 49"),
                listOf("Dàn to nhỏ", "to nho", "90 91 92 93 94 80 81 82 83 84 70 71 72 73 74 60 61 62 63 64 50 51 52 53 54"),
                listOf("Dàn chia 3", "chia 3", "00 03 06 09 12 15 18 21 24 27 30 33 36 39 42 45 48 51 54 57 60 63 66 69 72 75 78 81 84 87 90 93 96 99"),
                listOf("Dàn chia 3 dư 1", "chia 3 du 1", "01 04 07 10 13 16 19 22 25 28 31 34 37 40 43 46 49 52 55 58 61 64 67 70 73 76 79 82 85 88 91 94 97"),
                listOf("Dàn chia 3 dư 2", "chia 3 du 2", "02 05 08 11 14 17 20 23 26 29 32 35 38 41 44 47 50 53 56 59 62 65 68 71 74 77 80 83 86 89 92 95 98"),

                listOf("bộ 01", "boj 01", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 10", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 06", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 60", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 51", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 15", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 56", "01 10 06 60 51 15 56 65"),
                listOf("bộ 01", "boj 65", "01 10 06 60 51 15 56 65"),

                listOf("bộ 02", "boj 02", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 20", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 07", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 70", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 52", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 25", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 57", "02 20 07 70 52 25 57 75"),
                listOf("bộ 02", "boj 75", "02 20 07 70 52 25 57 75"),

                listOf("bộ 03", "boj 03", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 30", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 08", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 80", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 53", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 35", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 58", "03 30 08 80 53 35 58 85"),
                listOf("bộ 03", "boj 85", "03 30 08 80 53 35 58 85"),

                listOf("bộ 04", "boj 04", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 40", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 09", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 90", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 54", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 45", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 59", "04 40 09 90 54 45 59 95"),
                listOf("bộ 04", "boj 95", "04 40 09 90 54 45 59 95"),

                listOf("bộ 12", "boj 12", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 21", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 17", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 71", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 62", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 26", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 67", "12 21 17 71 62 26 67 76"),
                listOf("bộ 12", "boj 76", "12 21 17 71 62 26 67 76"),

                listOf("bộ 13", "boj 13", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 31", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 18", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 81", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 63", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 36", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 68", "13 31 18 81 63 36 68 86"),
                listOf("bộ 13", "boj 86", "13 31 18 81 63 36 68 86"),

                listOf("bộ 14", "boj 14", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 41", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 19", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 91", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 64", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 46", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 69", "14 41 19 91 64 46 69 96"),
                listOf("bộ 14", "boj 96", "14 41 19 91 64 46 69 96"),

                listOf("bộ 23", "boj 23", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 32", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 28", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 82", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 73", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 37", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 78", "23 32 28 82 73 37 78 87"),
                listOf("bộ 23", "boj 87", "23 32 28 82 73 37 78 87"),

                listOf("bộ 24", "boj 24", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 42", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 29", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 92", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 74", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 47", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 79", "24 42 29 92 74 47 79 97"),
                listOf("bộ 24", "boj 97", "24 42 29 92 74 47 79 97"),

                listOf("bộ 34", "boj 34", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 43", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 39", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 93", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 84", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 48", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 89", "34 43 39 93 84 48 89 98"),
                listOf("bộ 34", "boj 98", "34 43 39 93 84 48 89 98"),

                listOf("bộ 00", "boj 00", "00 55 05 50"),
                listOf("bộ 00", "boj 55", "00 55 05 50"),
                listOf("bộ 00", "boj 05", "00 55 05 50"),
                listOf("bộ 00", "boj 50", "00 55 05 50"),

                listOf("bộ 11", "boj 11", "11 66 16 61"),
                listOf("bộ 11", "boj 66", "11 66 16 61"),
                listOf("bộ 11", "boj 16", "11 66 16 61"),
                listOf("bộ 11", "boj 61", "11 66 16 61"),

                listOf("bộ 22", "boj 22", "22 77 27 72"),
                listOf("bộ 22", "boj 77", "22 77 27 72"),
                listOf("bộ 22", "boj 27", "22 77 27 72"),
                listOf("bộ 22", "boj 72", "22 77 27 72"),

                listOf("bộ 33", "boj 33", "33 88 38 83"),
                listOf("bộ 33", "boj 88", "33 88 38 83"),
                listOf("bộ 33", "boj 38", "33 88 38 83"),
                listOf("bộ 33", "boj 83", "33 88 38 83"),

                listOf("bộ 44", "boj 44", "44 99 49 94"),
                listOf("bộ 44", "boj 99", "44 99 49 94"),
                listOf("bộ 44", "boj 49", "44 99 49 94"),
                listOf("bộ 44", "boj 94", "44 99 49 94"),

                listOf("đầu 0", "dau 0", "00 01 02 03 04 05 06 07 08 09"),
                listOf("đầu 1", "dau 1", "10 11 12 13 14 15 16 17 18 19"),
                listOf("đầu 2", "dau 2", "20 21 22 23 24 25 26 27 28 29"),
                listOf("đầu 3", "dau 3", "30 31 32 33 34 35 36 37 38 39"),
                listOf("đầu 4", "dau 4", "40 41 42 43 44 45 46 47 48 49"),
                listOf("đầu 5", "dau 5", "50 51 52 53 54 55 56 57 58 59"),
                listOf("đầu 6", "dau 6", "60 61 62 63 64 65 66 67 68 69"),
                listOf("đầu 7", "dau 7", "70 71 72 73 74 75 76 77 78 79"),
                listOf("đầu 8", "dau 8", "80 81 82 83 84 85 86 87 88 89"),
                listOf("đầu 9", "dau 9", "90 91 92 93 94 95 96 97 98 99"),
                listOf("đít 0", "dit 0", "00 10 20 30 40 50 60 70 80 90"),
                listOf("đít 1", "dit 1", "01 11 21 31 41 51 61 71 81 91"),
                listOf("đít 2", "dit 2", "02 12 22 32 42 52 62 72 82 92"),
                listOf("đít 3", "dit 3", "03 13 23 33 43 53 63 73 83 93"),
                listOf("đít 4", "dit 4", "04 14 24 34 44 54 64 74 84 94"),
                listOf("đít 5", "dit 5", "05 15 25 35 45 55 65 75 85 95"),
                listOf("đít 6", "dit 6", "06 16 26 36 46 56 66 76 86 96"),
                listOf("đít 7", "dit 7", "07 17 27 37 47 57 67 77 87 97"),
                listOf("đít 8", "dit 8", "08 18 28 38 48 58 68 78 88 98"),
                listOf("đít 9", "dit 9", "09 19 29 39 49 59 69 79 89 99"),

                listOf("chạm 0", "cham 0", "01 10 02 20 03 30 04 40 05 50 06 60 07 70 08 80 09 90 00"),
                listOf("chạm 1", "cham 1", "01 10 12 21 13 31 14 41 15 51 16 61 17 71 18 81 19 91 11"),
                listOf("chạm 2", "cham 2", "02 20 12 21 23 32 24 42 25 52 26 62 27 72 28 82 29 92 22"),
                listOf("chạm 3", "cham 3", "03 30 13 31 23 32 33 43 34 53 35 63 36 73 37 83 38 93 39"),
                listOf("chạm 4", "cham 4", "04 40 14 41 24 42 34 43 44 54 45 64 46 74 47 84 48 94 49"),
                listOf("chạm 5", "cham 5", "05 50 15 51 25 52 35 53 45 54 55 65 56 75 57 85 58 95 59"),
                listOf("chạm 6", "cham 6", "06 60 16 61 26 62 36 63 46 64 56 65 66 76 67 86 68 96 69"),
                listOf("chạm 7", "cham 7", "07 70 17 71 27 72 37 73 47 74 57 75 67 76 77 87 78 97 79"),
                listOf("chạm 8", "cham 8", "08 80 18 81 28 82 38 83 48 84 58 85 68 86 78 87 88 98 89"),
                listOf("chạm 9", "cham 9", "09 90 19 91 29 92 39 93 49 94 59 95 69 96 79 97 89 98 99"),

                listOf("tổng 0", "tong 0", "19 91 28 82 37 73 46 64 55 00"),
                listOf("tổng 1", "tong 1", "01 10 29 92 38 83 47 74 56 65"),
                listOf("tổng 2", "tong 2", "02 20 39 93 48 84 57 75 11 66"),
                listOf("tổng 3", "tong 3", "03 30 12 21 49 94 58 85 67 76"),
                listOf("tổng 4", "tong 4", "04 40 13 31 59 95 68 86 22 77"),
                listOf("tổng 5", "tong 5", "05 50 14 41 23 32 69 96 78 87"),
                listOf("tổng 6", "tong 6", "06 60 15 51 24 42 79 97 33 88"),
                listOf("tổng 7", "tong 7", "07 70 16 61 25 52 34 43 89 98"),
                listOf("tổng 8", "tong 8", "08 80 17 71 26 62 35 53 44 99"),
                listOf("tổng 9", "tong 9", "09 90 18 81 27 72 36 63 45 54"))
    }
}