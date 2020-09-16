package io.onixlabs.corda.bnms.contract

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.schemas.QueryableState

/**
 * Defines a contract state that includes a claim to a business network.
 *
 * @property network The identity of the business network.
 */
interface NetworkState : ContractState, LinearState, QueryableState {
    val network: Network
}