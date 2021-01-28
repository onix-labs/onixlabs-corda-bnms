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

package io.onixlabs.corda.bnms.workflow

import io.onixlabs.corda.bnms.v1.contract.membership.Membership
import io.onixlabs.corda.bnms.v1.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.v1.contract.relationship.Relationship
import io.onixlabs.corda.bnms.v1.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.node.services.Vault

fun FlowLogic<*>.checkMembershipExists(membership: Membership) {
    subFlow(FindMembershipFlow(hash = membership.hash))?.let {
        throw FlowException("Membership state with the specified unique hash already exists: ${membership.hash}.")
    }
}

fun FlowLogic<*>.checkMembershipsAndAttestations(relationship: Relationship) {
    val counterparties = relationship.participants
        .map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }
        .filter { it !in serviceHub.myInfo.legalIdentities }

    counterparties.forEach {
        val findMembershipFlow = FindMembershipFlow(
            holder = it,
            network = relationship.network,
            stateStatus = Vault.StateStatus.UNCONSUMED
        )

        val membership = subFlow(findMembershipFlow) ?: throw FlowException(
            "Membership for specified holder and network could not be found, or has not been witnessed by this node."
        )

        val findAttestationFlow = FindMembershipAttestationFlow(
            attestor = ourIdentity,
            membership = membership,
            stateStatus = Vault.StateStatus.UNCONSUMED
        )

        subFlow(findAttestationFlow) ?: throw FlowException(
            "MembershipAttestation for specified membership could not be found, or has not been witnessed by this node."
        )
    }
}

fun FlowLogic<*>.findMembershipForAttestation(attestation: MembershipAttestation): StateAndRef<Membership> {
    return attestation.pointer.resolve(serviceHub) ?: throw FlowException(
        "Membership for the specified attestation could not be found, or has not been witnessed by this node."
    )
}

fun FlowLogic<*>.findRelationshipForAttestation(attestation: RelationshipAttestation): StateAndRef<Relationship> {
    return attestation.pointer.resolve(serviceHub) ?: throw FlowException(
        "Relationship for the specified attestation could not be found, or has not been witnessed by this node."
    )
}