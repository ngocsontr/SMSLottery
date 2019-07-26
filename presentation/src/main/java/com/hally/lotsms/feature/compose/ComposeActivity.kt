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
package com.hally.lotsms.feature.compose

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.javiersantos.bottomdialogs.BottomDialog
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hally.lotsms.R
import com.hally.lotsms.common.LodeDialog.Companion.E
import com.hally.lotsms.common.androidxcompat.scope
import com.hally.lotsms.common.base.QkThemedActivity
import com.hally.lotsms.common.network.ApiUtils
import com.hally.lotsms.common.network.model.XsmbRss
import com.hally.lotsms.common.util.DateFormatter
import com.hally.lotsms.common.util.LodeUtil
import com.hally.lotsms.common.util.extensions.*
import com.hally.lotsms.common.util.format
import com.hally.lotsms.model.*
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.lode_current_total.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ComposeActivity : QkThemedActivity(), ComposeView {

    companion object {
        private const val CAMERA_REQUEST_CODE = 0
        private const val GALLERY_REQUEST_CODE = 1
        private const val CONTACT_REQUEST_CODE = 2
    }

    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter
    @Inject
    lateinit var chipsAdapter: ChipsAdapter
    @Inject
    lateinit var contactsAdapter: ContactAdapter
    @Inject
    lateinit var dateFormatter: DateFormatter
    @Inject
    lateinit var messageAdapter: MessagesAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var lodeUtil: LodeUtil

    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent: Observable<CharSequence> by lazy { chipsAdapter.textChanges }
    override val queryBackspaceIntent: Observable<*> by lazy { chipsAdapter.backspaces }
    override val queryEditorActionIntent: Observable<Int> by lazy { chipsAdapter.actions }
    override val chipSelectedIntent: Subject<Contact> by lazy { contactsAdapter.contactSelected }
    override val chipDeletedIntent: Subject<Contact> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val sendAsGroupIntent by lazy { sendAsGroupBackground.clicks() }
    override val messageClickIntent: Subject<Message> by lazy { messageAdapter.clicks }
    override val messagesSelectedIntent by lazy { messageAdapter.selectionChanges }
    override val cancelSendingIntent: Subject<Message> by lazy { messageAdapter.cancelSending }
    override val attachmentDeletedIntent: Subject<Attachment> by lazy { attachmentAdapter.attachmentDeleted }
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { Observable.merge(attach.clicks(), attachingBackground.clicks()) }
    override val cameraIntent by lazy { Observable.merge(camera.clicks(), cameraLabel.clicks()) }
    override val galleryIntent by lazy { Observable.merge(gallery.clicks(), galleryLabel.clicks()) }
    override val scheduleIntent by lazy { Observable.merge(schedule.clicks(), scheduleLabel.clicks()) }
    override val attachContactIntent by lazy { Observable.merge(contact.clicks(), contactLabel.clicks()) }
    override val attachmentSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val contactSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val inputContentIntent by lazy { message.inputContentSelected }
    override val scheduleSelectedIntent: Subject<Long> = PublishSubject.create()
    override val changeSimIntent by lazy { sim.clicks() }
    override val scheduleCancelIntent by lazy { scheduledCancel.clicks() }
    override val sendIntent by lazy { send.clicks() }
    override val viewQksmsPlusIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ComposeViewModel::class.java] }

    private var cameraDestination: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        showBackButton(true)
        viewModel.bindView(this)

        contentView.layoutTransition = LayoutTransition().apply {
            disableTransitionType(LayoutTransition.CHANGING)
        }

        chipsAdapter.view = chips

        contacts.itemAnimator = null
        chips.itemAnimator = null
        chips.layoutManager = FlexboxLayoutManager(this)

        messageAdapter.activity = this
        messageAdapter.autoScrollToStart(messageList)
        messageAdapter.emptyView = messagesEmpty

        messageList.setHasFixedSize(true)
        messageList.adapter = messageAdapter

        attachments.adapter = attachmentAdapter

        message.supportsInputContent = true

        theme
                .doOnNext { loading.setTint(it.theme) }
                .doOnNext { attach.setBackgroundTint(it.theme) }
                .doOnNext { attach.setTint(it.textPrimary) }
                .doOnNext { messageAdapter.theme = it }
                .autoDisposable(scope())
                .subscribe { messageList.scrapViews() }

        window.callback = ComposeWindowCallback(window.callback, this)

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            messageBackground.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
            composeBackground.setBackgroundTint(resolveThemeColor(R.attr.composeBackground))
        }
    }

    override fun onStart() {
        super.onStart()
        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    override fun render(state: ComposeState) {
        if (state.hasError) {
            finish()
            return
        }

        threadId.onNext(state.selectedConversation)

        title = when {
            state.selectedMessages > 0 -> getString(R.string.compose_title_selected, state.selectedMessages)
            state.query.isNotEmpty() -> state.query
            else -> state.conversationtitle
        }

        toolbarSubtitle.setVisible(state.query.isNotEmpty())
        toolbarSubtitle.text = getString(R.string.compose_subtitle_results, state.searchSelectionPosition, state.searchResults)

        toolbarTitle.setVisible(!state.editingMode)
        chips.setVisible(state.editingMode)
        contacts.setVisible(state.contactsVisible)
        composeBar.setVisible(!state.contactsVisible && !state.loading)

        // Don't set the adapters unless needed
        if (state.editingMode && chips.adapter == null) chips.adapter = chipsAdapter
        if (state.editingMode && contacts.adapter == null) contacts.adapter = contactsAdapter

        toolbar.menu.findItem(R.id.call)?.isVisible = !state.editingMode && state.selectedMessages == 0 && state.query.isEmpty()
        toolbar.menu.findItem(R.id.info)?.isVisible = !state.editingMode && state.selectedMessages == 0 && state.query.isEmpty()
        toolbar.menu.findItem(R.id.copy)?.isVisible = !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.details)?.isVisible = !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.delete)?.isVisible = !state.editingMode && state.selectedMessages > 0
        toolbar.menu.findItem(R.id.forward)?.isVisible = !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.previous)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()
        toolbar.menu.findItem(R.id.next)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()
        toolbar.menu.findItem(R.id.clear)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()

        if (chipsAdapter.data.isEmpty() && state.selectedContacts.isNotEmpty()) {
            message.showKeyboard()
        }

        chipsAdapter.data = state.selectedContacts
        contactsAdapter.data = state.contacts

        loading.setVisible(state.loading)

        sendAsGroup.setVisible(state.editingMode && state.selectedContacts.size >= 2)
        sendAsGroupSwitch.isChecked = state.sendAsGroup

        messageList.setVisible(state.sendAsGroup)
        messageAdapter.data = filterData(state.messages)
        messageAdapter.highlight = state.searchSelectionId

        scheduledGroup.isVisible = state.scheduled != 0L
        scheduledTime.text = dateFormatter.getScheduledTimestamp(state.scheduled)

        attachments.setVisible(state.attachments.isNotEmpty())
        attachmentAdapter.data = state.attachments

        attach.animate().rotation(if (state.attaching) 45f else 0f).start()
        attaching.isVisible = state.attaching

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        sim.setVisible(state.subscription != null)
        sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        simIndex.text = "${state.subscription?.simSlotIndex?.plus(1)}"

        send.isEnabled = state.canSend
        send.imageAlpha = if (state.canSend) 255 else 128

        lotteryList.setOnClickListener { showDetalDialog(state.lodes?.second) }
        kq_xsmb.setOnClickListener { showKqDialog() }
        lode_settings.setOnClickListener { showSettingDialog() }
        loadLodeTongKet(state.lodes?.second)
    }

    private fun loadLodeTongKet(second: RealmResults<Lode>?) {
        second?.let {
            val giaLo = 23
            var chi = 0
            var thu = 0

            val builder = StringBuilder()
            for (e in E.values()) {
                val txt = lodeUtil.getText(e, it)
                if (!txt.isNullOrBlank()) {
                    builder.append("${e.vni} : ")
                    builder.append(txt)
                    builder.append(if (e == E.LO || e == E.XIEN) " Điểm " else " k ")
                    builder.append("\n")

                    val arr = txt.split("/")
                    chi += arr[0].toInt() * e.price
                    thu += arr[1].toInt() * if (e == E.LO || e == E.XIEN) giaLo else 1
                }
            }
            builder.append("Chi/Thu: <$chi / $thu>")
            lottery_lo.text = builder//.delete(builder.length - 2, builder.length)
            val tk = thu - chi
            val emo = if (tk >= 0) "\uD83E\uDD29" else "\uD83D\uDE30"
            if (tk < 0) tongket.setTextColor(Color.RED)
            tongket.text = "$emo ${tk.format()}k"
        }
    }

    private fun showDetalDialog(second: RealmResults<Lode>?) {
        second?.let {
            BottomDialog.Builder(this).setTitle("Tổng kết")
                    .setContent(lodeUtil.getLodeAllArray(it).toDisplay())
                    .show()
        }
    }

    private fun showSettingDialog() {
        BottomDialog.Builder(this).setTitle("Settings")
                .setCustomView(layoutInflater.inflate(R.layout.notification_action, null))
                .show()
    }

    private fun showKqDialog() {
        if (lodeUtil.isSameDay(prefs.lastDayXSMB.get())) {
            BottomDialog.Builder(this).setTitle("Kết quả XSMB")
                    .setContent(prefs.kqRaw.get())
                    .show()

        } else ApiUtils.getXsmb(object : Callback<XsmbRss> {
            override fun onResponse(call: Call<XsmbRss>, response: Response<XsmbRss>) {
                val res = response.body()
                if (res?.items == null || res.items.isEmpty()) return

                if (lodeUtil.isSameDay(res.items[0].pubDate))
                    lodeUtil.saveXSMB(res.items[0])

                BottomDialog.Builder(this@ComposeActivity).setTitle("Kết quả XSMB")
                        .setContent(prefs.kqRaw.get())
                        .show()
            }

            override fun onFailure(call: Call<XsmbRss>, t: Throwable) {
                makeToast(t.toString())
            }
        })
    }

    private fun filterData(data: Pair<Conversation, RealmResults<Message>>?): Pair<Conversation, RealmResults<Message>>? {
        if (!prefs.oneDaySms.get()) return data

        val then = Calendar.getInstance()
        then.add(Calendar.DAY_OF_YEAR, -1)
        val con = data?.first
        val mes = data?.second?.where()?.greaterThan("date", then.timeInMillis)?.findAll()
        return if (con == null || mes == null) null else Pair(con, mes)
    }

    override fun clearSelection() = messageAdapter.clearSelection()

    override fun showDetails(details: String) {
        AlertDialog.Builder(this)
                .setTitle(R.string.compose_details_title)
                .setMessage(details)
                .setCancelable(true)
                .show()
    }

    override fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
    }

    override fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS), 0)
    }

    override fun requestDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                scheduleSelectedIntent.onNext(calendar.timeInMillis)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this)).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun requestContact() {
        val intent = Intent(Intent.ACTION_PICK)
                .setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

        startActivityForResult(Intent.createChooser(intent, null), CONTACT_REQUEST_CODE)
    }

    override fun requestCamera() {
        cameraDestination = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                .let { timestamp -> ContentValues().apply { put(MediaStore.Images.Media.TITLE, timestamp) } }
                .let { cv -> contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, cameraDestination)
        startActivityForResult(Intent.createChooser(intent, null), CAMERA_REQUEST_CODE)
    }

    override fun requestGallery() {
        val intent = Intent(Intent.ACTION_PICK)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, false)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setType("image/*")
        startActivityForResult(Intent.createChooser(intent, null), GALLERY_REQUEST_CODE)
    }

    override fun setDraft(draft: String) = message.setText(draft)

    override fun scrollToMessage(id: Long) {
        messageAdapter.data?.second
                ?.indexOfLast { message -> message.id == id }
                ?.takeIf { position -> position != -1 }
                ?.let(messageList::scrollToPosition)
    }

    override fun showQksmsPlusSnackbar(message: Int) {
        Snackbar.make(contentView, message, Snackbar.LENGTH_LONG).run {
            setAction(R.string.button_more) { viewQksmsPlusIntent.onNext(Unit) }
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun getColoredMenuItems(): List<Int> {
        return super.getColoredMenuItems() + R.id.call
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> cameraDestination?.let(attachmentSelectedIntent::onNext)
                GALLERY_REQUEST_CODE -> data?.data?.let(attachmentSelectedIntent::onNext)
                CONTACT_REQUEST_CODE -> data?.data?.let(contactSelectedIntent::onNext)
            }
        }
    }

    override fun onBackPressed() = backPressedIntent.onNext(Unit)

}