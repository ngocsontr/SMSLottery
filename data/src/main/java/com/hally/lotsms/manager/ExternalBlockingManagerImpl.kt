/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hally.lotsms.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.core.os.bundleOf
import com.hally.lotsms.util.Preferences
import com.hally.lotsms.util.tryOrNull
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import timber.log.Timber
import javax.inject.Inject

class ExternalBlockingManagerImpl @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) : ExternalBlockingManager {

    companion object {
        const val RATING_UNKNOWN = 0
        const val RATING_POSITIVE = 1
        const val RATING_NEGATIVE = 2
        const val RATING_NEUTRAL = 3

        const val GET_NUMBER_RATING = 1
    }

    /**
     * Return a Single<Boolean> which emits whether or not the given [address] should be blocked
     */
    override fun shouldBlock(address: String): Single<Boolean> {
        return Binder(context, prefs, address).shouldBlock()
    }

    private class Binder(
        private val context: Context,
        private val prefs: Preferences,
        private val address: String
    ) : ServiceConnection {

        private val subject: SingleSubject<Boolean> = SingleSubject.create()
        private var serviceMessenger: Messenger? = null
        private var isBound: Boolean = false

        fun shouldBlock(): Single<Boolean> {

            var intent: Intent? = null

            // If either version of Should I Answer? is installed and SIA is enabled, build the
            // intent to request a rating
            if (prefs.sia.get()) {
                intent = tryOrNull(false) {
                    context.packageManager.getApplicationInfo("org.mistergroup.shouldianswer", 0).enabled
                    Intent("org.mistergroup.shouldianswer.PublicService").setPackage("org.mistergroup.shouldianswer")
                } ?: tryOrNull(false) {
                    context.packageManager.getApplicationInfo("org.mistergroup.shouldianswerpersonal", 0).enabled
                    Intent("org.mistergroup.shouldianswerpersonal.PublicService")
                            .setPackage("org.mistergroup.shouldianswerpersonal")
                } ?: tryOrNull(false) {
                    context.packageManager.getApplicationInfo("org.mistergroup.muzutozvednout", 0).enabled
                    Intent("org.mistergroup.muzutozvednout.PublicService").setPackage("org.mistergroup.muzutozvednout")
                }
            }

            // If the intent isn't null, bind the service and wait for a result. Otherwise, don't block
            if (intent != null) {
                context.bindService(intent, this, Context.BIND_AUTO_CREATE)
            } else {
                subject.onSuccess(false)
            }

            return subject
        }

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
            isBound = true

            val message = Message().apply {
                what = GET_NUMBER_RATING
                data = bundleOf("number" to address)
                replyTo = Messenger(IncomingHandler { response ->
                    val shouldBlock = response.rating == RATING_NEGATIVE || response.wantBlock
                    subject.onSuccess(shouldBlock)
                    Timber.v("Should block: $shouldBlock")

                    // We're done, so unbind the service
                    if (isBound && serviceMessenger != null) {
                        context.unbindService(this@Binder)
                    }
                })
            }

            serviceMessenger?.send(message)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            serviceMessenger = null
            isBound = false
        }
    }

    private class IncomingHandler(private val callback: (response: Response) -> Unit) : Handler() {
        class Response(bundle: Bundle) {
            val rating: Int = bundle.getInt("rating")
            val wantBlock = bundle.getInt("wantBlock") == 1
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_NUMBER_RATING -> callback(Response(msg.data))
                else -> super.handleMessage(msg)
            }
        }
    }

}
