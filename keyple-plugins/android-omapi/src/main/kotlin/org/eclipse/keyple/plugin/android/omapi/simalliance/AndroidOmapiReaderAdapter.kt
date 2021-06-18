/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.android.omapi.simalliance

import java.io.IOException
import org.eclipse.keyple.core.plugin.ReaderIOException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
import org.simalliance.openmobileapi.Channel
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.Session
import timber.log.Timber

internal class AndroidOmapiReaderAdapter(private val nativeReader: Reader, pluginName: String, readerName: String) : AbstractAndroidOmapiReader(pluginName, readerName) {

    companion object {
        private const val P2_SUPPORTED_MIN_VERSION = 3
    }

    private var session: Session? = null
    private var openChannel: Channel? = null
    private val omapiVersion = nativeReader.seService.version.toFloat()

    // TODO Comment gérer ce comportement avec Keyple V2???
    /**
     * Open a logical channel by selecting the application
     *
     * @param dfName A byte array containing the DF name or null if a basic opening is wanted.
     * @param isoControlMask The selection bits defined by the ISO selection command and expected by the OMAPI as P2 parameter.
     * @return A byte array containing the response to the OMAPI openLogicalChannel process or null if the Secure Element is unable to
     *         provide a new logical channel
     * @throws KeypleReaderIOException if the communication with the reader or the card has failed
     *
     * @since 0.9
     */
//    @Throws(KeypleReaderIOException::class)
//    override fun openChannelForAid(dfName: ByteArray?, isoControlMask: Byte): ByteArray? {
//        if (dfName == null) {
//            try {
//                openChannel = session?.openBasicChannel(null)
//            } catch (e: IOException) {
//                Timber.e(e)
//                throw KeypleReaderIOException("IOException while opening basic channel.")
//            } catch (e: SecurityException) {
//                Timber.e(e)
//                throw KeypleReaderIOException("Error while opening basic channel, DFNAME = " + ByteArrayUtil.toHex(dfName), e.cause)
//            }
//
//            if (openChannel == null) {
//                throw KeypleReaderIOException("Failed to open a basic channel.")
//            }
//        } else {
//            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
//                this.name, ByteArrayUtil.toHex(dfName))
//            try {
//                // openLogicalChannel of SimAlliance OMAPI is only available for version 3.0+ of the library.
//                // By default the library always passes p2=00h
//                // So if a p2 different of 00h is requested, we must check if omapi support it. Otherwise we throw an exception.
//                val p2 = isoControlMask
//                openChannel =
//                    if (0 == p2.toInt()) {
//                        session?.openLogicalChannel(dfName)
//                    } else {
//                        if (omapiVersion >= P2_SUPPORTED_MIN_VERSION) {
//                            session?.openLogicalChannel(dfName, p2)
//                        } else {
//                            throw KeypleReaderIOException("P2 != 00h while opening logical channel is only supported by OMAPI version >= 3.0. Current is $omapiVersion")
//                        }
//                    }
//            } catch (e: IOException) {
//                Timber.e(e, "IOException")
//                throw KeypleReaderIOException("IOException while opening logical channel.", e)
//            } catch (e: NoSuchElementException) {
//                Timber.e(e, "NoSuchElementException")
//                throw java.lang.IllegalArgumentException(
//                    "NoSuchElementException: " + ByteArrayUtil.toHex(dfName), e)
//            } catch (e: SecurityException) {
//                Timber.e(e, "SecurityException")
//                throw KeypleReaderIOException("SecurityException while opening logical channel, aid :" + ByteArrayUtil.toHex(dfName), e.cause)
//            }
//
//            if (openChannel == null) {
//                throw KeypleReaderIOException("Failed to open a logical channel.")
//            }
//        }
//        /* get the FCI and build an ApduResponse */
//        return openChannel!!.selectResponse
//    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun getPowerOnData(): String {
        val atr = session?.atr
        return if(atr != null){
            val sAtr =  ByteArrayUtil.toHex(atr)
            Timber.i("Retrieving ATR from session: $sAtr")
            sAtr
        }else ""
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Throws(ReaderIOException::class)
    public override fun openPhysicalChannel() {
        try {
            session = nativeReader.openSession()
        } catch (e: ReaderIOException) {
            Timber.e(e, "IOException")
            throw ReaderIOException("IOException while opening physical channel.", e)
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun closePhysicalChannel() {
        openChannel?.let {
            it.session.close()
            openChannel = null
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun isPhysicalChannelOpen(): Boolean {
        return session?.isClosed == false
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun checkCardPresence(): Boolean {
        return nativeReader.isSecureElementPresent
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Throws(ReaderIOException::class)
    override fun transmitApdu(apduIn: ByteArray): ByteArray {
        // Initialization
        Timber.d("Data Length to be sent to tag : %s", apduIn.size)
        Timber.d("Data in : %s", ByteArrayUtil.toHex(apduIn))
        var dataOut = byteArrayOf(0)
        try {
            openChannel.let {
                dataOut = it?.transmit(apduIn) ?: throw IOException("Channel is not open")
            }
        } catch (e: IOException) {
            throw ReaderIOException("Error while transmitting APDU", e)
        }

        Timber.d("Data out : %s", ByteArrayUtil.toHex(dataOut))
        return dataOut
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun unregister() {
        // NOTHING TO DO
    }
}
