package io.onixlabs.corda.bnms.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.INITIALIZING
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

class SynchronizeMembershipFlow(
    private val ourMembership: StateAndRef<Membership>,
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, SENDING, RECEIVING)

        private const val FLOW_VERSION_1 = 1

        private object SENDING : ProgressTracker.Step("Sending our membership and attestations.")
        private object RECEIVING : ProgressTracker.Step("Receiving their membership and attestations.")
    }

    @Suspendable
    override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
        currentStep(INITIALIZING)
        val network = ourMembership.state.data.network
        val holder = ourMembership.state.data.holder

        if (holder !in serviceHub.myInfo.legalIdentities) {
            throw FlowException("Membership synchronization can only occur when the membership is owned by this node.")
        }

        val attestations = subFlow(
            FindMembershipAttestationsFlow(
                holder = holder,
                network = network,
                stateStatus = Vault.StateStatus.UNCONSUMED
            )
        )

        if (network.operator != null && attestations.size > 1) {
            throw FlowException("Only one membership attestation is required when a network operator is present.")
        }

        val isMemberOfNetwork = session.sendAndReceive<Boolean>(network).unwrap { it }

        if (isMemberOfNetwork) {
            currentStep(SENDING)
            sendOurMembership()
            sendOurAttestations(attestations)

            currentStep(RECEIVING)
            val theirMembership = receiveTheirMembership()
            val theirAttestations = receiveTheirAttestations()

            return theirMembership to theirAttestations
        }

        return null
    }

    @Suspendable
    private fun sendOurMembership() {
        subFlow(PublishMembershipFlow(ourMembership, setOf(session)))
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

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val ourMembership: StateAndRef<Membership>,
        private val counterparty: Party
    ) : FlowLogic<Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>?>() {

        private companion object {
            object SYNCHRONIZING : ProgressTracker.Step("Synchronizing membership.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(SYNCHRONIZING)

        @Suspendable
        override fun call(): Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>? {
            currentStep(SYNCHRONIZING)
            val session = initiateFlow(counterparty)

            return subFlow(
                SynchronizeMembershipFlow(
                    ourMembership,
                    session,
                    SYNCHRONIZING.childProgressTracker()
                )
            )
        }
    }
}
