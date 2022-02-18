/*
 * Copyright 2020-2022 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.core.workflow.currentStep
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.utilities.unwrap

@Suspendable
fun FlowLogic<*>.checkMembership(checkMembership: Boolean, relationship: Relationship, sessions: Set<FlowSession>) {
    currentStep(SendCheckMembershipInstructionStep)
    sessions.forEach { it.send(checkMembership) }
    if (checkMembership) checkMembershipsAndAttestations(relationship)
}

@Suspendable
fun FlowLogic<*>.checkMembershipHandler(session: FlowSession): Boolean {
    currentStep(ReceiveCheckMembershipInstructionStep)
    return session.receive<Boolean>().unwrap { it }
}

@Suspendable
fun FlowLogic<*>.checkMembershipExists(membership: Membership) {
    val membershipExists = serviceHub.vaultServiceFor<Membership>().any {
        stateStatus(Vault.StateStatus.ALL)
        expression(MembershipEntity::hash equalTo membership.hash.toString())
    }

    if (membershipExists) {
        throw FlowException("Membership state with the specified unique hash already exists: ${membership.hash}.")
    }
}

@Suspendable
fun FlowLogic<*>.checkMembershipsAndAttestations(relationship: Relationship) {
    currentStep(CheckMembershipStep)
    val counterparties = relationship.participants
        .map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }
        .filter { it !in serviceHub.myInfo.legalIdentities }

    counterparties.forEach {
        val membership = serviceHub.vaultServiceFor<Membership>().singleOrNull {
            expression(MembershipEntity::holder equalTo it)
            expression(MembershipEntity::networkHash equalTo relationship.network.hash.toString())
        } ?: throw FlowException(buildString {
            append("Membership for '$it' on network '${relationship.network}' ")
            append("could not be found, or has not been witnessed by this node.")
        })

        serviceHub.vaultServiceFor<MembershipAttestation>().singleOrNull {
            expression(MembershipAttestationEntity::attestor equalTo ourIdentity)
            expression(MembershipAttestationEntity::pointer equalTo membership.ref.toString())
        } ?: throw FlowException(buildString {
            append("MembershipAttestation for '${membership.state.data.holder}' ")
            append("could not be found, or has not been witnessed by this node.")
        })
    }
}

@Suspendable
fun FlowLogic<*>.checkRevocationLockExists(owner: AbstractParty, state: LinearState) {
    val revocationLockExists = serviceHub.vaultServiceFor<RevocationLock<*>>().any {
        expression(RevocationLockEntity::owner equalTo owner)
        expression(RevocationLockEntity::pointerStateClass equalTo state.javaClass.canonicalName)
        expression(RevocationLockEntity::pointerStateLinearId equalTo state.linearId.id)
    }

    if (revocationLockExists) {
        throw FlowException("Revocation of this relationship is locked by counter-party: $owner.")
    }
}

@Suspendable
fun FlowLogic<*>.findMembershipForAttestation(attestation: MembershipAttestation): StateAndRef<Membership> {
    return attestation.pointer.resolve(serviceHub) ?: throw FlowException(
        "Membership for the specified attestation could not be found, or has not been witnessed by this node."
    )
}

@Suspendable
fun FlowLogic<*>.findRelationshipForAttestation(attestation: RelationshipAttestation): StateAndRef<Relationship> {
    return attestation.pointer.resolve(serviceHub) ?: throw FlowException(
        "Relationship for the specified attestation could not be found, or has not been witnessed by this node."
    )
}
