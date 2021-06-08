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

import android.view.MenuItem
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_core_examples.drawerLayout
import kotlinx.android.synthetic.main.activity_core_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_core_examples.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.keyple.card.generic.GenericCardExtensionProvider
import org.eclipse.keyple.core.service.CardSelectionServiceFactory
import org.eclipse.keyple.core.service.KeypleCardCommunicationException
import org.eclipse.keyple.core.service.KeypleReaderCommunicationException
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.ReaderEvent
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.core.service.selection.CardSelectionService
import org.eclipse.keyple.core.service.selection.CardSelector
import org.eclipse.keyple.core.service.selection.MultiSelectionProcessing
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.example.R
import org.eclipse.keyple.plugin.android.omapi.example.util.CalypsoClassicInfo
import timber.log.Timber

class CoreExamplesActivity : AbstractExampleActivity() {

    override fun onResume() {
        super.onResume()
        (reader as ObservableReader).startCardDetection(ObservableReader.PollingMode.REPEATING)
    }

    override fun initContentView() {
        setContentView(R.layout.activity_core_examples)
        initActionBar(toolbar, "NFC Plugins", "Core Examples")
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
        }
        return true
    }

    private fun configureUseCase4SequentialMultiSelection() {
        addHeaderEvent("UseCase Generic #4: AID based sequential explicit multiple selection")

        with(reader as ObservableReader) {
            addHeaderEvent("Reader  NAME = $name")
            if (isCardPresent) {
                /*
              * operate card AID selection (change the AID prefix here to adapt it to the card used for
              * the test [the card should have at least two applications matching the AID prefix])
              */
                val cardAidPrefix = CalypsoClassicInfo.AID_PREFIX

                /* First selection case */
                cardSelectionsService = CardSelectionServiceFactory.getService(MultiSelectionProcessing.FIRST_MATCH)

                val cardSelectorFirst = CardSelector
                    .builder()
                    .filterByDfName(cardAidPrefix)
                    .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                    .setFileOccurrence(CardSelector.FileOccurrence.FIRST)
                    .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                    .build()

                cardSelectionsService.prepareSelection(GenericCardExtensionProvider.getService().createGenericCardSelection(cardSelectorFirst))

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

                cardSelectionsService.prepareSelection(GenericCardExtensionProvider.getService().createGenericCardSelection(cardSelectorNext))

                /* Do the selection and display the result */
                addActionEvent("NEXT MATCH Calypso PO selection for prefix: $cardAidPrefix")
                doAndAnalyseSelection(this, cardSelectionsService, 2)
            } else {
                addResultEvent("No cards were detected.")
            }
            eventRecyclerView.smoothScrollToPosition(events.size - 1)
        }
    }

    private fun doAndAnalyseSelection(reader: Reader?, cardSelectionsService: CardSelectionService, index: Int) {
        try {
            val cardSelectionsResult = cardSelectionsService.processCardSelectionScenario(reader)
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

        useCase = null

        with(reader as ObservableReader) {
            addHeaderEvent("Reader  NAME = $name")
            if (isCardPresent) {
                cardSelectionsService = CardSelectionServiceFactory.getService(MultiSelectionProcessing.PROCESS_ALL)

                /* Close the channel after the selection to force the selection of all applications */
                cardSelectionsService.prepareReleaseChannel()

                /* operate card selection (change the AID here to adapt it to the card used for the test) */
                val cardAidPrefix = CalypsoClassicInfo.AID_PREFIX

                val cardSelectorFirst = CardSelector
                    .builder()
                    .filterByDfName(cardAidPrefix)
                    .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                    .setFileOccurrence(CardSelector.FileOccurrence.FIRST)
                    .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                    .build()

                cardSelectionsService.prepareSelection(GenericCardExtensionProvider.getService().createGenericCardSelection(cardSelectorFirst))

                val cardSelector2nd = CardSelector
                    .builder()
                    .filterByDfName(cardAidPrefix)
                    .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                    .setFileOccurrence(CardSelector.FileOccurrence.NEXT)
                    .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                    .build()

                cardSelectionsService.prepareSelection(GenericCardExtensionProvider.getService().createGenericCardSelection(cardSelector2nd))

                val cardSelector3rd = CardSelector
                    .builder()
                    .filterByDfName(cardAidPrefix)
                    .filterByCardProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
                    .setFileOccurrence(CardSelector.FileOccurrence.NEXT)
                    .setFileControlInformation(CardSelector.FileControlInformation.FCI)
                    .build()

                cardSelectionsService.prepareSelection(GenericCardExtensionProvider.getService().createGenericCardSelection(cardSelector3rd))

                addActionEvent("Calypso PO selection for prefix: $cardAidPrefix")

                /*
                * Actual card communication: operate through a single request the card selection
                */
                try {
                    val selectionResult = cardSelectionsService.processCardSelectionScenario(this)

                    if (selectionResult.smartCards.isNotEmpty()) {
                        selectionResult.smartCards.forEach {
                            addResultEvent(getSmardCardInfos(it.value, it.key))
                        }
                        addResultEvent("End of selection")
                    } else {
                        addResultEvent("No cards matched the selection.")
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
            cardSelectionsService.prepareSelection(GenericCardExtensionProvider.getService().createGenericCardSelection(cardSelector))

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
                                (reader as ObservableReader).finalizeCardProcessing()
                            }

                            ReaderEvent.EventType.CARD_INSERTED -> {
                                addResultEvent("CARD_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.")
                                (reader as ObservableReader).finalizeCardProcessing()
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

                val smartCardService = SmartCardServiceProvider.getService()

                // Get the generic card extension service
                val cardExtension = GenericCardExtensionProvider.getService()

                // Verify that the extension's API level is consistent with the current service.
                smartCardService.checkCardExtension(cardExtension)

                /*
                 * Prepare the card selection
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

                /**
                 * Create a card selection using the generic card extension.
                 */
                val cardSelection = cardExtension.createGenericCardSelection(cardSelector)

                /**
                 * Prepare Selection
                 */
                cardSelectionsService.prepareSelection(cardSelection)
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
    }

    override fun onReaderEvent(readerEvent: ReaderEvent?) {
        Timber.i("New ReaderEvent received : $readerEvent")
        useCase?.onEventUpdate(readerEvent)
    }
}
