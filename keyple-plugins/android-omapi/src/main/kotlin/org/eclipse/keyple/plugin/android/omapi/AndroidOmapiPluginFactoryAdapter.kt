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

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.eclipse.keyple.core.common.CommonsApiProperties
import org.eclipse.keyple.core.plugin.PluginApiProperties
import org.eclipse.keyple.core.plugin.PluginIOException
import org.eclipse.keyple.core.plugin.spi.PluginFactorySpi
import org.eclipse.keyple.core.plugin.spi.PluginSpi

internal class AndroidOmapiPluginFactoryAdapter(private val context: Context, callback: (AndroidOmapiPluginFactory) -> Unit) : AndroidOmapiPluginFactory, PluginFactorySpi {

    private var sdkVersion: Int = Build.VERSION.SDK_INT
    private var readerPlugin: AbstractAndroidOmapiPluginAdapter<*, *>

    companion object {
        const val SIMALLIANCE_OMAPI_PACKAGE_NAME = "org.simalliance.openmobileapi.service"
    }

    init {
        readerPlugin = getPluginRegardingOsVersion()
        readerPlugin.init(context) { callback(this) }
    }

    private fun getPluginRegardingOsVersion(): AbstractAndroidOmapiPluginAdapter<*, *> {
        return if (sdkVersion >= Build.VERSION_CODES.P)
            org.eclipse.keyple.plugin.android.omapi.se.AndroidOmapiPluginAdapter
        else
            getPluginRegardingPackages()
    }

    @Throws(PluginIOException::class)
    private fun getPluginRegardingPackages(): AbstractAndroidOmapiPluginAdapter<*, *> {
        return try {
            context.packageManager
                .getPackageInfo(SIMALLIANCE_OMAPI_PACKAGE_NAME, 0)
            org.eclipse.keyple.plugin.android.omapi.simalliance.AndroidOmapiPluginAdapter
        } catch (e2: PackageManager.NameNotFoundException) {
            throw PluginIOException("No OMAPI lib available within the OS")
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun getPluginApiVersion(): String {
        return PluginApiProperties.VERSION
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun getCommonsApiVersion(): String {
        return CommonsApiProperties.VERSION
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun getPluginName(): String {
        return AndroidOmapiPlugin.PLUGIN_NAME
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    override fun getPlugin(): PluginSpi {
        return readerPlugin
    }
}
