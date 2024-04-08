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
package org.eclipse.keyple.plugin.android.omapi.simalliance

import android.content.Context
import org.eclipse.keyple.plugin.android.omapi.SeServiceFactory
import org.simalliance.openmobileapi.SEService

/**
 * Implementation of [SeServiceFactory] using org.simalliance.openmobileapi
 *
 * @since 2.0.0
 */
internal class SeServiceFactoryAdapter(private val applicationContext: Context) :
    SeServiceFactory<SEService, SEService.CallBack> {

  override fun connectToSe(callBack: SEService.CallBack): SEService {
    return SEService(applicationContext, callBack)
  }
}
