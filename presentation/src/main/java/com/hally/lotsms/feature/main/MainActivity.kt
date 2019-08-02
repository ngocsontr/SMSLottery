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
package com.hally.lotsms.feature.main

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.javiersantos.bottomdialogs.BottomDialog
import com.google.android.material.snackbar.Snackbar
import com.hally.lotsms.R
import com.hally.lotsms.common.Navigator
import com.hally.lotsms.common.androidxcompat.drawerOpen
import com.hally.lotsms.common.androidxcompat.scope
import com.hally.lotsms.common.base.QkThemedActivity
import com.hally.lotsms.common.network.ApiUtils
import com.hally.lotsms.common.network.model.XsmbRss
import com.hally.lotsms.common.util.LodeUtil
import com.hally.lotsms.common.util.extensions.*
import com.hally.lotsms.common.util.format
import com.hally.lotsms.feature.conversations.ConversationItemTouchCallback
import com.hally.lotsms.feature.conversations.ConversationsAdapter
import com.hally.lotsms.model.Conversation
import com.hally.lotsms.repository.SyncRepository
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import kotlinx.android.synthetic.main.lode_setting_view.view.tong_so_lode
import kotlinx.android.synthetic.main.lode_tongket_item_view.view.*
import kotlinx.android.synthetic.main.lode_tongket_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class MainActivity : QkThemedActivity(), MainView {

    @Inject
    lateinit var disposables: CompositeDisposable
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter
    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment
    @Inject
    lateinit var searchAdapter: SearchAdapter
    @Inject
    lateinit var itemTouchCallback: ConversationItemTouchCallback
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var lodeUtil: LodeUtil

    override val activityResumedIntent: Subject<Unit> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy { compose.clicks() }
    override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout
                .drawerOpen(Gravity.START)
                .doOnNext { dismissKeyboard() }
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val drawerItemIntent: Observable<DrawerItem> by lazy {
        Observable.merge(listOf(
                inbox.clicks().map { DrawerItem.INBOX },
                archived.clicks().map { DrawerItem.ARCHIVED },
                backup.clicks().map { DrawerItem.BACKUP },
                scheduled.clicks().map { DrawerItem.SCHEDULED },
                blocking.clicks().map { DrawerItem.BLOCKING },
                settings.clicks().map { DrawerItem.SETTINGS },
                plus.clicks().map { DrawerItem.PLUS },
                help.clicks().map { DrawerItem.HELP },
                invite.clicks().map { DrawerItem.INVITE }))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val plusBannerIntent by lazy { plusBanner.clicks() }
    override val dismissRatingIntent by lazy { rateDismiss.clicks() }
    override val rateIntent by lazy { rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java] }
    private val toggle by lazy { ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.main_drawer_open_cd, 0) }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }
    private val archiveSnackbar by lazy {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
        }
    }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val is1Day = prefs.oneDaySms.get()
        datePicker.setText(if (is1Day) R.string.datePicker1Day else R.string.datePickerAllDay)
        datePicker.setOnClickListener { v -> changeDatePicker(v as TextView) }
        kq_xsmb.setOnClickListener { showXsmbDialog() }
        tongket.setOnClickListener { showTongketDialog() }
        clear.setOnClickListener { showClearDialog() }
        getKqXsmb()

        viewModel.bindView(this)

        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks().subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
            syncingProgress?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
        }

        toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme
                .doOnNext { recyclerView.scrapViews() }
                .autoDisposable(scope())
                .subscribe { theme ->
                    // Set the color for the drawer icons
                    val states = arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf(-android.R.attr.state_activated))
                    resolveThemeColor(android.R.attr.textColorSecondary)
                            .let { textSecondary -> ColorStateList(states, intArrayOf(theme.theme, textSecondary)) }
                            .let { tintList ->
                                inboxIcon.imageTintList = tintList
                                archivedIcon.imageTintList = tintList
                            }

                    // Miscellaneous views
                    listOf(plusBadge1, plusBadge2).forEach { badge ->
                        badge.setBackgroundTint(theme.theme)
                        badge.setTextColor(theme.textPrimary)
                    }
                    syncingProgress?.progressTintList = ColorStateList.valueOf(theme.theme)
                    syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.theme)
                    plusIcon.setTint(theme.theme)
                    rateIcon.setTint(theme.theme)
                    compose.setBackgroundTint(theme.theme)

                    // Set the FAB compose icon color
                    compose.setTint(theme.textPrimary)
                }

        itemTouchCallback.adapter = conversationsAdapter
        conversationsAdapter.autoScrollToStart(recyclerView)
    }

    private fun showClearDialog() {
        BottomDialog.Builder(this).setTitle("Xóa dữ liệu")
                .setContent("Chắc chắn muốn xóa TOÀN BỘ dữ liệu Lô Đề?")
                .setPositiveText("Có")
                .onPositive {
                    lodeUtil.clearData(null)
                }
                .show()
    }

    var data: RealmResults<Conversation>? = null
    private fun showTongketDialog() {
        val view = layoutInflater.inflate(R.layout.lode_tongket_view, null)
        view.chuyen.setOnClickListener {
            view.chuyen_text.text = "TỔNG KẾT"
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("TỔNG KẾT", view.chuyen_text.text)
            clipboard.primaryClip = clip
            makeToast("Đã copy vào clipboard.\nChuyển!!")
        }
        view.update.setOnClickListener {
            val tongSo = view.tong_so_lode.text.toString()
            if (tongSo.isNotEmpty()) {
                prefs.tongSoLode.set(tongSo.toInt())
                makeToast("Cập nhật xong!!")
            } else makeToast("Không được bỏ trống!!")
        }
        view.tong_so_lode.setText("${prefs.tongSoLode.get()}")


        var tk = 0
        data?.forEach { conv ->
            val lodes = lodeUtil.lodeRepo.getLodes(conv.id)
            if (lodes.size > 0) {
                val viewItem = layoutInflater.inflate(R.layout.lode_tongket_item_view, null)
                viewItem.username.text = conv.getTitle()
                view.tongket_content.addView(viewItem)
                val giaLo = conv.giaLo
                val arr = lodeUtil.tongKet(lodes, giaLo)
                viewItem.lottery_result.text = arr[0]
                viewItem.tongket_text.text = arr[1]
                if (arr[2].toInt() > 0) viewItem.tongket_text.setTextColor(Color.RED)
                tk += arr[2].toInt().unaryMinus()
            }
        }

        BottomDialog.Builder(this).setTitle("TỔNG KẾT: ${tk.format()}k")
                .setContent(Calendar.getInstance().time.toString())
                .setCustomView(view)
                .show()
    }

    private fun showXsmbDialog() {
        if (lodeUtil.isToDay()) {
            BottomDialog.Builder(this).setTitle(getString(R.string.kq_title))
                    .setContent(prefs.kqRaw.get())
                    .show()
            return
        }

        // request server to update new KQ XSMB
        val builder = AlertDialog.Builder(this)
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        builder.setTitle(getString(R.string.kq_title))
                .setAdapter(arrayAdapter, null)
                .setPositiveButton("OK", null)
                .create()
                .show()
        ApiUtils.getXsmb(object : Callback<XsmbRss> {
            override fun onResponse(call: Call<XsmbRss>, response: Response<XsmbRss>) {
                val res = response.body()
                if (res?.items == null || res.items.isEmpty()) {
                    makeToast(response.errorBody().toString())
                    return
                }
                lodeUtil.saveXSMB(res.items[0])

                for (item in res.items) {
                    arrayAdapter.add(item.title + "\n" + item.description)
                    if (lodeUtil.isToDay2(item.link))
                        lodeUtil.saveXSMB(item)
                }
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<XsmbRss>, t: Throwable) {
                makeToast(getString(R.string.not_internet_message))
            }
        })
    }

    private fun getKqXsmb() {
        if (lodeUtil.isToDay() || !lodeUtil.isLodeTime()) return

        ApiUtils.getXsmb(object : Callback<XsmbRss> {
            override fun onResponse(call: Call<XsmbRss>, response: Response<XsmbRss>) {
                val res = response.body()
                if (res?.items == null || res.items.isEmpty())
                    makeToast(response.errorBody().toString())
                else {
                    lodeUtil.saveXSMB(res.items[0])

                    for (item in res.items) {
                        if (lodeUtil.isToDay2(item.link))
                            lodeUtil.saveXSMB(item)
                    }
                }
            }

            override fun onFailure(call: Call<XsmbRss>, t: Throwable) {
                makeToast(getString(R.string.not_internet_message))
            }
        })
    }

    private fun changeDatePicker(textView: TextView) {
        val is1Day = prefs.oneDaySms.get()
        textView.setText(if (is1Day) R.string.datePickerAllDay else R.string.datePicker1Day)
        prefs.oneDaySms.set(!is1Day)
        viewModel.bindView(this)
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)

        toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0

        listOf(plusBadge1, plusBadge2).forEach { badge ->
            badge.isVisible = drawerBadgesExperiment.variant && !state.upgraded
        }
        plus.isVisible = state.upgraded
        plusBanner.isVisible = !state.upgraded
        rateLayout.setVisible(state.showRating)

        compose.setVisible(state.page is Inbox || state.page is Archived)
        conversationsAdapter.emptyView = empty.takeIf { state.page is Inbox || state.page is Archived }

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                data = filterData(state.page.data)
                conversationsAdapter.updateData(data)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (recyclerView.adapter !== searchAdapter) recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter = conversationsAdapter
                data = filterData(state.page.data)
                conversationsAdapter.updateData(data)
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.archived_empty_text)
            }
        }

        inbox.isActivated = state.page is Inbox
        archived.isActivated = state.page is Archived

        if (drawerLayout.isDrawerOpen(GravityCompat.START) && !state.drawerOpen) drawerLayout.closeDrawer(GravityCompat.START)
        else if (!drawerLayout.isDrawerVisible(GravityCompat.START) && state.drawerOpen) drawerLayout.openDrawer(GravityCompat.START)

        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                syncing.isVisible = false
                snackbar.isVisible = !state.defaultSms || !state.smsPermission || !state.contactPermission
            }

            is SyncRepository.SyncProgress.Running -> {
                syncing.isVisible = true
                syncingProgress.max = state.syncing.max
                progressAnimator.apply { setIntValues(syncingProgress.progress, state.syncing.progress) }.start()
                syncingProgress.isIndeterminate = state.syncing.indeterminate
                snackbar.isVisible = false
            }
        }

        when {
            !state.defaultSms -> {
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.main_default_sms_message)
                snackbarButton?.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_sms)
                snackbarButton?.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_contacts)
                snackbarButton?.setText(R.string.main_permission_allow)
            }
        }
    }

    private fun filterData(data: RealmResults<Conversation>?): RealmResults<Conversation>? {
        if (!prefs.oneDaySms.get()) return data
        val time = lodeUtil.getLodeTime()
        return data?.where()?.between("date", time[0], time[1])?.findAll()
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = when (show) {
            true -> resolveThemeColor(android.R.attr.textColorSecondary)
            false -> resolveThemeColor(android.R.attr.textColorPrimary)
        }
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS), 0)
    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
                .setPositiveButton(R.string.button_delete) { _, _ -> confirmDeleteIntent.onNext(conversations) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showArchivedSnackbar() {
        archiveSnackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        backPressedIntent.onNext(Unit)
    }

}
