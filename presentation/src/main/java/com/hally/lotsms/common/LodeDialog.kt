/*
 *
 * Copyright 2018
 *
 *    Licensed under the Apache License, Version 2.0 (the "License")
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.hally.lotsms.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.hally.lotsms.R
import com.hally.lotsms.common.util.LodeUtil
import com.hally.lotsms.common.util.LodeUtil.Companion.SIGNAL
import com.hally.lotsms.common.util.LodeUtil.Companion.SIGNX
import com.hally.lotsms.model.Lode
import kotlinx.android.synthetic.main.lode_dialog.*
import kotlinx.android.synthetic.main.lode_view_row.*
import kotlinx.android.synthetic.main.lode_view_row.view.*

/**
 * Created by HallyTran on 25.05.2019.
 */
class LodeDialog : DialogFragment() {

    private lateinit var inflater: LayoutInflater
    private lateinit var message: String
    private var callback: Callback? = null
    private var avatar: Long? = null
    private var position: Int = 0
    private var rows: ArrayList<View> = ArrayList()

    private var diem = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        message = arguments?.getString(MESSAGE).toString()
        avatar = arguments?.getLong(AVATAR)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Material_Light_Dialog_MinWidth)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = inflater
        return inflater.inflate(R.layout.lode_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (callback == null || message.isBlank()) {
            return
        }

//        lode_bt.text = TYPE[position].name
        message = "Đánh cho tao lô chan   chan 10diem 10x   21d 20x20d, 22   5 diem, 11x5, de 20x20k, bộ   01    100n"
        message = message.removeSpace()
        view.findViewById<TextView>(R.id.body).text = message
        message = LodeUtil.removeVietnamese(message)
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()

        // xử lý chia các view LÔ ĐỀ riêng
        var index = 0
        while (index != -1) {
            for (key in TYPE) {
                index = message.lastIndexOf(key.name)
                if (index != -1) {
                    val row = inflater.inflate(R.layout.lode_view_row, null)
                    rows.add(row)
                    row.lode_bt.text = key.name.toUpperCase()
                    // cắt bỏ chữ lô đề.
                    row.lode_number.setText(message.substring(index + key.name.length))
                    message = message.substring(0, index - 1)
                    break
                }
            }
        }
        rows.reverse()
        rows.forEach { row -> lode_container.addView(row) }


        // xử lý chuẩn format x điểm lô đề : 'x'
        rows.forEach { row ->
            var text = row.lode_number.text.toString()
            for (i in 0 until text.length) {
                for ((j, key) in SIGNAL.withIndex()) {
                    if (key.equals(text[i])) {
                        if (j <= 2) {
                            if (SPACE.equals(text[i + 1]))
                                text = text.removeRange(i + 1, i + 2)
                        } else {
                            if (SPACE.equals(text[i - 1]))
                                text = text.removeRange(i - 1, i)
                        }
                    }
                }
            }

            val arrs = text.split(SPACE).toMutableList()
            for ((i, it) in arrs.withIndex()) {
                for (key in SIGNAL) {
                    val num = removeText(it)
                    if (it.contains(key) && num.isNotBlank() && num.isDigitsOnly()) {
                        arrs[i] = "$SIGNX$num" /*+ if (row.lode_bt.text == "LO") "d" else "k"*/
                    } else if (it.contains(key) && num.split(SPACE).size == 2) {
                        arrs[i] = num.replace(SPACE, SIGNX)
                    }
                }
            }
            row.lode_number.setText(arrs.toText())
        }

//        // tìm mã lệnh
//        val maLenh = LodeUtil.MA_LENH.map { it[1] }
//        for (key in maLenh)
//            if (message.contains(key)) {
//                val value = LodeUtil.MA_LENH.map { it[2] }
//                lode_number.setText(value[maLenh.indexOf(key)])
//            }


        view.findViewById<View>(R.id.lode_chot).setOnClickListener {
            if (checkValidLode()) {
                val lode = Lode()
                lode.lodeType = TYPE[position % TYPE.size]
                lode.body = lode_number.text.toString()
//                lode.diem = lode_so_diem.text.toString().toInt()
                callback!!.onPositiveButtonClicked(lode)

                dialog?.dismiss()
            }
        }
        view.findViewById<View>(R.id.lode_huy).setOnClickListener {
            dialog?.dismiss()
        }
        view.findViewById<TextView>(R.id.lode_bt).setOnClickListener {
            (it as TextView).text = TYPE[(++position) % TYPE.size].name.toUpperCase()
        }
    }

    private fun findEndIndex(text: String, index: Int): Int {
        var isNumber = false
        var start = 0
        for ((i, c) in text.withIndex()) {
            if (i > index && c.isDigit()) {
                start = i
                isNumber = true
            }
            if (isNumber && !c.isDigit()) {
                diem = text.substring(start, i)
                return i
            }
        }
        diem = text.substring(start, text.length)
        return text.length
    }

    private fun checkValidLode(): Boolean {
        val number = lode_number.text
        if (number?.trim().isNullOrBlank()) {
            Toast.makeText(activity, "Không bỏ trống, số LÔ ĐỀ!!", Toast.LENGTH_LONG).show()
            return false
        }

        val arr = number?.split(SPACE)
        if (arr != null) {
            for (num in arr) {
                if (num.isBlank() || !num.isDigitsOnly()) {
                    lode_number.setText(removeText(number.toString()))
                    Toast.makeText(activity, "Xóa ký tự chữ, kiểm tra lại xem!!", Toast.LENGTH_LONG).show()
                    return false
                }

                if (num.toInt() !in 0..99) {
                    Toast.makeText(activity, "Chỉ đánh số: từ 00 đến 99!!", Toast.LENGTH_LONG).show()
                    return false
                }
            }

        } else if (number?.toString()?.toInt() !in 0..99) {
            Toast.makeText(activity, "Chỉ đánh số: từ 00 đến 99!!", Toast.LENGTH_LONG).show()
            return false
        }

//        if (lode_so_diem.text?.trim().isNullOrBlank()) {
//            Toast.makeText(activity, "Không bỏ trống, bao nhiêu ĐIỂM!!", Toast.LENGTH_LONG).show()
//            return false
//        }
//        if (lode_so_diem.text.toString().toInt() < 1) return false

        return true
    }

    private fun removeText(txt: String): String {
        // tra nay day cac so phan tach = dau SPACE
        return txt.replace("[^0-9]".toRegex(), SPACE).replace("\\s+".toRegex(), SPACE).trim()
    }


    fun setCallback(callback: Callback) {
        this.callback = callback
    }


    interface Callback {
        fun onPositiveButtonClicked(lode: Lode)
    }

    companion object {
        val TAG: String = LodeDialog::class.java.simpleName
        val MESSAGE = "RemoveSNSConfirmDialog.MESSAGE"
        val AVATAR = "RemoveSNSConfirmDialog.AVATAR"

        val TYPE = Lode.Type.values()
        val SPACE = " "
    }
}

private fun String.removeSpace(): String {
    return this.replace("\\s+".toRegex(), LodeDialog.SPACE).trim()
}

private fun <E> MutableList<E>.toText(): String {
    val builder = StringBuffer()
    this.forEach { builder.append(it.toString() + " ") }
    return builder.toString()
}
