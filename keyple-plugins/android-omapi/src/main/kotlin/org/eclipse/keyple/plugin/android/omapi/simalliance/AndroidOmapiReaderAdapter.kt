/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
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

internal class AndroidOmapiReaderAdapter(private val nativeReader: Reader, readerName: String) : AbstractAndroidOmapiReader(readerName) {

    companion object {
        private const val P2_SUPPORTED_MIN_VERSION = 3
    }

    private var session: Session? = null
    private var openChannel: Channel? = null
    private val omapiVersion = nativeReader.seService.version.toFloat()

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun openChannelForAid(aid: ByteArray?, isoControlMask: Byte): ByteArray {
        if (aid == null) {
            try {
                openChannel = session?.openBasicChannel(null)
            } catch (e: IOException) {
                Timber.e(e)
                throw ReaderIOException("IOException while opening basic channel.")
            } catch (e: ReaderIOException) {
                Timber.e(e)
                throw ReaderIOException("Error while opening basic channel, DFNAME = " + ByteArrayUtil.toHex(aid), e.cause)
            }

            if (openChannel == null) {
                throw ReaderIOException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                this.getName(), ByteArrayUtil.toHex(aid))
            try {
                // openLogicalChannel of SimAlliance OMAPI is only available for version 3.0+ of the library.
                // By default the library always passes p2=00h
                // So if a p2 different of 00h is requested, we must check if omapi support it. Otherwise we throw an exception.
                val p2 = isoControlMask
                openChannel =
                    if (0 == p2.toInt()) {
                        session?.openLogicalChannel(aid)
                    } else {
                        if (omapiVersion >= P2_SUPPORTED_MIN_VERSION) {
                            session?.openLogicalChannel(aid, p2)
                        } else {
                            throw ReaderIOException("P2 != 00h while opening logical channel is only supported by OMAPI version >= 3.0. Current is $omapiVersion")
                        }
                    }
            } catch (e: IOException) {
                Timber.e(e, "IOException")
                throw ReaderIOException("IOException while opening logical channel.", e)
            } catch (e: NoSuchElementException) {
                Timber.e(e, "NoSuchElementException")
                throw java.lang.IllegalArgumentException(
                    "NoSuchElementException: " + ByteArrayUtil.toHex(aid), e)
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw ReaderIOException("SecurityException while opening logical channel, aid :" + ByteArrayUtil.toHex(aid), e.cause)
            }

            if (openChannel == null) {
                throw ReaderIOException("Failed to open a logical channel.")
            }
        }
        /* get the FCI and build an ApduResponse */
        return openChannel!!.selectResponse
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun closeLogicalChannel() {
        session?.closeChannels()
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun getPowerOnData(): String {
        val atr = session?.atr
        return if (atr != null) {
            val sAtr = ByteArrayUtil.toHex(atr)
            Timber.i("Retrieving ATR from session: $sAtr")
            sAtr
        } else ""
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
    override fun onUnregister() {
        // NOTHING TO DO
    }
}
