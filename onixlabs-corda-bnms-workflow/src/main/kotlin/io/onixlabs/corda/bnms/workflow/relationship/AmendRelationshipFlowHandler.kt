package io.onixlabs.corda.bnms.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.checkMembershipsAndAttestations
import io.onixlabs.corda.bnms.workflow.filterCounterpartyIdentities
import io.onixlabs.corda.identity.framework.workflow.FINALIZING
import io.onixlabs.corda.identity.framework.workflow.SIGNING
import io.onixlabs.corda.identity.framework.workflow.currentStep
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

                    val counterparties = filterCounterpartyIdentities(relationship.participants)

                    checkMembershipsAndAttestations(relationship, counterparties)
                }
            }
        })

        currentStep(FINALIZING)
        return subFlow(ReceiveFinalityFlow(session, transaction.id, StatesToRecord.ONLY_RELEVANT))
    }
}