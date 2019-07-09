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
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.hally.lotsms.R
import com.hally.lotsms.common.util.LodeUtil
import com.hally.lotsms.common.util.LodeUtil.Companion.SIGNAL
import com.hally.lotsms.common.util.LodeUtil.Companion.SIGNX
import com.hally.lotsms.common.util.LodeUtil.Companion.TEST
import com.hally.lotsms.model.Lode
import kotlinx.android.synthetic.main.backup_controller.*
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
    private var position: Int = 0
    private var rows: ArrayList<View> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        message = arguments?.getString(MESSAGE).toString()
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

//        message = "Đánh cho tao lô chan   chan 10diem 10x   21d 20x20d, 22   5 diem, 11x5, de 20x20k, bộ   01    100n"
        message = TEST
//        message = message.removeSpace()
        body.setText(message)
        reload.setOnClickListener {
            message = body.text.toString()
            handleLodeText()
        }

        handleLodeText()


        lode_chot.setOnClickListener {
            var valid = false
            rows.forEach { row ->
                val lode = Lode()
                valid = checkValidLode(row, lode)
                if (valid) {
                    callback!!.onPositiveButtonClicked(lode)
                    dialog?.dismiss()
                }
            }
        }
        lode_huy.setOnClickListener {
            dialog?.dismiss()
        }
        lode_type.setOnClickListener {
            (it as TextView).text = TYPE[(++position) % TYPE.size].name.toUpperCase()
        }
    }

    private fun handleLodeText() {
        // xử lý chia các view LÔ ĐỀ riêng
        message = message.removeSpace()
        message = LodeUtil.removeVietnamese(message)
        var index = 0
        while (index != -1) {
            var type = ""
            index = -1
            for (key in TYPE) {
                if (index < message.lastIndexOf(key.name)) {
                    index = message.lastIndexOf(key.name)
                    type = key.name
                }
            }

            if (index != -1) {
                val row = inflater.inflate(R.layout.lode_view_row, null)
                rows.add(row)

                row.lode_type.text = type.toUpperCase()
                // cắt bỏ chữ lô đề.
                row.lode_number.setText(message.substring(index + type.length).trim())
                message = message.substring(0, index)
            }
        }
        rows.reverse()
        lode_container.removeAllViews()
        rows.forEach { row -> lode_container.addView(row) }


        // xử lý chuẩn format x điểm lô đề : 'x'
        rows.forEach { row ->
            var text = row.lode_number.text.toString()
            var i = 1
            while (i < text.length) { // xóa SPACE trước sau các SIGNAL
                for ((j, key) in SIGNAL.withIndex()) {
                    if (i < text.length && key == text[i]) {
                        if (j <= 2) {
                            if (i < text.length && SPACE == text[i + 1].toString())
                                text = text.removeRange(i + 1, i + 2)
                        } else {
                            if (i >= 1 && SPACE == text[i - 1].toString())
                                text = text.removeRange(i - 1, i)
                        }
                    }
                }
                i++
            }

            // chuyển hết về dạng  5 4 3x99
            val arrs = text.split(SPACE).toMutableList()
            for ((i, it) in arrs.withIndex()) {
                for (key in SIGNAL) {
                    val num = removeText(it)
                    if (it.contains(key) && num.isNotBlank() && num.isDigitsOnly()) {
                        arrs[i] = "$SIGNX$num" /*+ if (row.lode_type.text == "LO") "d" else "k"*/
                    } else if (it.contains(key) && num.split(SPACE).size == 2) {
                        arrs[i] = num.replace(SPACE, SIGNX.toString())
                    }
                }
            }
            row.lode_number.setText(arrs.toText())
        }
    }

//    private fun findEndIndex(text: String, index: Int): Int {
//        var isNumber = false
//        var start = 0
//        for ((i, c) in text.withIndex()) {
//            if (i > index && c.isDigit()) {
//                start = i
//                isNumber = true
//            }
//            if (isNumber && !c.isDigit()) {
//                diem = text.substring(start, i)
//                return i
//            }
//        }
//        diem = text.substring(start, text.length)
//        return text.length
//    }

    private fun checkValidLode(row: View, lode: Lode): Boolean {
        var result = true
        val type = row.lode_type.text
        val number = row.lode_number.text.toString()

        // Tách mã lệnh: 1 bên mã, 1 bên số điểm
        val lenh: HashMap<String, String> = HashMap()
        var start = 0
        for ((i, it) in number.withIndex()) {
            if (SIGNX == it) {
                val end = number.indexOf(SPACE, i, false)
                lenh[number.substring(start, i)] = number.substring(i, end)
                start = end + 1
            }
        }

        // tìm mã lệnh
        val maLenh = LodeUtil.MA_LENH.map { it[1] }
        val value = LodeUtil.MA_LENH.map { it[2] }
        for (ma in lenh.keys) {
            for ((index, key) in maLenh.withIndex()) {
                if (ma.contains(key) && ma.replace(key, "").removeSpace().isEmpty()) {
                    if (lenh[ma]!!.removeText().isDigitsOnly()) {
                        row.lode_number.floatingLabelText = value[index] + SIGNX + lenh[ma]!!.removeText()
                    }
                }
            }

            if (row.lode_number.floatingLabelText.isNullOrBlank()) {
                if (lenh[ma]!!.removeText().isDigitsOnly()) {
                    var check = true
                    for (n in ma.removeText().split(SPACE)){
                        check = n.isDigitsOnly()
                    }
                    if (check) row.lode_number.floatingLabelText = ma.removeText() + SIGNX + lenh[ma]!!.removeText()
                }
            }
        }

        if (row.lode_number.floatingLabelText.isNullOrBlank()) {
            row.lode_number.error = "Kiểm tra lại số LÔ ĐỀ!!"
            result = false
        }

        return false
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
        val TYPE = Type.values()
        val SPACE = " "
    }

    enum class Type { de, lo, xien, bc }
}

private fun String.removeText(): String {
    // tra nay day cac so phan tach = dau SPACE
    return this.replace("[^0-9]".toRegex(), LodeDialog.SPACE).replace("\\s+".toRegex(), LodeDialog.SPACE).trim()
}

private fun String.removeSpace(): String {
    return this.replace("\\s+".toRegex(), LodeDialog.SPACE).trim()
}

private fun <E> MutableList<E>.toText(): String {
    val builder = StringBuffer()
    this.forEach { builder.append(it.toString() + " ") }
    return builder.toString()
}
