package io.onixlabs.corda.bnms.contract.relationship

import net.corda.core.contracts.StateAndRef

fun StateAndRef<Relationship>.getNextOutput(): Relationship {
    return state.data.copy(previousStateRef = ref)
}