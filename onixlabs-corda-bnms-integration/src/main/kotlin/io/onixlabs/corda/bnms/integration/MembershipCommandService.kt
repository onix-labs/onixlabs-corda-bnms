package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.Permission
import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.membership.AmendMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.RevokeMembershipFlow
import io.onixlabs.corda.identity.framework.contract.StaticClaimPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

class MembershipCommandService(rpc: CordaRPCOps) : Service(rpc) {

    fun issueMembership(
        network: Network,
        holder: AbstractParty = ourIdentity,
        identity: Set<StaticClaimPointer<*>> = emptySet(),
        roles: Set<Role> = emptySet(),
        permissions: Set<Permission> = emptySet(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val membership = Membership(network, holder, identity, roles, permissions, linearId)
        return issueMembership(membership, notary, observers)
    }

    fun issueMembership(
        membership: Membership,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueMembershipFlow::Initiator,
            membership,
            notary,
            observers
        )
    }

    fun amendMembership(
        oldMembership: StateAndRef<Membership>,
        newMembership: Membership,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendMembershipFlow::Initiator,
            oldMembership,
            newMembership,
            observers
        )
    }

    fun revokeMembership(
        membership: StateAndRef<Membership>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeMembershipFlow::Initiator,
            membership,
            observers
        )
    }
}