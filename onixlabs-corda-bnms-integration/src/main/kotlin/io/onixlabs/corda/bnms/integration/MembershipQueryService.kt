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

package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipsFlow
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration

class MembershipQueryService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun findMembership(
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        holder: AbstractParty? = null,
        network: Network? = null,
        networkValue: String? = null,
        networkOperator: AbstractParty? = null,
        networkHash: SecureHash? = null,
        isNetworkOperator: Boolean? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlowDynamic(
            FindMembershipFlow::class.java,
            linearId,
            externalId,
            holder,
            network,
            networkValue,
            networkOperator,
            networkHash,
            isNetworkOperator,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMemberships(
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        holder: AbstractParty? = null,
        network: Network? = null,
        networkValue: String? = null,
        networkOperator: AbstractParty? = null,
        networkHash: SecureHash? = null,
        isNetworkOperator: Boolean? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Membership>> {
        return rpc.startFlowDynamic(
            FindMembershipsFlow::class.java,
            linearId,
            externalId,
            holder,
            network,
            networkValue,
            networkOperator,
            networkHash,
            isNetworkOperator,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }
}
