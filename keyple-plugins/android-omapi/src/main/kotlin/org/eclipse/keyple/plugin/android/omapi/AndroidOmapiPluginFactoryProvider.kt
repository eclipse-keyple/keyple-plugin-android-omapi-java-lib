/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://www.calypsonet-asso.org/
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
 * Provides plugin's Factory. Regarding version of the device,
 * the factory will return adapters with readers built upon android.se or Simalliance omapi library
 *
 * This factory must be provided to SmartCardServiceProvider.
 *
 * <pre>SmartCardServiceProvider.getService().registerPlugin(AndroidOmapiPluginFactoryProvider(this, callback).getFactory())</pre>
 *
 * @since 2.0
 */
class AndroidOmapiPluginFactoryProvider(private val activity: Activity, private val callback: (AndroidOmapiPluginFactory) -> Unit) : AndroidOmapiPluginFactory {

    fun getFactory(): AndroidOmapiPluginFactory {
        return AndroidOmapiPluginFactoryAdapter(activity, callback)
    }
}
