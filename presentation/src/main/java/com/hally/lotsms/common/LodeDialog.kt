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

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.hally.lotsms.R
import com.hally.lotsms.common.util.LodeUtil
import com.hally.lotsms.common.util.LodeUtil.Companion.SIGNAL
import com.hally.lotsms.common.util.LodeUtil.Companion.SIGNX
import com.hally.lotsms.common.util.removeSpace
import com.hally.lotsms.common.util.removeText
import com.hally.lotsms.common.util.toText
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
        setStyle(STYLE_NO_FRAME, R.style.MyBottomDialogs)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = inflater
        return inflater.inflate(R.layout.lode_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//        dialog.window!!.setGravity(Gravity.BOTTOM)
//        dialog.setTitle("Xử lý Lô Đề")
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (callback == null || message.isBlank()) {
            return
        }

//        message = "xi2 202x100k, xien3 11.22,33 300k, xi4 12,23,34,45 x200, Xiên quây 050.94.63 x 1tr, và xq 50.23.63.07 x300k" +
//                ",3 cang 876 ,234 x120k 111 100,.,200 x200k Đe giai nhat 79 97 12x200 " +
//                "lo chan chan x9  22,23,20x20d, 63  5 diem, 11x5, de 20x20k, bộ  01 100n"
//        message = TEST
        body.setText(message)
//        message = message.removeSpace()
        handleLodeText()

        // set listener
        reload.setOnClickListener {
            message = body.text.toString()
            Toast.makeText(context, "Load: $message", Toast.LENGTH_LONG).show()
            handleLodeText()
        }
        lode_chot.setOnClickListener {
            var valid = true
            val lode = Lode().init()
            rows.forEach { row: View -> valid = valid && isValidRow(row, lode) }

            if (valid) {
//                Log.i("TNS", "Result : $lode")
                callback!!.onPositiveButtonClicked(lode)
                dialog?.dismiss()
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
        // xử lý tiền đề text
        message = LodeUtil.removeVietnamese(message)

        // tìm kiếm loại
        var index = 0
        while (index != -1) {
            var type = ""
            index = -1
            for (key in TYPE) {
                if (index < message.lastIndexOf(key)) {
                    index = message.lastIndexOf(key)
                    type = key
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
        rows.forEach { lode_container.addView(it) }

        rows.forEach { row ->
            isValidRow(row, Lode().init())
            row.lode_number.setOnFocusChangeListener { v, hasFocus ->
                row.lode_number_bubble.visibility = if (hasFocus) View.VISIBLE else View.GONE
            }
            row.lode_type.setOnClickListener {
                (it as TextView).text = TYPE[(++position) % TYPE.size].toUpperCase()
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

    private fun isNotCode(s: String): Boolean {
        return s != "dau" && s != "dit" && s != "du " && s != "nho" && s != "kep"
    }

    private fun isValidRow(row: View, lode: Lode): Boolean {

        // xử lý chuẩn format x điểm lô đề : 'x'
        var text = row.lode_number.text.toString()
//        Log.i("TNS", "Before: ${row.lode_type.text} $text")
        var i = 1
        while (i < text.length) { // xóa SPACE trước sau các SIGNAL
            for ((j, key) in SIGNAL.withIndex()) {
                if (i < text.length && key == text[i]) {
                    if (j <= 2) { // 'x', '*', '/',
                        if (i + 1 < text.length && SPACE == text[i + 1].toString())
                            text = text.removeRange(i + 1, i + 2)
                    } else { // 'd', 'n', 'k'
                        if (i >= 1 && SPACE == text[i - 1].toString())
                            if (i + 2 < text.length && isNotCode(text.substring(i, i + 3)))
                                text = text.removeRange(i - 1, i)
                    }
                }
            }
            i++
        }

        // chuyển hết về dạng:  5 4 3x99
        val arrs = text.split(SPACE).toMutableList()
        for ((i, it) in arrs.withIndex()) {
            for (key in SIGNAL) {
                val num = it.removeText()
                if (it.contains(key) && num.isNotBlank() && num.isDigitsOnly()) {
                    arrs[i] = "$SIGNX$num"
                } else if (it.contains(key) && num.split(SPACE).size == 2) {
                    arrs[i] = num.replace(SPACE, SIGNX.toString())
                }
            }
        }
        row.lode_number.setText(arrs.toText())

        val type = row.lode_type.text.toString()
        val number = row.lode_number.text.toString()
        Log.i("TNS", "After: $type $text")
        if (number.removeSpace().isBlank()) return true

        // Tách mã lệnh: 1 bên mã, 1 bên số điểm
        val lenh: ArrayList<Pair<String, String>> = ArrayList()
        var start = 0
        for ((i, it) in number.withIndex()) {
            if (SIGNX == it && i >= start) {
                var end = findEndIndex(number, i)
                if (end < i) end = number.length
                lenh.add(Pair(number.substring(start, i).trim(), number.substring(i, end).trim()))
                start = end + 1
            }
        }

        // tìm mã lệnh lô đề
        val numbers = lenh.map { it.first }
        val points = lenh.map { it.second }
        val builder = StringBuilder()
        val codes = LodeUtil.MA_LENH.map { it[1] }
        val values = LodeUtil.MA_LENH.map { it[2] }

        var isValid = true
        for ((k, num) in numbers.withIndex()) {
            var flag = true
            val point = points[k].removeText()
            Log.i("TNS", "$type<$num|$point>")
            if (point.isBlank() || !point.isDigitsOnly()) {
                isValid = false
                break
            }

            // xử lý 3 càng
            if (type.equalTo(E.BC)) {
                var check = true
                val bd = StringBuilder()
                for (n in num.removeText().split(SPACE)) {
                    check = n.isNotBlank() && n.isDigitsOnly() && (n.length == 3)
                    if (check) bd.append(n).append(SPACE)
                    else break
                }

                if (check && point.isDigitsOnly()) {
                    builder.append("$bd$SIGNX$point, ")
                    addOther(lode.bc, bd.toString(), point)
                    flag = false
                }
                isValid = isValid && !flag
                flag = false
            }

            if (flag) {
                // xử lý theo mẫu mã sẵn có
                for ((index, key) in codes.withIndex()) {
                    if (num.contains(key)) {
                        if (point.isDigitsOnly() &&
                                num.replace(key, "").removeSpace().isBlank()) {
                            builder.append(values[index] + SIGNX + point + ", ")
                            addLode(lode.byType(type), values[index], point)
                        } else isValid = false
                        flag = false
                    }
                }
            }

            if (flag) {
                var check = true
                var bd = StringBuilder()
//                Log.i("TNS", num.removeText())
                for (n in num.removeText().split(SPACE)) {
                    check = n.isNotBlank() && n.isDigitsOnly()
                    if (check && n.length == 2) {
                        bd.append(n).append(SPACE)
                    } else if (check && n.length == 3 && n[0] == n[2]) {
                        bd.append(n.substring(0, 2)).append(SPACE)
                        bd.append(n.substring(1, 3)).append(SPACE)
                    } else check = false

                    if (!check) break
                }

//                Log.i("TNS", "$check " + point)
                if (check && point.isDigitsOnly()) {
                    if (type.equalTo(E.XQ)) {
                        bd = addXienQuay(lode.xien, bd, point)
                        builder.append(bd)
                    } else {
                        builder.append("$bd$SIGNX$point, ")
                        if (type.equalTo(E.XIEN)) lode.xien.add("$bd$SIGNX$point")
                        else addLode(lode.byType(type), bd.toString(), point)
                    }

                    flag = false
                }
            }

            // check valid tat ca cac code vs lenh??
            isValid = isValid && !flag
//            if (!isValid) break
//            Log.i("TNS", "isValid : $isValid")
        }

        if (builder.isNotBlank()) {
            row.lode_number.setTextColor(Color.BLACK)
            row.lode_number.floatingLabelText = builder.delete(builder.length - 2, builder.length - 1)
            val t = row.lode_number?.floatingLabelText?.toString()
            row.lode_number_bubble.text = t?.replace(", ", "\n")
        }
        if (!isValid) {
            row.lode_number.setTextColor(Color.RED)
            row.lode_number.error = "Kiểm tra lại số LÔ ĐỀ!!"
        }

        return isValid
    }

    private fun addXienQuay(xqArr: RealmList<String>, bd: StringBuilder, point: String): StringBuilder {
        val arr = bd.toString().removeSpace().split(SPACE)
        bd.clear()
        val n = arr.size
        for (i in 2..n) {
            val compa = LodeUtil.genCombina(n, i)
            compa.forEach {
                val item = it.map { i -> arr[i] }.toString().removeText() + "$SIGNX$point"
                xqArr.add(item)
                bd.append("$item, ")
            }
        }
        return bd
    }

    private fun addOther(arr: RealmList<String>, num: String, point: String) {
        for (n in num.removeSpace().split(SPACE))
            arr.add("$n$SIGNX$point")
        Log.d("TNS", "addOther : $num : $point  => $arr")
    }

    private fun addLode(lode: RealmList<Int>, numbers: String, point: String) {
        for (num in numbers.removeSpace().split(SPACE)) {
            val value = lode[num.toInt()]!!
            lode[num.toInt()] = value.plus(point.toInt())
        }
//        Log.d("TNS", "addLode : $numbers : $point  => $lode")
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
        val TYPE = arrayOf("lo", "de giai nhat", "de", "3 cang", "xien", "xq")
        val SPACE = " "

        enum class E(val vni: String, val price: Int) {
            LO("Lô", 80),
            DE1("Đề giải nhất", 70), DE("Đề", 70), BC("Ba Càng", 400),
            XIEN("Lô Xiên", 1), XQ("Xiên quay", 1)
        }
    }
}

private fun String.equalTo(e: LodeDialog.Companion.E): Boolean {
    return toLowerCase() == LodeDialog.TYPE[e.ordinal]
}
