/*
 * Copyright 2020-2021 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.workflow.revocation.FindRevocationLockFlow
import io.onixlabs.corda.core.contract.cast
import io.onixlabs.corda.core.integration.RPCService
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.getOrThrow
import java.time.Duration

class RevocationLockQueryService(rpc: CordaRPCOps) : RPCService(rpc) {

    inline fun <reified T : LinearState> findRevocationLock(
        state: T,
        owner: AbstractParty = ourIdentity,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<T>? {
        return rpc.startFlowDynamic(
            FindRevocationLockFlow::class.java,
            owner,
            state
        ).returnValue.getOrThrow(flowTimeout)?.cast()
    }
}
