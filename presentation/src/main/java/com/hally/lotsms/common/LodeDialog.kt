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
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import com.hally.lotsms.R
import kotlinx.android.synthetic.main.lode_dialog.*


/**
 * Created by HallyTran on 25.05.2019.
 */
class LodeDialog : DialogFragment() {

    private var callback: Callback? = null
    private var name: String? = null
    private var avatar: Long? = null
    private var alert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        name = arguments.getString(NAME)
        avatar = arguments.getLong(AVATAR)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (callback == null) {
            throw RuntimeException(activity.title.toString() + " should implements Callback")
        }
        val layoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.lode_dialog, null)
        body.text = name

        val builder = AlertDialog.Builder(activity)
        builder.setView(view)
                .setPositiveButton(R.string.lode_chot) { _: DialogInterface, _: Int ->
                    callback!!.onPositiveButtonClicked("@")
                }

        alert = builder.create()
        alert!!.setCancelable(false)
        return alert as AlertDialog
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
    }
}
