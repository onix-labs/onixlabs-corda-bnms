package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.membership.FindLatestMembershipAttestationByBearerFlow
import io.onixlabs.corda.bnms.workflow.membership.FindLatestMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.FindVersionedMembershipFlow
import net.corda.core.contracts.ContractState
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.security.PublicKey

val FlowLogic<*>.firstNotary: Party get() = serviceHub.networkMapCache.notaryIdentities.first()

fun FlowLogic<*>.currentStep(step: ProgressTracker.Step) {
    progressTracker?.currentStep = step
    logger.info("IDENTITY = $ourIdentity, FLOW = ${javaClass.simpleName}, STEP = ${step.label}")
}

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
    val existingMembership = if (membership.previousStateRef == null) {
        subFlow(FindLatestMembershipFlow(membership.bearer, membership.network))
    } else {
        subFlow(FindVersionedMembershipFlow(membership.bearer, membership.network, membership.previousStateRef!!))
    }

    if (existingMembership != null) {
        throw FlowException("Membership state with the specified unique hash already exists: ${membership.hash}.")
    }
}

fun FlowLogic<*>.checkMembershipsAndAttestations(relationship: Relationship, counterparties: Iterable<AbstractParty>) {
    counterparties.forEach {
        val membership = subFlow(FindLatestMembershipFlow(it, relationship.network))
            ?: throw FlowException("Membership not found for counter-party: $it.")

        val attestation = subFlow(FindLatestMembershipAttestationByBearerFlow(it, relationship.network))
            ?: throw FlowException("Membership attestation not found for counter-party: $it.")

        if (!attestation.state.data.pointer.isPointingTo(membership)) {
            throw FlowException("Latest attestation does not point to membership for counter-party: $it.")
        }
    }
}

fun FlowLogic<*>.filterOurIdentities(parties: Iterable<AbstractParty>): List<AbstractParty> {
    return parties.filter { it in serviceHub.myInfo.legalIdentities }
}

fun FlowLogic<*>.filterCounterpartyIdentities(parties: Iterable<AbstractParty>): List<AbstractParty> {
    return parties.filter { it !in serviceHub.myInfo.legalIdentities }
}

fun FlowLogic<*>.isOurIdentity(party: AbstractParty?): Boolean {
    return party in serviceHub.myInfo.legalIdentities
}

fun FlowLogic<*>.initiateFlow(counterparty: AbstractParty): FlowSession {
    return initiateFlow(serviceHub.identityService.requireWellKnownPartyFromAnonymous(counterparty))
}

fun FlowLogic<*>.initiateFlows(parties: Iterable<AbstractParty?>, vararg extras: AbstractParty?): Set<FlowSession> {
    val counterparties = filterCounterpartyIdentities((parties + extras).filterNotNull())
    return counterparties.map { initiateFlow(it) }.toSet()
}

fun FlowLogic<*>.initiateFlows(vararg counterparties: AbstractParty?): Set<FlowSession> {
    return initiateFlows(counterparties.toList())
}

/**
 * Generates an unsigned transaction.
 *
 * @param notary The notary to use for the generated transaction.
 * @param action An action which builds and results in the unsigned transaction.
 * @return Returns a [TransactionBuilder] representing the unsigned transaction.
 */
fun FlowLogic<*>.transaction(notary: Party, action: TransactionBuilder.() -> TransactionBuilder): TransactionBuilder {
    currentStep(GENERATING)
    return with(TransactionBuilder(notary)) { action(this) }
}

/**
 * Verifies and signs an unsigned transaction.
 *
 * @param builder The unsigned transaction to verify and sign.
 * @param signingKey The required key to sign the transaction.
 * @return Returns a signed transaction.
 */
fun FlowLogic<*>.verifyAndSign(
    builder: TransactionBuilder,
    vararg signingKeys: PublicKey = arrayOf(ourIdentity.owningKey)
): SignedTransaction {
    currentStep(VERIFYING)
    builder.verify(serviceHub)

    currentStep(SIGNING)
    return serviceHub.signInitialTransaction(builder, signingKeys.toSet())
}

/**
 * Counter-signs a partially signed transaction.
 *
 * @param signedTransaction The partially signed transaction to countersign.
 * @param sessions The flow sessions for the required counter-signing parties.
 * @return Returns a signed transaction.
 */
@Suspendable
fun FlowLogic<*>.countersign(
    signedTransaction: SignedTransaction,
    sessions: Set<FlowSession>
): SignedTransaction {
    currentStep(COUNTERSIGNING)
    return subFlow(CollectSignaturesFlow(signedTransaction, sessions, COUNTERSIGNING.childProgressTracker()))
}

/**
 * Finalizes a signed transaction and sends it to the specified flow sessions.
 *
 * @param signedTransaction The signed transaction to finalize.
 * @param sessions The flow sessions to distribute the finalized transaction to.
 * @return Returns the finalized signed transaction.
 */
@Suspendable
fun FlowLogic<*>.finalize(
    signedTransaction: SignedTransaction,
    sessions: Set<FlowSession> = emptySet()
): SignedTransaction {
    currentStep(FINALIZING)
    return subFlow(FinalityFlow(signedTransaction, sessions, FINALIZING.childProgressTracker()))
}