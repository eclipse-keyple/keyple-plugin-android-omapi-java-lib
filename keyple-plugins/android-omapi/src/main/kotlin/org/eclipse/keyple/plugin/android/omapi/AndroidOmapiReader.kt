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

import org.eclipse.keyple.core.common.KeypleReaderExtension
import org.eclipse.keyple.core.plugin.spi.reader.AutonomousSelectionReaderSpi
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi

/**
 * Generic type for a Keyple Android OMAPI reader extension.
 *
 * @since 2.0
 */
interface AndroidOmapiReader : KeypleReaderExtension, AutonomousSelectionReaderSpi, ReaderSpi {
    companion object {
        const val READER_NAME_SIM_1 = "AndroidOmapiReaderSim1"
        const val READER_NAME_SIM_2 = "AndroidOmapiReaderSim2"
        const val READER_NAME_ESE = "AndroidOmapiReaderESE"
    }
}
