package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import kotlin.reflect.jvm.jvmErasure

@StartableByRPC
abstract class FindStatesFlow<T : ContractState> protected constructor(
    private val criteria: QueryCriteria,
    private val pageSpecification: PageSpecification = MAX_PAGE_SPECIFICATION
) : FlowLogic<List<StateAndRef<T>>>() {

    @Suppress("UNCHECKED_CAST")
    private val stateClass: Class<T>
        get() = javaClass.kotlin.supertypes[0].arguments[0].type?.jvmErasure?.javaObjectType as Class<T>

    @Suspendable
    override fun call(): List<StateAndRef<T>> {
        return serviceHub.vaultService.queryBy(stateClass, criteria, pageSpecification).states
    }
}