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
import com.hally.lotsms.model.Lode
import kotlinx.android.synthetic.main.lode_dialog.*

/**
 * Created by HallyTran on 25.05.2019.
 */
class LodeDialog : DialogFragment() {

    private var callback: Callback? = null
    private lateinit var message: String
    private var avatar: Long? = null
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        message = arguments?.getString(MESSAGE).toString()
        avatar = arguments?.getLong(AVATAR)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Material_Light_Dialog_MinWidth)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lode_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (callback == null || message.isBlank()) {
            return
        }

        lode_bt.text = TYPE[position].name

//        message = "Đánh cho tao lô chan chan 10 diem"
        message = removeVietnamese(message)
        view.findViewById<TextView>(R.id.body).text = message
        message = message.toLowerCase()

        for (key in TYPE)
            if (message.contains(key.name)) {
                lode_bt.text = key.name.toUpperCase()
                position = TYPE.indexOf(key)
                message = message.substring(message.indexOf(key.name))
                val s = removeText(message)
                val arr = s.split(" ").toMutableList()
                lode_so_diem.setText(arr[arr.size - 1])
                arr.removeAt(arr.size - 1)
                lode_number.setText(removeText(arr.toString()))
            }

        // tìm mã lệnh
        val maLenh = LodeUtil.MA_LENH.map { it[1] }
        for (key in maLenh)
            if (message.contains(key)) {
                val value = LodeUtil.MA_LENH.map { it[2] }
                lode_number.setText(value[maLenh.indexOf(key)])
            }


        view.findViewById<View>(R.id.lode_chot).setOnClickListener {
            if (checkValidLode()) {
                val lode = Lode()
                lode.lodeType = TYPE[position % TYPE.size]
                lode.body = lode_number.text.toString()
                lode.diem = lode_so_diem.text.toString().toInt()
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

    private fun checkValidLode(): Boolean {
        val number = lode_number.text
        if (number?.trim().isNullOrBlank()) {
            Toast.makeText(activity, "Không bỏ trống, số LÔ ĐỀ!!", Toast.LENGTH_LONG).show()
            return false
        }

        val arr = number?.split(REGIX)
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

        if (lode_so_diem.text?.trim().isNullOrBlank()) {
            Toast.makeText(activity, "Không bỏ trống, bao nhiêu ĐIỂM!!", Toast.LENGTH_LONG).show()
            return false
        }
        if (lode_so_diem.text.toString().toInt() < 1) return false

        return true
    }

    private fun removeText(txt: String): String {
        return txt.replace("[^0-9]".toRegex(), REGIX).replace("\\s+".toRegex(), REGIX).trim()
    }

    private fun removeVietnamese(s: String): String {
//        Toast.makeText(activity, "Xóa Vietnamese!!", Toast.LENGTH_LONG).show()
        var str = s
//        str = str.toLowerCase()
        str = str.trim()

        //Thay thế và lọc dấu từng char
        for (i in 1 until VietNamChar.size) {
            for (j in 0 until VietNamChar[i].length)
                str = str.replace(VietNamChar[i][j], VietNamChar[0][i - 1])
        }
        return str
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }


    interface Callback {
        fun onPositiveButtonClicked(lode: Lode)
    }

    companion object {
        val TAG = LodeDialog::class.java.simpleName
        val MESSAGE = "RemoveSNSConfirmDialog.MESSAGE"
        val AVATAR = "RemoveSNSConfirmDialog.AVATAR"

        val TYPE = Lode.Type.values()
        val REGIX = " "
        private var VietNamChar = arrayOf("aAeEoOuUiIdDyY", "áàạảãâấầậẩẫăắằặẳẵ", "ÁÀẠẢÃÂẤẦẬẨẪĂẮẰẶẲẴ", "éèẹẻẽêếềệểễ", "ÉÈẸẺẼÊẾỀỆỂỄ", "óòọỏõôốồộổỗơớờợởỡ", "ÓÒỌỎÕÔỐỒỘỔỖƠỚỜỢỞỠ", "úùụủũưứừựửữ", "ÚÙỤỦŨƯỨỪỰỬỮ", "íìịỉĩ", "ÍÌỊỈĨ", "đ", "Đ", "ýỳỵỷỹ", "ÝỲỴỶỸ")
    }
}
