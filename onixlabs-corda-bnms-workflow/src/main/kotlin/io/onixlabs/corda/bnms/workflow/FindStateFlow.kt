package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import kotlin.reflect.jvm.jvmErasure


abstract class FindStateFlow<T : ContractState> protected constructor(
    private val criteria: QueryCriteria,
    private val pageSpecification: PageSpecification = MAX_PAGE_SPECIFICATION
) : FlowLogic<StateAndRef<T>?>() {

    @Suppress("UNCHECKED_CAST")
    private val stateClass: Class<T>
        get() = javaClass.kotlin.supertypes[0].arguments[0].type?.jvmErasure?.javaObjectType as Class<T>

    @Suspendable
    override fun call(): StateAndRef<T>? {
        return serviceHub.vaultService.queryBy(stateClass, criteria, pageSpecification).states.singleOrNull()
    }
}