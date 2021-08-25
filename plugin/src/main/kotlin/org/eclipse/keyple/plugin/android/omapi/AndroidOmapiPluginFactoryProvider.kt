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

import android.app.Activity

/**
 * The following example shows how to create a [AndroidOmapiPluginFactoryFactory] object with the
 * [AndroidOmapiPluginFactoryProvider] and use it to register a Plugin.
 *
 *```
 * AndroidOmapiPluginFactoryProvider(this) {
 *  val plugin = SmartCardServiceProvider.getService().registerPlugin(it)
 * }
 *```
 *
 * @property activity Any Android activity.
 * @property callback Triggered when the reader is ready
 * @constructor Builds instances of [AndroidOmapiPluginFactory] from context provided in constructor.
 * @since 2.0.0
 */
class AndroidOmapiPluginFactoryProvider(private val activity: Activity, callback: (AndroidOmapiPluginFactory) -> Unit) : AndroidOmapiPluginFactory {

    private var factoryAdapter: AndroidOmapiPluginFactoryAdapter = AndroidOmapiPluginFactoryAdapter(activity, callback)

    /**
     * Returns an instance of [AndroidOmapiPluginFactory].
     *
     * @return A [AndroidOmapiPluginFactory]
     * @since 2.0.0
     */
    fun getFactory(): AndroidOmapiPluginFactory {
        return factoryAdapter
    }
}
