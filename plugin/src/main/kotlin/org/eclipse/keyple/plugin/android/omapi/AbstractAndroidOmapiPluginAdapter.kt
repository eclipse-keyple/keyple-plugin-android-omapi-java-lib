/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://calypsonet.org/
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

import android.content.Context
import org.eclipse.keyple.core.plugin.spi.PluginSpi
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi
import timber.log.Timber

/**
 * The AndroidOmapiPlugin interface provides the public elements used to manage the Android OMAPI plugin.
 *
 * @since 2.0.0
 */
internal abstract class AbstractAndroidOmapiPluginAdapter<T, V> : AndroidOmapiPlugin, PluginSpi {

    abstract fun connectToSe(context: Context, callback: () -> Unit)
    abstract fun getNativeReaders(): Array<T>?
    abstract fun mapToReader(nativeReader: T): ReaderSpi

    protected var seService: V? = null
    private val params = mutableMapOf<String, String>()

    /**
     * Initialize plugin by connecting to [SEService]
     *
     * @since 0.9
     */
    fun init(context: Context, callback: () -> Unit) {
        return if (seService != null) {
            callback()
        } else {
            Timber.d("Connect to a card")
            connectToSe(context.applicationContext, callback)
        }
    }

    override fun searchAvailableReaders(): MutableSet<ReaderSpi> {
        Timber.d("searchAvailableReaders")
        val readerSpis = HashSet<ReaderSpi>()
        getNativeReaders()?.forEach { nativeReader ->
            val reader = mapToReader(nativeReader)
            readerSpis.add(reader)
        }
        return readerSpis
    }

    override fun getName(): String {
        return AndroidOmapiPlugin.PLUGIN_NAME
    }

    override fun onUnregister() {
        // DO nothing
    }

    /**
     * Map Native reader name (SIM, SIM1,SIM2,ESE) to Keyple reader name const
     * Return nativeReaderName if no mapping found
     */
    protected fun mapNativeReaderNameToKeypleReaderName(nativeReaderName: String): String {
        return if (nativeReaderName.equals("SIM", true) || nativeReaderName.equals("SIM1", true)) {
            AndroidOmapiReader.READER_NAME_SIM_1
        } else if (nativeReaderName.equals("SIM2", true)) {
            AndroidOmapiReader.READER_NAME_SIM_2
        } else if (nativeReaderName.equals("ESE", true)) {
            AndroidOmapiReader.READER_NAME_ESE
        } else {
            nativeReaderName
        }
    }
}
