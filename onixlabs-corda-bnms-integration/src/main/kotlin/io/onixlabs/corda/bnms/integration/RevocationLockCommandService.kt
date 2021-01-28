/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.workflow.revocation.LockRevocationLockFlow
import io.onixlabs.corda.bnms.workflow.revocation.UnlockRevocationLockFlow
import io.onixlabs.corda.core.integration.RPCService
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

class RevocationLockCommandService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun <T : LinearState> lock(
        state: T,
        owner: AbstractParty = ourIdentity,
        notary: Party? = null
    ): FlowProgressHandle<SignedTransaction> {
        val lock = RevocationLock(owner, state)
        return rpc.startTrackedFlow(::LockRevocationLockFlow, lock, notary)
    }

    fun <T : LinearState> unlock(
        lock: StateAndRef<RevocationLock<T>>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(::UnlockRevocationLockFlow, lock)
    }
}