package io.onixlabs.corda.bnms.workflow

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipAttestationByHolderFlow
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipByHashFlow
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipByHolderFlow
import net.corda.core.contracts.ContractState
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.AbstractParty

fun FlowLogic<*>.checkSufficientSessions(state: ContractState, sessions: Iterable<FlowSession>) {
    val stateCounterparties = state.participants - serviceHub.myInfo.legalIdentities
    val sessionCounterparties = sessions.map { it.counterparty }
    stateCounterparties.forEach {
        if (it !in sessionCounterparties) {
            throw FlowException("A flow session must be provided for the specified counter-party: $it.")
        }
    }
}

fun FlowLogic<*>.checkMembershipExists(membership: Membership) {
    if (subFlow(FindMembershipByHashFlow(membership.hash)) != null) {
        throw FlowException("Membership state with the specified unique hash already exists: ${membership.hash}.")
    }
}

fun FlowLogic<*>.checkMembershipsAndAttestations(relationship: Relationship, counterparties: Iterable<AbstractParty>) {
    counterparties.forEach {
        val membership = subFlow(FindMembershipByHolderFlow(it, relationship.network))
            ?: throw FlowException("Membership not found for counter-party: $it.")

        val attestation = subFlow(FindMembershipAttestationByHolderFlow(it, relationship.network))
            ?: throw FlowException("Membership attestation not found for counter-party: $it.")

        if (!attestation.state.data.pointer.isPointingTo(membership)) {
            throw FlowException("Latest attestation does not point to membership for counter-party: $it.")
        }
    }
}

fun FlowLogic<*>.filterCounterpartyIdentities(parties: Iterable<AbstractParty>): List<AbstractParty> {
    return parties.filter { it !in serviceHub.myInfo.legalIdentities }
}