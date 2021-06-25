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
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.simalliance.openmobileapi.SEService
import timber.log.Timber

internal object AndroidOmapiPluginAdapter : AbstractAndroidOmapiPluginAdapter<org.simalliance.openmobileapi.Reader, SEService>() {

    override fun connectToSe(context: Context, callback: () -> Unit) {
        val seServiceFactory = SeServiceFactoryImpl(context)
        seService = seServiceFactory.connectToSe(SEService.CallBack {
            Timber.i("Connected, ready to register plugin")
            Timber.i("OMAPI SEService version: %s", seService?.version)
            callback()
        })
    }

    override fun getNativeReaders(): Array<org.simalliance.openmobileapi.Reader>? {
        return seService?.readers
    }

    override fun mapToReader(nativeReader: org.simalliance.openmobileapi.Reader): ReaderSpi {
        Timber.d("Reader available name : %s", nativeReader.name)
        Timber.d("Reader available isCardPresent : %S", nativeReader.isSecureElementPresent)
        return AndroidOmapiReaderAdapter(nativeReader, AndroidOmapiPlugin.PLUGIN_NAME, nativeReader.name)
    }
}
