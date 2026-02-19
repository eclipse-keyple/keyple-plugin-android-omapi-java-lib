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
import org.eclipse.keyple.core.util.HexUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
import org.slf4j.LoggerFactory

@RequiresApi(android.os.Build.VERSION_CODES.P)
internal class AndroidOmapiReaderAdapter(private val nativeReader: Reader, readerName: String) :
    AbstractAndroidOmapiReader(readerName) {

  private val logger = LoggerFactory.getLogger(AndroidOmapiReaderAdapter::class.java)

  private var session: Session? = null
  private var openChannel: Channel? = null

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun openChannelForAid(aid: ByteArray?, isoControlMask: Byte): ByteArray? {
    if (aid == null) {
      try {
        openChannel = session?.openBasicChannel(null)
      } catch (e: IOException) {
        throw ReaderIOException("Failed to open the basic channel", e)
      } catch (e: SecurityException) {
        throw ReaderIOException(
            "Failed to open the basic channel. AID: " + HexUtil.toHex(aid), e.cause)
      }

      if (openChannel == null) {
        throw ReaderIOException("Failed to open the basic channel")
      }
    } else {
      if (logger.isDebugEnabled) {
        logger.debug(
            "[readerExt={}] Opening card physical channel [aid={}]", name, HexUtil.toHex(aid))
      }
      try {
        openChannel = session?.openLogicalChannel(aid, isoControlMask)
      } catch (e: IOException) {
        throw ReaderIOException("Failed to open the logical channel", e)
      } catch (e: NoSuchElementException) {
        throw java.lang.IllegalArgumentException("AID not found: " + HexUtil.toHex(aid), e)
      } catch (e: SecurityException) {
        throw ReaderIOException(
            "Failed to open the logical channel. AID: " + HexUtil.toHex(aid), e.cause)
      }

      if (openChannel == null) {
        throw ReaderIOException("Failed to open the logical channel")
      }
    }
    /* get the FCI and build an ApduResponse */
    return openChannel?.selectResponse
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun closeLogicalChannel() {
    session?.closeChannels()
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Throws(ReaderIOException::class)
  override fun openPhysicalChannel() {
    try {
      session = nativeReader.openSession()
    } catch (e: ReaderIOException) {
      throw ReaderIOException("Failed to open the physical channel", e)
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
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
   * @since 2.0.0
   */
  override fun isPhysicalChannelOpen(): Boolean {
    return session?.isClosed == false
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun checkCardPresence(): Boolean {
    return nativeReader.isSecureElementPresent
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun getPowerOnData(): String {
    val atr = session?.atr
    return if (atr != null) {
      val sAtr = HexUtil.toHex(atr)
      if (logger.isDebugEnabled) {
        logger.debug("[readerExt={}] Retrieving ATR [atr={}]", name, sAtr)
      }
      sAtr
    } else ""
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  @Throws(ReaderIOException::class)
  override fun transmitApdu(apduIn: ByteArray): ByteArray {
    var dataOut = byteArrayOf(0)
    try {
      openChannel.let { dataOut = it?.transmit(apduIn) ?: throw IOException("Channel is not open") }
    } catch (e: IOException) {
      throw ReaderIOException("Failed to transmit APDU", e)
    }
    return dataOut
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun onUnregister() {
    // NOTHING TO DO
  }
}
