package io.onixlabs.corda.bnms.workflow

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.SignedTransaction

inline fun <reified T : ContractState> SignedTransaction.getOutput(): StateAndRef<T> {
    return tx.outRefsOfType<T>().single()
}

inline fun <reified T : ContractState> SignedTransaction.getOutputs(): List<StateAndRef<T>> {
    return tx.outRefsOfType<T>()
}