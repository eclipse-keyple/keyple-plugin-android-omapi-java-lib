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

import android.content.Context
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiPluginAdapter
import org.simalliance.openmobileapi.SEService
import org.slf4j.LoggerFactory

/**
 * Implementation of AbstractAndroidOmapiPlugin using the SIMALLIANCE OMAPI implementation of Reader
 * and SeService objects.
 *
 * @since 2.0.0
 */
internal object AndroidOmapiPluginAdapter :
    AbstractAndroidOmapiPluginAdapter<org.simalliance.openmobileapi.Reader, SEService>() {

  private val logger = LoggerFactory.getLogger(AndroidOmapiPluginAdapter::class.java)

  override fun connectToSe(context: Context, callback: () -> Unit) {
    val seServiceFactory = SeServiceFactoryAdapter(context)
    seService =
        seServiceFactory.connectToSe(
            SEService.CallBack {
              logger.info(
                  "Plugin [{}]: connected, ready to register plugin, OMAPI SEService version: {}",
                  name,
                  seService?.version)
              callback()
            })
  }

  override fun getNativeReaders(): Array<org.simalliance.openmobileapi.Reader>? {
    return seService?.readers
  }

  override fun mapToReader(nativeReader: org.simalliance.openmobileapi.Reader): ReaderSpi {
    logger.info(
        "Plugin [{}]: reader available: [{}], is card present: {}",
        name,
        nativeReader.name,
        nativeReader.isSecureElementPresent)
    return AndroidOmapiReaderAdapter(
        nativeReader, mapNativeReaderNameToKeypleReaderName(nativeReader.name))
  }
}
