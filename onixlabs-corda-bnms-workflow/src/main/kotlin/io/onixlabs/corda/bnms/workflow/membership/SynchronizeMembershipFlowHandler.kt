package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.INITIALIZING
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.node.services.Vault
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

class SynchronizeMembershipFlowHandler(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, RECEIVING, SENDING)

        private object RECEIVING : ProgressTracker.Step("Receiving their membership and attestations.")
        private object SENDING : ProgressTracker.Step("Sending our membership and attestations.")
    }

    @Suspendable
    override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
        currentStep(INITIALIZING)
        val network = session.receive<Network>().unwrap { it }
        val membership = subFlow(FindMembershipFlow(holder = ourIdentity, network = network))

        session.send(membership != null)

        if (membership != null) {
            val attestations = subFlow(
                FindMembershipAttestationsFlow(
                    holder = ourIdentity,
                    network = network,
                    stateStatus = Vault.StateStatus.UNCONSUMED
                )
            )

            currentStep(RECEIVING)
            val theirMembership = receiveTheirMembership()
            val theirAttestations = receiveTheirAttestations()

            currentStep(SENDING)
            sendOurMembership(membership)
            sendOurAttestations(attestations)
            return theirMembership to theirAttestations
        }

        return null
    }

    @Suspendable
    private fun sendOurMembership(membership: StateAndRef<Membership>) {
        subFlow(PublishMembershipFlow(membership, setOf(session)))
    }

    @Suspendable
    private fun sendOurAttestations(attestations: Iterable<StateAndRef<MembershipAttestation>>) {
        session.send(attestations.count())

        for (attestation in attestations) {
            subFlow(PublishMembershipAttestationFlow(attestation, setOf(session)))
        }
    }

    @Suspendable
    private fun receiveTheirMembership(): StateAndRef<Membership> {
        val transaction = subFlow(PublishMembershipFlowHandler(session))
        return transaction.tx.outRefsOfType<Membership>().single()
    }

    @Suspendable
    private fun receiveTheirAttestations(): Set<StateAndRef<MembershipAttestation>> {
        val attestations = mutableSetOf<StateAndRef<MembershipAttestation>>()
        val theirAttestationSize = session.receive<Int>().unwrap { it }

        for (index in 0 until theirAttestationSize) {
            val transaction = subFlow(PublishMembershipAttestationFlowHandler(session))
            val attestation = transaction.tx.outRefsOfType<MembershipAttestation>().single()
            attestations.add(attestation)
        }

        return attestations
    }

    @InitiatedBy(SynchronizeMembershipFlow.Initiator::class)
    private class Handler(private val session: FlowSession) :
        FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

        private companion object {
            object SYNCHRONIZING : ProgressTracker.Step("Synchronizing membership.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(SYNCHRONIZING)

        @Suspendable
        override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
            currentStep(SYNCHRONIZING)
            return subFlow(SynchronizeMembershipFlowHandler(session, SYNCHRONIZING.childProgressTracker()))
        }
    }
}
