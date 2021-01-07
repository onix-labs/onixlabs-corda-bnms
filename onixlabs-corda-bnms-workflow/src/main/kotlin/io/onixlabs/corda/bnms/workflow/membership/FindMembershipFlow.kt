/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.core.workflow.FindStateFlow
import io.onixlabs.corda.core.workflow.andWithExpressions
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria

@StartableByRPC
@StartableByService
class FindMembershipFlow(
    linearId: UniqueIdentifier? = null,
    externalId: String? = null,
    holder: AbstractParty? = null,
    network: Network? = null,
    networkValue: String? = null,
    networkOperator: AbstractParty? = null,
    networkHash: SecureHash? = null,
    isNetworkOperator: Boolean? = null,
    hash: SecureHash? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION
) : FindStateFlow<Membership>() {
    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(contractStateType),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).andWithExpressions(
        linearId?.let { MembershipEntity::linearId.equal(it.id) },
        externalId?.let { MembershipEntity::externalId.equal(it) },
        holder?.let { MembershipEntity::holder.equal(it) },
        network?.let { MembershipEntity::networkHash.equal(it.hash.toString()) },
        networkValue?.let { MembershipEntity::networkValue.equal(it) },
        networkOperator?.let { MembershipEntity::networkOperator.equal(it) },
        networkHash?.let { MembershipEntity::networkHash.equal(it.toString()) },
        isNetworkOperator?.let { MembershipEntity::isNetworkOperator.equal(it) },
        hash?.let { MembershipEntity::hash.equal(it.toString()) }
    )
}
