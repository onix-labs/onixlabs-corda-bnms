package io.onixlabs.corda.bnms.integration

import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps

abstract class Service(protected val rpc: CordaRPCOps) {

    val ourIdentity: Party get() = rpc.nodeInfo().legalIdentities.first()
}