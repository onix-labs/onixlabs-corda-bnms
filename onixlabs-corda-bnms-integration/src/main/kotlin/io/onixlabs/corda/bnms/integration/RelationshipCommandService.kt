package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.Setting
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipMember
import io.onixlabs.corda.bnms.workflow.relationship.AmendRelationshipFlow
import io.onixlabs.corda.bnms.workflow.relationship.IssueRelationshipFlow
import io.onixlabs.corda.bnms.workflow.relationship.RevokeRelationshipFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

class RelationshipCommandService(rpc: CordaRPCOps) : Service(rpc) {

    fun issueRelationship(
        network: Network,
        members: Set<RelationshipMember> = emptySet(),
        settings: Set<Setting<*>> = emptySet(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        checkMembership: Boolean = false
    ): FlowProgressHandle<SignedTransaction> {
        val relationship = Relationship(network, members, settings, linearId)
        return issueRelationship(relationship, notary, checkMembership)
    }

    fun issueRelationship(
        relationship: Relationship,
        notary: Party? = null,
        checkMembership: Boolean = false
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueRelationshipFlow::Initiator,
            relationship,
            notary,
            checkMembership
        )
    }

    fun amendRelationship(
        oldRelationship: StateAndRef<Relationship>,
        newRelationship: Relationship,
        checkMembership: Boolean = false
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendRelationshipFlow::Initiator,
            oldRelationship,
            newRelationship,
            checkMembership
        )
    }

    fun revokeRelationship(
        relationship: StateAndRef<Relationship>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeRelationshipFlow::Initiator,
            relationship
        )
    }
}