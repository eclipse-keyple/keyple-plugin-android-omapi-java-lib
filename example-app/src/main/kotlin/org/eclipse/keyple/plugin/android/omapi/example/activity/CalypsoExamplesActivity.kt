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
import android.view.MenuItem
import androidx.core.view.GravityCompat
import java.io.IOException
import kotlinx.android.synthetic.main.activity_calypso_examples.drawerLayout
import kotlinx.android.synthetic.main.activity_calypso_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_calypso_examples.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.keyple.card.calypso.CalypsoCardExtensionProvider
import org.eclipse.keyple.card.calypso.po.PoSmartCard
import org.eclipse.keyple.core.common.KeypleCardSelectionResponse
import org.eclipse.keyple.core.service.CardSelectionServiceFactory
import org.eclipse.keyple.core.service.KeypleCardCommunicationException
import org.eclipse.keyple.core.service.KeypleReaderCommunicationException
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.ReaderEvent
import org.eclipse.keyple.core.service.selection.CardSelectionService
import org.eclipse.keyple.core.service.selection.CardSelector
import org.eclipse.keyple.core.service.selection.MultiSelectionProcessing
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.example.R
import org.eclipse.keyple.plugin.android.omapi.example.util.CalypsoClassicInfo
import timber.log.Timber

/**
 * Example of @[SmartCardService] implementation based on the @[AndroidNfcPlugin]
 *
 * By default the plugin only listens to events when your application activity is in the foreground.
 * To activate NFC events while you application is not in the foreground, add the following
 * statements to your activity definition in AndroidManifest.xml
 *
 * <intent-filter> <action android:name="android.nfc.action.TECH_DISCOVERED" /> </intent-filter>
 * <meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/tech_list" />
 *
 * Create a xml/tech_list.xml file in your res folder with the following content <?xml version="1.0"
 * encoding="utf-8"?> <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2"> <tech-list>
 * <tech>android.nfc.tech.IsoDep</tech> <tech>android.nfc.tech.NfcA</tech> </tech-list> </resources>
 */
class CalypsoExamplesActivity : AbstractExampleActivity() {

    private var readEnvironmentParserIndex: Int = 0

    override fun onResume() {
        super.onResume()
        try {
            checkNfcAvailability()
            if (intent.action != null && intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) run {
                configureUseCase0()

                Timber.d("Handle ACTION TECH intent")
                // notify reader that card detection has been launched
                (reader as ObservableReader).startCardDetection(ObservableReader.PollingMode.SINGLESHOT)
                initFromBackgroundTextView()
                (reader as AndroidNfcReader).processIntent(intent)
            } else {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                // enable detection
                (reader as ObservableReader).startCardDetection(ObservableReader.PollingMode.SINGLESHOT)
            }
        } catch (e: IOException) {
            showAlertDialog(e)
        }
    }

    override fun onPause() {
        Timber.i("on Pause Fragment - Stopping Read Write Mode")
        // notify reader that card detection has been switched off
        (reader as ObservableReader).stopCardDetection()

        super.onPause()
    }

