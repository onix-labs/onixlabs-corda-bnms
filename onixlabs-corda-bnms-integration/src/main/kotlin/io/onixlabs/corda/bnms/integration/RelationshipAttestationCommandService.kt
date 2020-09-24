package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.attest
import io.onixlabs.corda.bnms.workflow.relationship.AmendRelationshipAttestationFlow
import io.onixlabs.corda.bnms.workflow.relationship.IssueRelationshipAttestationFlow
import io.onixlabs.corda.bnms.workflow.relationship.RevokeRelationshipAttestationFlow
import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

class RelationshipAttestationCommandService(rpc: CordaRPCOps) : Service(rpc) {

    fun issueRelationshipAttestation(
        relationship: StateAndRef<Relationship>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null
    ): FlowProgressHandle<SignedTransaction> {
        val attestation = relationship.attest(attestor, status, linearId)
        return issueRelationshipAttestation(attestation, notary)
    }

    fun issueRelationshipAttestation(
        attestation: RelationshipAttestation,
        notary: Party? = null
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueRelationshipAttestationFlow::Initiator,
            attestation,
            notary
        )
    }

    fun amendRelationshipAttestation(
        oldAttestation: StateAndRef<RelationshipAttestation>,
        newAttestation: RelationshipAttestation
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendRelationshipAttestationFlow::Initiator,
            oldAttestation,
            newAttestation
        )
    }

    fun revokeRelationshipAttestation(
        attestation: StateAndRef<RelationshipAttestation>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeRelationshipAttestationFlow::Initiator,
            attestation
        )
    }
}