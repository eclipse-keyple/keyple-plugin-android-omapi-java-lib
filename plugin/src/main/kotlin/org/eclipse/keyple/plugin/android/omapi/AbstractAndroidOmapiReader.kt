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
package org.eclipse.keyple.plugin.android.omapi

import java.io.IOException
import org.eclipse.keyple.core.plugin.ReaderIOException
import org.eclipse.keyple.core.plugin.spi.reader.AutonomousSelectionReaderSpi
import org.slf4j.LoggerFactory

/**
 * Common part of the OMAPI readers, independent of the OMAPI implementation (Android or
 * SIMalliance), which manages the native session and the native channel used for the APDU
 * exchanges.
 *
 * @param S The type of the native session.
 * @param C The type of the native channel.
 */
internal abstract class AbstractAndroidOmapiReader<S : Any, C : Any>(
    private val readerName: String
) : AndroidOmapiReader, AutonomousSelectionReaderSpi {

  private val logger = LoggerFactory.getLogger(AbstractAndroidOmapiReader::class.java)

  /** The native session, open while the physical channel is open. */
  protected var session: S? = null

  /** The native channel, opened by the selection or, without AID, on the first transmission. */
  protected var openChannel: C? = null

  /** Opens the basic channel of the native [session] without selecting any application. */
  @Throws(IOException::class) protected abstract fun openNativeBasicChannel(session: S): C?

  /** Transmits [apduIn] through the native [channel] and returns the response. */
  @Throws(IOException::class)
  protected abstract fun transmitNative(channel: C, apduIn: ByteArray): ByteArray

  /** Closes all the channels of the native [session]. */
  protected abstract fun closeNativeChannels(session: S)

  /** Closes the native [session] and its channels. */
  protected abstract fun closeNativeSession(session: S)

  override fun getName(): String {
    return readerName
  }

  override fun isContactless(): Boolean {
    return false
  }

  /**
   * Opens the basic channel without selecting any application.
   *
   * @return The open channel.
   * @throws ReaderIOException If the basic channel cannot be opened.
   */
  @Throws(ReaderIOException::class)
  protected fun openBasicChannel(): C {
    if (logger.isDebugEnabled) {
      logger.debug("[readerExt={}] Opening card basic channel", name)
    }
    val channel =
        try {
          session?.let { openNativeBasicChannel(it) }
        } catch (e: IOException) {
          throw ReaderIOException("Failed to open the basic channel", e)
        } catch (e: SecurityException) {
          throw ReaderIOException("Failed to open the basic channel", e)
        } ?: throw ReaderIOException("Failed to open the basic channel")
    openChannel = channel
    return channel
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun closeLogicalChannel() {
    session?.let { closeNativeChannels(it) }
    openChannel = null
  }

  /**
   * {@inheritDoc}
   *
   * @since 2.0.0
   */
  override fun closePhysicalChannel() {
    session?.let { closeNativeSession(it) }
    session = null
    openChannel = null
  }

  /**
   * {@inheritDoc}
   *
   * A card selected without AID (e.g. a SAM selected by its power-on data only) has no channel open
   * yet: the basic channel is then opened on first use.
   *
   * @since 2.0.0
   */
  @Throws(ReaderIOException::class)
  override fun transmitApdu(apduIn: ByteArray): ByteArray {
    val channel = openChannel ?: openBasicChannel()
    try {
      return transmitNative(channel, apduIn)
    } catch (e: IOException) {
      throw ReaderIOException("Failed to transmit APDU", e)
    }
  }
}
