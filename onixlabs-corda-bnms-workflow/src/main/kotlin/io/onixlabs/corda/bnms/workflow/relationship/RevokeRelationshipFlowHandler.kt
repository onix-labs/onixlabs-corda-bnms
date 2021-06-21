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

package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.revocation.FindRevocationLockFlow
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.FINALIZING
import io.onixlabs.corda.identityframework.workflow.SIGNING
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class RevokeRelationshipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(SIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(SIGNING)
        val transaction = subFlow(object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
                val relationshipStateRef = stx.tx.inputs.singleOrNull() ?: throw FlowException(
                    "Failed to obtain a single state reference from the transaction."
                )

                val relationship = serviceHub.toStateAndRef<Relationship>(relationshipStateRef)
                subFlow(FindRevocationLockFlow(ourIdentity, relationship.state.data))?.let {
                    throw FlowException("Revocation of this relationship is locked by counter-party: $ourIdentity")
                }
            }
        })

        currentStep(FINALIZING)
        return subFlow(ReceiveFinalityFlow(session, transaction.id, StatesToRecord.ONLY_RELEVANT))
    }

    @InitiatedBy(RevokeRelationshipFlow.Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : ProgressTracker.Step("Observing relationship revocation.") {
                override fun childProgressTracker() = RevokeRelationshipFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(RevokeRelationshipFlowHandler(session, OBSERVING.childProgressTracker()))
        }
    }
}
