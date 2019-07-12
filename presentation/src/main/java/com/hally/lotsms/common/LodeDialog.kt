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

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import io.realm.RealmList
import kotlinx.android.synthetic.main.lode_dialog.*
import kotlinx.android.synthetic.main.lode_view_row.view.*

/**
 * Created by HallyTran on 25.05.2019.
 */
open class LodeDialog : DialogFragment() {

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
//        message = TEST
//        message = message.removeSpace()
        body.setText(message)
        reload.setOnClickListener {
            message = body.text.toString()
            Toast.makeText(context, "Load: $message", Toast.LENGTH_LONG).show()
            handleLodeText()
        }

        handleLodeText()
        rows.forEach { row -> checkValidLode(row, Lode()) }

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
    }

    private fun handleLodeText() {
        rows.clear()
        lode_container.removeAllViews()
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
            row.lode_number.setOnFocusChangeListener { v, hasFocus ->
                val t = row.lode_number?.floatingLabelText?.toString()
                row.lode_number_bubble.text = t?.replace(",", "\n")
                row.lode_number_bubble.visibility = if (hasFocus) VISIBLE else GONE
            }
            row.lode_type.setOnClickListener {
                (it as TextView).text = TYPE[(++position) % TYPE.size].name.toUpperCase()
            }
        }
    }

    private fun findEndIndex(text: String, index: Int): Int {
        var isNumber = false
        for ((i, c) in text.withIndex()) {
            if (i > index && c.isDigit()) isNumber = true
            if (isNumber && !c.isDigit()) return i
        }
        return text.length
    }

    private fun checkValidLode(row: View, lode: Lode): Boolean {
        val type = row.lode_type.text.toString()
        val number = row.lode_number.text.toString()

        // Tách mã lệnh: 1 bên mã, 1 bên số điểm
        val lenh: ArrayList<Pair<String, String>> = ArrayList()
        var start = 0
        for ((i, it) in number.withIndex()) {
            if (SIGNX == it && i >= start) {
                var end = findEndIndex(number, i)
                if (end < i) end = number.length
                lenh.add(Pair(number.substring(start, i), number.substring(i, end)))
                start = end + 1
            }
        }
        for (l in lenh) Log.i("TNS", l.toString())

        // tìm mã lệnh
        val numbers = lenh.map { it.first }
        val points = lenh.map { it.second }
        val builder = StringBuilder()
        val codes = LodeUtil.MA_LENH.map { it[1] }
        val values = LodeUtil.MA_LENH.map { it[2] }

        var isValid = true
        for ((k, num) in numbers.withIndex()) {
            var b = true
            val point = points[k]
            for ((index, key) in codes.withIndex()) {
                if (num.contains(key)) {
                    if (point.removeText().isDigitsOnly() && num.replace(key, "").removeSpace().isBlank()) {
                        builder.append(values[index] + SIGNX + point.removeText() + ", ")
                        addLode(lode.byType(type), values[index], point.removeText())
                    }
                    b = false
                }
            }

            if (b) {
                var check = true
                val bd = StringBuilder()
//                Log.i("TNS", num.removeText())
                for (n in num.removeText().split(SPACE)) {
                    check = n.isNotBlank() && n.isDigitsOnly()
                    if (check && n.toInt() < 100) {
                        bd.append(n).append(SPACE)
                    } else if (check && n.toInt() >= 100 && n.toInt() < 1000 && n[0] == n[2]) {
                        bd.append(n.substring(0, 2)).append(SPACE)
                        bd.append(n.substring(1, 3)).append(SPACE)
                    } else check = false

                    if (!check) break
                }

//                Log.i("TNS", "$check " + point.removeText())
                if (check && point.removeText().isDigitsOnly()) {
                    builder.append(bd.toString() + SIGNX + point.removeText() + ", ")
                    addLode(lode.byType(type), bd.toString(), point.removeText())
                    b = false
                }
            }
            isValid = isValid && !b
//            if (!isValid) break
        }

        if (builder.isNotBlank()) {
            row.lode_number.setTextColor(Color.BLACK)
            row.lode_number.floatingLabelText = builder.delete(builder.length - 2, builder.length - 1)
        }
        if (!isValid || builder.isBlank()) {
            row.lode_number.setTextColor(Color.RED)
            row.lode_number.error = "Kiểm tra lại số LÔ ĐỀ!!"
        }

        return false
    }

    private fun addLode(lode: RealmList<Int>, numbers: String, point: String) {
        for (num in numbers.removeSpace().split(SPACE)) {
            lode[num.toInt()]?.plus(point.toInt())
        }
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

        enum class Type { de, lo, xien, bc }
    }
}

private fun String.removeText(): String {
    // tra nay day cac so phan tach = dau SPACE
    return this.replace("[^0-9]".toRegex(), LodeDialog.SPACE).replace("\\s+".toRegex(), LodeDialog.SPACE).trim()
}

private fun String.removeSpace(): String {
    // remove duplicate SPACE
    return this.replace("\\s+".toRegex(), LodeDialog.SPACE).trim()
}

private fun <E> MutableList<E>.toText(): String {
    val builder = StringBuffer()
    this.forEach { builder.append(it.toString() + " ") }
    return builder.toString()
}
