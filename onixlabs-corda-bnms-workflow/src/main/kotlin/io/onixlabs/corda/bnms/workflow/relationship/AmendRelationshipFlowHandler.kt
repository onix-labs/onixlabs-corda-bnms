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

package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.checkMembershipsAndAttestations
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.FINALIZING
import io.onixlabs.corda.identityframework.workflow.SIGNING
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

class AmendRelationshipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(RECEIVING, SIGNING, FINALIZING)

        private object RECEIVING : Step("Receiving check membership instruction.")
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(RECEIVING)
        val checkMembership = session.receive<Boolean>().unwrap { it }

        currentStep(SIGNING)
        val transaction = subFlow(object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
                if (checkMembership) {
                    val relationship = stx.tx.outputsOfType<Relationship>().singleOrNull()
                        ?: throw FlowException("Failed to obtain a relationship from the transaction.")

                    checkMembershipsAndAttestations(relationship)
                }
            }
        })

        currentStep(FINALIZING)
        return subFlow(ReceiveFinalityFlow(session, transaction.id, StatesToRecord.ONLY_RELEVANT))
    }
}
