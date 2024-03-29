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
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.javiersantos.bottomdialogs.BottomDialog
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hally.lotsms.R
import com.hally.lotsms.common.LodeDialog
import com.hally.lotsms.common.androidxcompat.scope
import com.hally.lotsms.common.base.QkThemedActivity
import com.hally.lotsms.common.util.DateFormatter
import com.hally.lotsms.common.util.LodeUtil
import com.hally.lotsms.common.util.extensions.*
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
import kotlinx.android.synthetic.main.lode_setting_view.view.*
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

        initLode()
    }

    override fun onStart() {
        super.onStart()
        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    @SuppressLint("SetTextI18n")
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
        messageAdapter.autoScrollToStart(messageList)
        messageList.adapter = messageAdapter

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

        kq_xsmb.setOnClickListener { showKqDialog() }
        state.messages?.first?.let {
            giaLo = it.giaLo
            giaDe = it.giaDe
            lode_settings.text = "${(it.giaLo.toFloat().div(10))}k "
            lode_settings.setOnClickListener {
                showSettingDialog(state.selectedConversation, giaLo, giaDe)
            }
        }
        clear.setOnClickListener { showClearDialog(state.selectedConversation) }
        computeLodeResult(state.selectedConversation)
    }

    var giaLo = 230
    var giaDe = 100

    private fun computeLodeResult(conv_id: Long) {
        val lodes = lodeUtil.lodeRepo.getLodes(conv_id)
        val arr = lodeUtil.tongKet(lodes, giaLo, giaDe)
        lottery_result.text = arr[0]
        tongket_text.text = arr[1]
        if (arr[2].toInt() > 0) tongket_text.setTextColor(Color.RED)
        tongket_text.setOnClickListener { message.setText(arr[1]) }
        lotteryList.setOnClickListener { showDetalDialog(lodes) }
    }

    private fun initLode() {
        datePicker.text = prefs.pickDate.get()
        datePicker.setOnClickListener { v -> changeDatePicker(v as TextView) }
        all.setOnClickListener { allDate() }
    }

    private fun allDate() {
        if (prefs.oneDaySms.get()) {
            prefs.oneDaySms.set(false)
            changeToday()
            lodeUtil.getKqXsmb()
            reloadView()
        }
    }

    private fun changeToday() {
        val date = LodeUtil.sdf.format(Calendar.getInstance().time)
        datePicker.text = date
        prefs.pickDate.set(date)
    }

    @SuppressLint("RestrictedApi")
    private fun changeDatePicker(textView: TextView) {
        val calendar = lodeUtil.getNow()
        val dialog = DatePickerDialog(this, 0,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val date = LodeUtil.sdf.format(calendar.time)
                    prefs.pickDate.set(date)
                    prefs.oneDaySms.set(true)
                    lottery_result.text = ""
                    tongket_text.text = ""
                    textView.text = prefs.pickDate.get()

                    lodeUtil.getKqXsmb()
                    reloadView()

                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.spinnersShown = true
        dialog.show()
    }


    private fun showDetalDialog(second: RealmResults<Lode>?) {
        second?.let {
            val content = lodeUtil.getLodeAllArray(it).toDisplay()
            BottomDialog.Builder(this).setTitle("Tổng kết")
                    .setContent(content)
                    .setPositiveText("Copy")
                    .onPositive {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("TỔNG KẾT", content)
                        clipboard.primaryClip = clip
//                        message.setText(content)
                        makeToast("Đã copy vào clipboard!!")
                    }
                    .show()
        }
    }

    private fun showSettingDialog(id: Long, giaLo: Int, giaDe: Int) {
        val view = layoutInflater.inflate(R.layout.lode_setting_view, null)
        view.gia_lo.setText("$giaLo")
        view.gia_de.setText("$giaDe")
        BottomDialog.Builder(this).setTitle("Settings")
                .setCustomView(view)
                .setPositiveText("Cập Nhật")
                .onPositive {
                    lodeUtil.update(id, view.gia_lo.text.toString(), view.gia_de.text.toString())
                    reloadView()
                }
                .show()
    }

    private fun showClearDialog(id: Long) {
        BottomDialog.Builder(this).setTitle("Xóa dữ liệu")
                .setContent("Chắc chắn muốn xóa hết dữ liệu Lô Đề?")
                .setPositiveText("Có")
                .onPositive {
                    lodeUtil.clearData(id)
                    reloadView()
                }
                .show()
    }

    private fun showKqDialog() {
        BottomDialog.Builder(this).setTitle(getString(R.string.kq_title))
                .setContent(prefs.kqRaw.get())
                .show()
    }

    private fun filterData(data: Pair<Conversation, RealmResults<Message>>?): Pair<Conversation, RealmResults<Message>>? {
        if (!prefs.oneDaySms.get()) return data

        val con = data?.first
        val time = lodeUtil.getLodeTime()
        val mes = data?.second?.where()?.between("date", time[0], time[1])?.findAll()
        return if (con == null || mes == null) null else Pair(con, mes)
    }

    private fun reloadView() {
        Handler().postDelayed({ viewModel.bindView(this@ComposeActivity) }, 400)
    }

    fun isLodeFormat(mes: Message): Boolean {
        if (!mes.isSms()) {
            makeToast("Chỉ dùng với tin nhắn SMS!")
            return false
        }
        val messNoSign = LodeUtil.removeVietnamese(mes.body)
        for (type in LodeDialog.TYPE) {
            if (messNoSign.contains(type))
                return true
        }
        makeToast("SMS không thấy LÔ ĐỀ!")
        return true
    }

    fun showDialogLode(message: Message) {
        val data = Bundle()
        data.putString(LodeDialog.MESSAGE, message.body)
        val lodeDialog = LodeDialog()
        lodeDialog.arguments = data
        lodeDialog.setCallback(object : LodeDialog.Callback {
            override fun onPositiveButtonClicked(lode: Lode) {
                makeToast("Xử lý OK!")
                lodeUtil.xuly(message, lode)
                reloadView()
            }
        })
        lodeDialog.isCancelable = false
        lodeDialog.show(supportFragmentManager, LodeDialog.TAG)
    }

    fun showDialogDelLode(message: Message) {
        BottomDialog.Builder(this).setTitle("Chú ý")
                .setContent("Chắc chắn hủy lô đề cho tin nhắn này!!")
                .setPositiveText("OK")
                .setNegativeText("Không")
                .onPositive {
                    lodeUtil.huy(message.id)
                    reloadView()
                }
                .show()
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