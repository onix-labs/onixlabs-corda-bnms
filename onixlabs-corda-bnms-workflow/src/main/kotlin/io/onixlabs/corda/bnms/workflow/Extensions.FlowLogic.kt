/*
 * Copyright 2020-2021 ONIXLabs
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
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey

@Suspendable
fun FlowLogic<*>.checkMembershipExists(membership: Membership) {
    val membershipExists = serviceHub.vaultServiceFor<Membership>().any {
        stateStatus(Vault.StateStatus.ALL)
        expression(MembershipSchema.MembershipEntity::hash equalTo membership.hash.toString())
    }

    if (membershipExists) {
        throw FlowException("Membership state with the specified unique hash already exists: ${membership.hash}.")
    }
}

@Suspendable
fun FlowLogic<*>.checkMembershipsAndAttestations(relationship: Relationship) {
    val counterparties = relationship.participants
        .map { serviceHub.identityService.requireWellKnownPartyFromAnonymous(it) }
        .filter { it !in serviceHub.myInfo.legalIdentities }

    counterparties.forEach {
        val membership = serviceHub.vaultServiceFor<Membership>().singleOrNull {
            expression(MembershipSchema.MembershipEntity::holder equalTo it)
            expression(MembershipSchema.MembershipEntity::networkHash equalTo relationship.network.hash.toString())
        } ?: throw FlowException(buildString {
            append("Membership for '$it' on network '${relationship.network}' ")
            append("could not be found, or has not been witnessed by this node.")
        })

        serviceHub.vaultServiceFor<MembershipAttestation>().singleOrNull {
            expression(MembershipAttestationSchema.MembershipAttestationEntity::attestor equalTo ourIdentity)
            expression(MembershipAttestationSchema.MembershipAttestationEntity::pointer equalTo membership.ref.toString())
        } ?: throw FlowException(buildString {
            append("MembershipAttestation for '${membership.state.data.holder}' ")
            append("could not be found, or has not been witnessed by this node.")
        })
    }
}

@Suspendable
fun FlowLogic<*>.checkRevocationLockExists(owner: AbstractParty, state: LinearState) {
    val revocationLockExists = serviceHub.vaultServiceFor<RevocationLock<*>>().any {
        expression(RevocationLockSchema.RevocationLockEntity::owner equalTo owner)
        expression(RevocationLockSchema.RevocationLockEntity::pointerStateClass equalTo state.javaClass.canonicalName)
        expression(RevocationLockSchema.RevocationLockEntity::pointerStateLinearId equalTo state.linearId.id)
    }

    if(revocationLockExists) {
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
//
///**
// * Generates an unsigned transaction.
// *
// * @param notary The notary to assign to the transaction.
// * @param action The context in which the [TransactionBuilder] will build the transaction.
// * @return Returns an unsigned transaction.
// */
//@Suspendable
//internal fun FlowLogic<*>.transaction(
//    notary: Party,
//    action: TransactionBuilder.() -> TransactionBuilder
//): TransactionBuilder {
//    currentStep(GENERATING)
//    return with(TransactionBuilder(notary)) { action(this) }
//}
//
///**
// * Verifies and signs an unsigned transaction.
// *
// * @param builder The unsigned transaction to verify and sign.
// * @param signingKey The initial signing ket for the transaction.
// * @return Returns a verified and signed transaction.
// */
//@Suspendable
//internal fun FlowLogic<*>.verifyAndSign(
//    builder: TransactionBuilder,
//    signingKey: PublicKey
//): SignedTransaction {
//    currentStep(VERIFYING)
//    builder.verify(serviceHub)
//
//    currentStep(SIGNING)
//    return serviceHub.signInitialTransaction(builder, signingKey)
//}
//
///**
// * Gathers counter-party signatures for a partially signed transaction.
// *
// * @param transaction The signed transaction for which to obtain additional signatures.
// * @param sessions The flow sessions for the required signing counter-parties.
// * @return Returns a signed transaction.
// */
//@Suspendable
//internal fun FlowLogic<*>.countersign(
//    transaction: SignedTransaction,
//    sessions: Set<FlowSession>
//): SignedTransaction {
//    currentStep(COUNTERSIGNING)
//    return subFlow(CollectSignaturesFlow(transaction, sessions, COUNTERSIGNING.childProgressTracker()))
//}
//
///**
// * Finalizes and records a signed transaction to the vault.
// *
// * @param transaction The transaction to finalize and record.
// * @param sessions The flow sessions for counter-parties who are expected to finalize and record the transaction.
// * @return Returns a finalized and recorded transaction.
// */
//@Suspendable
//internal fun FlowLogic<*>.finalize(
//    transaction: SignedTransaction,
//    sessions: Set<FlowSession> = emptySet()
//): SignedTransaction {
//    currentStep(FINALIZING)
//    return subFlow(FinalityFlow(transaction, sessions, FINALIZING.childProgressTracker()))
//}
