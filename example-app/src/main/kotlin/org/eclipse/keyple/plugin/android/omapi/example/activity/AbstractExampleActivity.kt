/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.android.omapi.example.activity

import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import kotlinx.android.synthetic.main.activity_calypso_examples.drawerLayout
import kotlinx.android.synthetic.main.activity_calypso_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_calypso_examples.navigationView
import kotlinx.android.synthetic.main.activity_calypso_examples.toolbar
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.ReaderEvent
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.core.service.selection.CardSelectionService
import org.eclipse.keyple.core.service.selection.spi.SmartCard
import org.eclipse.keyple.core.service.spi.ReaderObserverSpi
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactoryAdapter
import org.eclipse.keyple.plugin.android.omapi.example.R
import org.eclipse.keyple.plugin.android.omapi.example.adapter.EventAdapter
import org.eclipse.keyple.plugin.android.omapi.example.model.ChoiceEventModel
import org.eclipse.keyple.plugin.android.omapi.example.model.EventModel
import timber.log.Timber

abstract class AbstractExampleActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ReaderObserverSpi {

    /**
     * Use to modify event update behaviour regarding current use case execution
     */
    interface UseCase {
        fun onEventUpdate(event: ReaderEvent?)
    }

    /**
     * Variables for event window
     */
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var layoutManager: RecyclerView.LayoutManager
    protected val events = arrayListOf<EventModel>()

    protected lateinit var reader: Reader

    protected var useCase: UseCase? = null
    protected lateinit var cardSelectionsService: CardSelectionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()

        /**
         * Init recycler view
         */
        adapter = EventAdapter(events)
        layoutManager = LinearLayoutManager(this)
        eventRecyclerView.layoutManager = layoutManager
        eventRecyclerView.adapter = adapter

        /**
         * Init menu
         */
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /**
         * Register AndroidNfc plugin Factory
         */
        val plugin = SmartCardServiceProvider.getService().registerPlugin(AndroidOmapiPluginFactoryAdapter(this))

        /**
         * Configure Nfc Reader
         */
        with(plugin.readers.values.first() as AndroidNfcReader) {
            presenceCheckDelay = 100
            noPlateformSound = false
            skipNdefCheck = false
            // with this protocol settings we activate the nfc for ISO1443_4 protocol
            activateProtocol(ContactlessCardCommonProtocol.ISO_14443_4.name)
            with(this as ObservableReader) {
                addObserver(this@AbstractExampleActivity)
                setReaderObservationExceptionHandler { pluginName, readerName, e ->
                    Timber.e("An unexpected reader error occurred: $pluginName:$readerName : $e")
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    protected fun initActionBar(toolbar: Toolbar, title: String, subtitle: String) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = title
        actionBar?.subtitle = subtitle
    }

    protected fun showAlertDialog(t: Throwable) {
        val builder = AlertDialog.Builder(this@AbstractExampleActivity)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()
    }

    protected fun initFromBackgroundTextView() {
        addResultEvent("Smartcard detected while in background...")
    }

    protected fun clearEvents() {
        events.clear()
        adapter.notifyDataSetChanged()
    }

    protected fun addHeaderEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_HEADER, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Header: %s", message)
    }

    protected fun addActionEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_ACTION, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Action: %s", message)
    }

    protected fun addResultEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_RESULT, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Result: %s", message)
    }

    protected fun addChoiceEvent(title: String, choices: List<String>, callback: (choice: String) -> Unit) {
        events.add(ChoiceEventModel(title, choices, callback))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Choice: %s: %s", title, choices.toString())
    }

    @Throws(IOException::class)
    protected fun checkNfcAvailability() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            throw IOException("Your device does not support NFC")
        } else {
            if (!nfcAdapter.isEnabled) {
                throw IOException("Please enable NFC to communicate with NFC Elements\"")
            }
        }
    }

    abstract fun initContentView()

    override fun onDestroy() {
        SmartCardServiceProvider.getService().plugins.forEach {
            SmartCardServiceProvider.getService().unregisterPlugin(it.key)
        }
        super.onDestroy()
    }

    protected fun getSmardCardInfos(smartCard: SmartCard, index: Int): String {
        val atr = try {
            ByteArrayUtil.toHex(smartCard.atrBytes)
        } catch (e: IllegalStateException) {
            Timber.w(e)
            e.message
        }
        val fci = try {
            ByteArrayUtil.toHex(smartCard.fciBytes)
        } catch (e: IllegalStateException) {
            Timber.w(e)
            e.message
        }

        return "Selection status for selection " +
                "(indexed $index): \n\t\t" +
                "ATR: ${atr}\n\t\t" +
                "FCI: $fci"
    }
}
