package io.onixlabs.corda.bnms.contract.revocation

import io.onixlabs.corda.identity.framework.contract.Resolvable
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.util.*

@CordaSerializable
class RevocationLockPointer<T : LinearState>(val linearId: UniqueIdentifier, val type: Class<T>) : Resolvable<T> {

    companion object {
        private const val EX_CANNOT_RESOLVE =
            "State with linearId is not known by this node:"
    }

    private val criteria = LinearStateQueryCriteria(
        contractStateTypes = setOf(type),
        relevancyStatus = Vault.RelevancyStatus.RELEVANT,
        status = Vault.StateStatus.UNCONSUMED,
        linearId = listOf(linearId)
    )

    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T> {
        return cordaRPCOps.vaultQueryByCriteria(criteria, type).states.singleOrNull()
            ?: throw IllegalStateException("$EX_CANNOT_RESOLVE $linearId")
    }

    override fun resolve(serviceHub: ServiceHub): StateAndRef<T> {
        return serviceHub.vaultService.queryBy(type, criteria).states.singleOrNull()
            ?: throw IllegalStateException("$EX_CANNOT_RESOLVE $linearId")
    }

    override fun resolve(transaction: LedgerTransaction): StateAndRef<T> {
        return transaction.referenceInputRefsOfType(type).singleOrNull { it.state.data.linearId == linearId }
            ?: throw IllegalStateException("State with specified linearId has not been referenced in the transaction $linearId")
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is RevocationLockPointer<*>
                && other.linearId == linearId
                && other.type == type)
    }

    override fun hashCode(): Int {
        return Objects.hash(linearId, type)
    }

    override fun isPointingTo(stateAndRef: StateAndRef<T>): Boolean {
        return stateAndRef.state.data.javaClass == type && stateAndRef.state.data.linearId == linearId
    }
}