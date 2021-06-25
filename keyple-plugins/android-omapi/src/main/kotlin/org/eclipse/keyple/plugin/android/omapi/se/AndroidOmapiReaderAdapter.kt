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
package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.Session
import androidx.annotation.RequiresApi
import java.io.IOException
import org.eclipse.keyple.core.plugin.ReaderIOException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
import timber.log.Timber

@RequiresApi(android.os.Build.VERSION_CODES.P)
internal class AndroidOmapiReaderAdapter(private val nativeReader: Reader, pluginName: String, readerName: String) : AbstractAndroidOmapiReader(pluginName, readerName) {

    private var session: Session? = null
    private var openChannel: Channel? = null

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun openChannelForAid(aid: ByteArray?, isoControlMask: Byte): ByteArray? {
        if (aid == null) { try {
            openChannel = session?.openBasicChannel(null)
        } catch (e: IOException) {
            Timber.e(e, "IOException")
            throw ReaderIOException("IOException while opening basic channel.")
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException")
            throw ReaderIOException("Error while opening basic channel, DFNAME = " + ByteArrayUtil.toHex(aid), e.cause)
        }

            if (openChannel == null) {
                throw ReaderIOException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                this.name, ByteArrayUtil.toHex(aid))
            try {
                openChannel =
                    session?.openLogicalChannel(aid, isoControlMask)
            } catch (e: IOException) {
                Timber.e(e, "IOException")
                throw ReaderIOException("IOException while opening logical channel.")
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
        return openChannel?.selectResponse
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
    @Throws(ReaderIOException::class)
    override fun openPhysicalChannel() {
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
