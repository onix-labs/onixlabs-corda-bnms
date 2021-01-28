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

package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.v1.contract.Network
import io.onixlabs.corda.bnms.v1.contract.relationship.Relationship
import io.onixlabs.corda.bnms.v1.contract.relationship.RelationshipSchema.RelationshipEntity
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
class FindRelationshipFlow(
    linearId: UniqueIdentifier? = null,
    externalId: String? = null,
    network: Network? = null,
    networkValue: String? = null,
    networkOperator: AbstractParty? = null,
    networkHash: SecureHash? = null,
    hash: SecureHash? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION
) : FindStateFlow<Relationship>() {
    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(contractStateType),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).andWithExpressions(
        linearId?.let { RelationshipEntity::linearId.equal(it.id) },
        externalId?.let { RelationshipEntity::externalId.equal(it) },
        network?.let { RelationshipEntity::networkHash.equal(it.hash.toString()) },
        networkValue?.let { RelationshipEntity::networkValue.equal(it) },
        networkOperator?.let { RelationshipEntity::networkOperator.equal(it) },
        networkHash?.let { RelationshipEntity::networkHash.equal(it.toString()) },
        hash?.let { RelationshipEntity::hash.equal(it.toString()) }
    )
}
