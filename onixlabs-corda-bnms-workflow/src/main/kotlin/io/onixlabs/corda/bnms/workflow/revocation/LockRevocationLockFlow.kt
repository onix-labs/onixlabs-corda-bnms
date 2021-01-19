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
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import io.onixlabs.corda.core.workflow.getPreferredNotary
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class LockRevocationLockFlow(
    private val revocationLock: RevocationLock<*>,
    private val notary: Party? = null
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(GENERATING, VERIFYING, SIGNING, FINALIZING)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction = transaction(notary ?: getPreferredNotary()) {
            addOutputState(revocationLock, RevocationLockContract.ID)
            addCommand(RevocationLockContract.Lock, revocationLock.owner.owningKey)
        }

        val signedTransaction = verifyAndSign(transaction, revocationLock.owner.owningKey)
        return finalize(signedTransaction)
    }
}
