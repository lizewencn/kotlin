/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.daemon.client.reportFromDaemon
import org.jetbrains.kotlin.daemon.common.*
import org.jetbrains.kotlin.gradle.plugin.kotlinDebug
import java.io.Serializable
import java.rmi.Remote
import java.rmi.server.UnicastRemoteObject

internal open class GradleCompilerServicesFacadeImpl(
    private val log: KotlinLogger,
    private val compilerMessageCollector: MessageCollector,
    port: Int = SOCKET_ANY_FREE_PORT
) : UnicastRemoteObject(port, LoopbackNetworkInterface.clientLoopbackSocketFactory, LoopbackNetworkInterface.serverLoopbackSocketFactory),
    CompilerServicesFacadeBase,
    Remote {

    override fun report(category: Int, severity: Int, message: String?, attachment: Serializable?) {
        when (ReportCategory.fromCode(category)) {
            ReportCategory.IC_MESSAGE -> {
                log.kotlinDebug { "[IC] $message" }
            }
            ReportCategory.DAEMON_MESSAGE -> {
                log.kotlinDebug { "[DAEMON] $message" }
            }
            else -> {
                compilerMessageCollector.reportFromDaemon(
                    outputsCollector = null,
                    category = category,
                    severity = severity,
                    message = message,
                    attachment = attachment
                )
            }
        }
    }
}

internal class GradleIncrementalCompilerServicesFacadeImpl(
    log: KotlinLogger,
    messageCollector: MessageCollector,
    port: Int = SOCKET_ANY_FREE_PORT
) : GradleCompilerServicesFacadeImpl(log, messageCollector, port),
    IncrementalCompilerServicesFacade