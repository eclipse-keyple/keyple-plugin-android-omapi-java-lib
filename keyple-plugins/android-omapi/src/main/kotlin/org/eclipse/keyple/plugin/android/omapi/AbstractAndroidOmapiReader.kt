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

import org.eclipse.keyple.core.util.protocol.ContactCardCommonProtocol

internal abstract class AbstractAndroidOmapiReader(private val readerName: String) : AndroidOmapiReader {

    override fun getName(): String {
        return readerName
    }

    override fun isProtocolSupported(readerProtocol: String?): Boolean {
        return ContactCardCommonProtocol.ISO_7816_3.name == readerProtocol
    }

    override fun activateProtocol(readerProtocol: String?) {
        // OMAPI only support ContactCardCommonProtocol.ISO_7816_3
        // Do nothing
    }

    override fun deactivateProtocol(readerProtocol: String?) {
        // OMAPI only support ContactCardCommonProtocol.ISO_7816_3
        // Do nothing
    }

    override fun isCurrentProtocol(readerProtocol: String): Boolean {
        return ContactCardCommonProtocol.ISO_7816_3.name == readerProtocol
    }

    override fun isContactless(): Boolean {
        return false
    }
}
