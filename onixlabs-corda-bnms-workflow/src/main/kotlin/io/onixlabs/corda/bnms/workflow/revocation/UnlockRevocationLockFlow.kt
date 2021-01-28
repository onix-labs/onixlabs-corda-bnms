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

package io.onixlabs.corda.bnms.workflow.revocation

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.v1.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.v1.contract.revocation.RevocationLockContract
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class UnlockRevocationLockFlow(
    private val revocationLock: StateAndRef<RevocationLock<*>>
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(GENERATING, VERIFYING, SIGNING, FINALIZING)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = transaction(revocationLock.state.notary) {
            addInputState(revocationLock)
            addCommand(RevocationLockContract.Unlock, revocationLock.state.data.owner.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction, revocationLock.state.data.owner.owningKey)
        return finalize(signedTransaction)
    }
}
