package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.membership.Membership
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

fun FlowLogic<*>.checkMembershipExists(membership: Membership) {
    if (subFlow(FindMembershipFlow(membership.bearer, membership.network, membership.previousStateRef)) != null) {
        throw FlowException("Membership state already exists in the vault.")
    }
}

fun FlowLogic<*>.checkSufficientSessions(state: ContractState, sessions: Iterable<FlowSession>) {
    val counterparties = state.participants - serviceHub.myInfo.legalIdentities
    if (counterparties.isNotEmpty() && sessions.any { it.counterparty !in counterparties }) {
        throw FlowException("Flow sessions are required for all counter-parties.")
    }
}

fun FlowLogic<*>.initiateFlow(counterparty: AbstractParty): FlowSession {
    return initiateFlow(serviceHub.identityService.requireWellKnownPartyFromAnonymous(counterparty))
}

fun FlowLogic<*>.initiateFlows(parties: Iterable<AbstractParty?>, vararg extras: AbstractParty?): Set<FlowSession> {
    val counterparties = (parties + extras).filterNotNull().filter { it !in serviceHub.myInfo.legalIdentities }
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