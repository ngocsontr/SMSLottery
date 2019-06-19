/*
 *
 * Copyright 2018
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.hally.lotsms.R

/**
 * Created by HallyTran on 25.05.2019.
 */
class LodeDialog : DialogFragment() {

    private var callback: Callback? = null
    private var name: String? = null
    private var avatar: Long? = null
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        name = arguments?.getString(NAME)
        avatar = arguments?.getLong(AVATAR)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Material_Light_Dialog_MinWidth)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lode_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (callback == null) {
            throw RuntimeException(activity?.title.toString() + " should implements Callback")
        }

        view.findViewById<TextView>(R.id.body).text = name
        view.findViewById<View>(R.id.lode_chot).setOnClickListener {
            callback!!.onPositiveButtonClicked("@")
            dialog?.dismiss()
        }
        view.findViewById<View>(R.id.lode_huy).setOnClickListener {
            dialog?.dismiss()
        }
        view.findViewById<TextView>(R.id.lode_bt).setOnClickListener {
            (it as TextView).text = LODE[(++position) % LODE.size]
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }


    interface Callback {
        fun onPositiveButtonClicked(password: String)
    }

    companion object {
        val TAG = LodeDialog::class.java.simpleName
        val NAME = "RemoveSNSConfirmDialog.NAME"
        val AVATAR = "RemoveSNSConfirmDialog.AVATAR"
        val LODE = arrayOf("Lô", "Đề", "3C")
    }
}
