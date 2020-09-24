package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.attest
import io.onixlabs.corda.bnms.workflow.membership.AmendMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.RevokeMembershipAttestationFlow
import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

class MembershipAttestationCommandService(rpc: CordaRPCOps) : Service(rpc) {

    fun issueMembershipAttestation(
        membership: StateAndRef<Membership>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val attestation = membership.attest(attestor, status, linearId)
        return issueMembershipAttestation(attestation, notary, observers)
    }

    fun issueMembershipAttestation(
        attestation: MembershipAttestation,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueMembershipAttestationFlow::Initiator,
            attestation,
            notary,
            observers
        )
    }

    fun amendMembershipAttestation(
        oldAttestation: StateAndRef<MembershipAttestation>,
        newAttestation: MembershipAttestation,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendMembershipAttestationFlow::Initiator,
            oldAttestation,
            newAttestation,
            observers
        )
    }

    fun revokeMembershipAttestation(
        attestation: StateAndRef<MembershipAttestation>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeMembershipAttestationFlow::Initiator,
            attestation,
            observers
        )
    }
}