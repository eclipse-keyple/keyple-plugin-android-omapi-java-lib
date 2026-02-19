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
import org.eclipse.keyple.core.util.HexUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
import org.simalliance.openmobileapi.Channel
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.Session
import org.slf4j.LoggerFactory

internal class AndroidOmapiReaderAdapter(private val nativeReader: Reader, readerName: String) :
    AbstractAndroidOmapiReader(readerName) {

  private val logger = LoggerFactory.getLogger(AndroidOmapiReaderAdapter::class.java)

  companion object {
    private const val P2_SUPPORTED_MIN_VERSION = 3
  }

  private var session: Session? = null
  private var openChannel: Channel? = null
  private val omapiVersion = nativeReader.seService.version.toFloat()

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun openChannelForAid(aid: ByteArray?, isoControlMask: Byte): ByteArray {
    if (aid == null) {
      try {
        openChannel = session?.openBasicChannel(null)
      } catch (e: IOException) {
        throw ReaderIOException("Failed to open the basic channel", e)
      } catch (e: ReaderIOException) {
        throw ReaderIOException(
            "Failed to communicate with card while opening the basic channel. DF Name: " +
                HexUtil.toHex(aid),
            e.cause)
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
        // openLogicalChannel of SimAlliance OMAPI is only available for version 3.0+ of the
        // library.
        // By default the library always passes p2=00h
        // So if a p2 different of 00h is requested, we must check if omapi support it. Otherwise we
        // throw an exception.
        val p2 = isoControlMask
        openChannel =
            if (0 == p2.toInt()) {
              session?.openLogicalChannel(aid)
            } else {
              if (omapiVersion >= P2_SUPPORTED_MIN_VERSION) {
                session?.openLogicalChannel(aid, p2)
              } else {
                throw ReaderIOException(
                    "P2 != 00h while opening logical channel is only supported by OMAPI version >= 3.0. Current is $omapiVersion")
              }
            }
      } catch (e: IOException) {
        throw ReaderIOException("Failed to open the logical channel", e)
      } catch (e: NoSuchElementException) {
        throw java.lang.IllegalArgumentException("AID not found: " + HexUtil.toHex(aid), e)
      } catch (e: SecurityException) {
        throw ReaderIOException(
            "Failed to open the logical channel. AID:" + HexUtil.toHex(aid), e.cause)
      }

      if (openChannel == null) {
        throw ReaderIOException("Failed to open the logical channel")
      }
    }
    /* get the FCI and build an ApduResponse */
    return openChannel!!.selectResponse
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
  public override fun openPhysicalChannel() {
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