    override fun onDestroy() {
        (reader as ObservableReader).removeObserver(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        when (item.itemId) {
            R.id.usecase1 -> {
                clearEvents()
                configureUseCase1ExplicitSelectionAid()
            }
            R.id.usecase2 -> {
                clearEvents()
                configureUseCase2DefaultSelectionNotification()
            }
            R.id.usecase3 -> {
                clearEvents()
                configureUseCase3GroupedMultiSelection()
            }
            R.id.usecase4 -> {
                clearEvents()
                configureUseCase4SequentialMultiSelection()
            }
            R.id.start_scan -> {
                clearEvents()
                configureUseCase0()
            }
        }
        return true
    }

    override fun initContentView() {
        setContentView(R.layout.activity_calypso_examples)
        initActionBar(toolbar, "NFC Plugins", "Calypso Examples")
    }

    private fun configureUseCase4SequentialMultiSelection() {
        addHeaderEvent("UseCase Generic #4: AID based sequential explicit multiple selection")
        addHeaderEvent("Reader  NAME = ${reader.name}")

        with(reader as ObservableReader) {
            if (isCardPresent) {

                /*
                  * operate card AID selection (change the AID prefix here to adapt it to the card used for
                  * the test [the card should have at least two applications matching the AID prefix])
                  */
                val cardAidPrefix = CalypsoClassicInfo.AID_PREFIX

                /* First selection case */
                cardSelectionsService = CardSelectionServiceFactory.getService(MultiSelectionProcessing.FIRST_MATCH)

                /* AID based selection (1st selection, later indexed 0) */
                val selectionRequest1st = CardSelector
                        .builder()
                        .filterByDfName(cardAidPrefix)
                        .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                        .setFileOccurrence(CardSelector.FileOccurrence.FIRST)
                        .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                        .build()

                /* AID based selection (1st selection, later indexed 0) */
                cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                        .createPoCardSelection(selectionRequest1st, false))

                /* Do the selection and display the result */
                addActionEvent("FIRST MATCH Calypso PO selection for prefix: $cardAidPrefix")
                doAndAnalyseSelection(this, cardSelectionsService, 1)

                /*
                  * New selection: get the next application occurrence matching the same AID, close the
                  * physical channel after
                  */
                cardSelectionsService = CardSelectionServiceFactory.getService(MultiSelectionProcessing.FIRST_MATCH)

                /* Close the channel after the selection */
                cardSelectionsService.prepareReleaseChannel()

                /* next selection (2nd selection, later indexed 1) */
                val cardSelectorNext = CardSelector
                        .builder()
                        .filterByDfName(cardAidPrefix)
                        .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                        .setFileOccurrence(CardSelector.FileOccurrence.NEXT)
                        .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                        .build()

                cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                        .createPoCardSelection(cardSelectorNext, false))

                /* Do the selection and display the result */
                addActionEvent("NEXT MATCH Calypso PO selection for prefix: $cardAidPrefix")
                doAndAnalyseSelection(this, cardSelectionsService, 2)
            } else {
                addResultEvent("No cards were detected.")
            }
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun doAndAnalyseSelection(reader: Reader, cardSelectionService: CardSelectionService, index: Int) {
        try {
            val cardSelectionsResult = cardSelectionService.processCardSelectionScenario(reader)
            if (cardSelectionsResult.hasActiveSelection()) {
                val smartCard = cardSelectionsResult.activeSmartCard
                addResultEvent(getSmardCardInfos(smartCard, index))
            } else {
                addResultEvent("The selection did not match for case $index.")
            }
        } catch (e: KeypleCardCommunicationException) {
            addResultEvent("Error: ${e.message}")
        } catch (e: KeypleReaderCommunicationException) {
            addResultEvent("Error: ${e.message}")
        }
    }

    private fun configureUseCase3GroupedMultiSelection() {
        addHeaderEvent("UseCase Generic #3: AID based grouped explicit multiple selection")

        with(reader as ObservableReader) {
            addHeaderEvent("Reader  NAME = $name")
            if (isCardPresent) {
                cardSelectionsService = CardSelectionServiceFactory.getService(MultiSelectionProcessing.PROCESS_ALL)

                /* operate card selection (change the AID here to adapt it to the card used for the test) */
                val cardAidPrefix = CalypsoClassicInfo.AID_PREFIX

                /* Close the channel after the selection to force the selection of all applications */
                cardSelectionsService.prepareReleaseChannel()

                val selectionRequest1st = CardSelector
                        .builder()
                        .filterByDfName(cardAidPrefix)
                        .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                        .setFileOccurrence(CardSelector.FileOccurrence.FIRST)
                        .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                        .build()

                cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                        .createPoCardSelection(selectionRequest1st, false))

                val cardSelector2nd = CardSelector
                        .builder()
                        .filterByDfName(cardAidPrefix)
                        .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                        .setFileOccurrence(CardSelector.FileOccurrence.NEXT)
                        .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                        .build()

                cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                        .createPoCardSelection(cardSelector2nd, false))

                val cardSelector3rd = CardSelector
                        .builder()
                        .filterByDfName(cardAidPrefix)
                        .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                        .setFileOccurrence(CardSelector.FileOccurrence.NEXT)
                        .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                        .build()

                cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                        .createPoCardSelection(cardSelector3rd, false))

                /**
                 * We won't be listening for event update within this use case
                 */
                useCase = null

                addActionEvent("Calypso PO selection for prefix: $cardAidPrefix")

                /*
                * Actual card communication: operate through a single request the card selection
                */
                try {
                    val selectionResult = cardSelectionsService.processCardSelectionScenario(this)

                    if (selectionResult.smartCards.isNotEmpty()) {
                        try {
                            selectionResult.smartCards.forEach {
                                addResultEvent(getSmardCardInfos(it.value, it.key))
                            }
                        } catch (e: IllegalStateException) {
                            showAlertDialog(e)
                        }
                        addResultEvent("End of selection")
                    } else {
                        addResultEvent("No card matched the selection.")
                        addResultEvent("The card must be in the field when starting this use case")
                    }
                } catch (e: KeypleCardCommunicationException) {
                    addResultEvent("Error: ${e.message}")
                } catch (e: KeypleReaderCommunicationException) {
                    addResultEvent("Error: ${e.message}")
                }
            } else {
                addResultEvent("No cards were detected.")
            }
        }

        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun configureUseCase2DefaultSelectionNotification() {
        addHeaderEvent("UseCase Generic #2: AID based default selection")

        with(reader as ObservableReader) {
            addHeaderEvent("Reader  NAME = $name")

            /*
            * Prepare a card selection
            */
            cardSelectionsService = CardSelectionServiceFactory.getService()

            /*
            * Setting of an AID based selection
            *
            * Select the first application matching the selection AID whatever the card communication
            * protocol keep the logical channel open after the selection
            */
            val aid = CalypsoClassicInfo.AID

            /*
             * Generic selection: configures a CardSelector with all the desired attributes to make the
             * selection
             */
            val cardSelector = CardSelector
                    .builder()
                    .filterByDfName(aid)
                    .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                    .build()

            /*
            * Add the selection case to the current selection (we could have added other cases here)
            */
            cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                    .createPoCardSelection(cardSelector, false))
            cardSelectionsService.scheduleCardSelectionScenario(reader as ObservableReader, ObservableReader.NotificationMode.MATCHED_ONLY)

            useCase = object : UseCase {
                override fun onEventUpdate(event: ReaderEvent?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        when (event?.eventType) {
                            ReaderEvent.EventType.CARD_MATCHED -> {
                                addResultEvent("CARD_MATCHED event: A card corresponding to request has been detected")
                                val selectedCard = cardSelectionsService.processCardSelectionResponses(event.cardSelectionResponses).activeSmartCard
                                if (selectedCard != null) {
                                    addResultEvent("Observer notification: the selection of the card has succeeded. End of the card processing.")
                                    addResultEvent("Application FCI = ${ByteArrayUtil.toHex(selectedCard.fciBytes)}")
                                } else {
                                    addResultEvent("The selection of the card has failed. Should not have occurred due to the MATCHED_ONLY selection mode.")
                                }
                            }

                            ReaderEvent.EventType.CARD_INSERTED -> {
                                addResultEvent("CARD_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.")
                            }

                            ReaderEvent.EventType.CARD_REMOVED -> {
                                addResultEvent("CARD_REMOVED event: There is no PO inserted anymore. Return to the waiting state...")
                            }

                            else -> {
                            }
                        }
                        eventRecyclerView.smoothScrollToPosition(events.size - 1)
                    }
                    eventRecyclerView.smoothScrollToPosition(events.size - 1)
                }
            }
            addActionEvent("Waiting for a card... The default AID based selection to be processed as soon as the card is detected.")
        }
    }

    private fun configureUseCase1ExplicitSelectionAid() {
        addHeaderEvent("UseCase Generic #1: Explicit AID selection")

        with(reader as ObservableReader) {
            addHeaderEvent("Reader  NAME = $name")

            if (isCardPresent) {
                /*
                 * Prepare the card selection
                 */
                cardSelectionsService = CardSelectionServiceFactory.getService()

                /* Close the channel after the selection */
                cardSelectionsService.prepareReleaseChannel()

                /*
                 * Setting of an AID based selection (in this example a Calypso REV3 PO)
                 *
                 * Select the first application matching the selection AID whatever the card communication
                 * protocol keep the logical channel open after the selection
                 */
                val aid = CalypsoClassicInfo.AID

                /*
                 * Generic selection: configures a CardSelector with all the desired attributes to make
                 * the selection and read additional information afterwards
                 */
                val cardSelector = CardSelector
                        .builder()
                        .filterByDfName(aid)
                        .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                        .build()

                /**
                 * Prepare Selection
                 */
                cardSelectionsService.prepareSelection(CalypsoCardExtensionProvider.getService()
                        .createPoCardSelection(cardSelector, false))

                /**
                 * Provide the Reader with the selection operation to be processed when a card is inserted.
                 */
                cardSelectionsService.scheduleCardSelectionScenario(reader as ObservableReader, ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.SINGLESHOT)

                /**
                 * We won't be listening for event update within this use case
                 */
                useCase = null

                addActionEvent("Calypso PO selection: $aid")
                try {
                    val cardSelectionsResult = cardSelectionsService.processCardSelectionScenario(this)

                    if (cardSelectionsResult.hasActiveSelection()) {
                        val matchedCard = cardSelectionsResult.activeSmartCard
                        addResultEvent("The selection of the card has succeeded.")
                        addResultEvent("Application FCI = ${ByteArrayUtil.toHex(matchedCard.fciBytes)}")
                        addResultEvent("End of the generic card processing.")
                    } else {
                        addResultEvent("The selection of the card has failed.")
                    }
                    (reader as ObservableReader).finalizeCardProcessing()
                } catch (e: KeypleCardCommunicationException) {
                    addResultEvent("Error: ${e.message}")
                } catch (e: KeypleReaderCommunicationException) {
                    addResultEvent("Error: ${e.message}")
                }
            } else {
                addResultEvent("No cards were detected.")
                addResultEvent("The card must be in the field when starting this use case")
            }
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
        }
        eventRecyclerView.smoothScrollToPosition(events.size - 1)
    }

    private fun configureUseCase0() {
        addHeaderEvent("UseCase Generic #0: Explicit AID selection and reading")

        with(reader as ObservableReader) {
            addHeaderEvent("Reader  NAME = $name")
            // define task as an observer for ReaderEvents
            /*
             * Prepare a a new Calypso PO selection
             */
            cardSelectionsService = CardSelectionServiceFactory.getService()

            /*
              * Setting of an AID based selection (in this example a Calypso REV3 PO)
              *
              * Select the first application matching the selection AID whatever the card communication
              * protocol keep the logical channel open after the selection
              */
            val aid = CalypsoClassicInfo.AID

            /*
             * Generic selection: configures a CardSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            val cardSelector = CardSelector
                    .builder()
                    .filterByDfName(aid)
                    .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                    .build()

            val cardSelection = CalypsoCardExtensionProvider.getService().createPoCardSelection(cardSelector, false)

            cardSelection.prepareReadRecordFile(
                    CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                    CalypsoClassicInfo.RECORD_NUMBER_1.toInt())
            /**
             * Prepare Selection
             */
            cardSelectionsService.prepareSelection(cardSelection)

            /**
             * Provide the Reader with the selection operation to be processed when a card is inserted.
             */
            cardSelectionsService.scheduleCardSelectionScenario(reader as ObservableReader, ObservableReader.NotificationMode.ALWAYS)

            useCase = object : UseCase {
                override fun onEventUpdate(event: ReaderEvent?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        when (event?.eventType) {
                            ReaderEvent.EventType.CARD_MATCHED -> {
                                addResultEvent("Tag detected - card MATCHED")
                                executeCommands(event.cardSelectionResponses)
                                (this as ObservableReader).finalizeCardProcessing()
                            }

                            ReaderEvent.EventType.CARD_INSERTED -> {
                                addResultEvent("PO detected but AID didn't match with ${CalypsoClassicInfo.AID}")
                                (this as ObservableReader).finalizeCardProcessing()
                            }

                            ReaderEvent.EventType.CARD_REMOVED -> {
                                addResultEvent("Tag detected - card CARD_REMOVED")
                            }

                            ReaderEvent.EventType.UNREGISTERED -> {
                                addResultEvent("Unexpected error - reader is UNREGISTERED")
                            }
                        }
                    }
                    eventRecyclerView.smoothScrollToPosition(events.size - 1)
                }
            }
            // notify reader that card detection has been launched
            (reader as ObservableReader).startCardDetection(ObservableReader.PollingMode.REPEATING)
        }
    }

    /**
     * Run Calypso simple read transaction
     *
     * @param keypleCardSelectionsResponse
     */
    private fun executeCommands(keypleCardSelectionsResponse: List<KeypleCardSelectionResponse>) {

        // addHeaderEvent("Running Calypso Simple Read transaction")

        try {
            /*
             * print tag info in View
             */
            addHeaderEvent("Tag Id : ${(reader as AndroidNfcReader).printTagId()}")
            val cardSelectionsResult = cardSelectionsService.processCardSelectionResponses(keypleCardSelectionsResponse)
            addResultEvent("1st PO exchange: aid selection")

            if (cardSelectionsResult.hasActiveSelection()) {
                val calypsoPo = cardSelectionsResult.activeSmartCard as PoSmartCard

                addResultEvent("Calypso PO selection: ")
                addResultEvent("AID: ${ByteArrayUtil.fromHex(CalypsoClassicInfo.AID)}")

                /*
                 * Retrieve the data read from the parser updated during the selection process
                 */

                val environmentAndHolder = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder).data.content
                addResultEvent("Environment file data: ${ByteArrayUtil.toHex(environmentAndHolder)}")

                addResultEvent("2nd PO exchange: read the event log file")
                val calypsoCardExtensionProvider = CalypsoCardExtensionProvider.getService()
                val poTransaction = calypsoCardExtensionProvider.createPoUnsecuredTransaction(reader, calypsoPo)

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                poTransaction.prepareReadRecordFile(
                        CalypsoClassicInfo.SFI_EventLog,
                        CalypsoClassicInfo.RECORD_NUMBER_1.toInt())

                /*
                 * Actual PO communication: send the prepared read order, then close the channel
                 * with the PO
                 */
                addActionEvent("processPoCommands")
                poTransaction.prepareReleasePoChannel()
                poTransaction.processPoCommands()
                addResultEvent("SUCCESS")

                /*
                 * Retrieve the data read from the parser updated during the transaction process
                 */
                val eventLog = calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EventLog).data.content

                /* Log the result */
                addResultEvent("EventLog file: ${ByteArrayUtil.toHex(eventLog)}")
                addResultEvent("End of the Calypso PO processing.")
                addResultEvent("You can remove the card now")
            } else {
                addResultEvent("The selection of the PO has failed. Should not have occurred due to the MATCHED_ONLY selection mode.")
            }
        } catch (e: KeypleReaderCommunicationException) {
            Timber.e(e)
            addResultEvent("Exception: ${e.message}")
        } catch (e: Exception) {
            Timber.e(e)
            addResultEvent("Exception: ${e.message}")
        }
    }

    override fun onReaderEvent(readerEvent: ReaderEvent?) {
        Timber.i("New ReaderEvent received : $readerEvent")
        useCase?.onEventUpdate(readerEvent)
    }
}
