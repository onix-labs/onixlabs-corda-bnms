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

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationEntity
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.core.workflow.DEFAULT_SORTING
import io.onixlabs.corda.core.workflow.FindStatesFlow
import io.onixlabs.corda.core.workflow.andWithExpressions
import io.onixlabs.corda.identityframework.contract.AttestationPointer
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
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
import net.corda.core.node.services.vault.Sort

@StartableByRPC
@StartableByService
class FindRelationshipAttestationsFlow(
    linearId: UniqueIdentifier? = null,
    externalId: String? = null,
    attestor: AbstractParty? = null,
    network: Network? = null,
    networkValue: String? = null,
    networkOperator: AbstractParty? = null,
    networkHash: SecureHash? = null,
    pointer: AttestationPointer<Relationship>? = null,
    pointerStateRef: StateRef? = null,
    pointerStateLinearId: UniqueIdentifier? = null,
    pointerHash: SecureHash? = null,
    status: AttestationStatus? = null,
    previousStateRef: StateRef? = null,
    hash: SecureHash? = null,
    relationship: StateAndRef<Relationship>? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
    override val sorting: Sort = DEFAULT_SORTING
) : FindStatesFlow<RelationshipAttestation>() {
    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(contractStateType),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).andWithExpressions(
        linearId?.let { RelationshipAttestationEntity::linearId.equal(it.id) },
        externalId?.let { RelationshipAttestationEntity::externalId.equal(it) },
        attestor?.let { RelationshipAttestationEntity::attestor.equal(it) },
        network?.let { RelationshipAttestationEntity::networkHash.equal(it.hash.toString()) },
        networkValue?.let { RelationshipAttestationEntity::networkValue.equal(it) },
        networkOperator?.let { RelationshipAttestationEntity::networkOperator.equal(it) },
        networkHash?.let { RelationshipAttestationEntity::networkHash.equal(it.toString()) },
        pointer?.let { RelationshipAttestationEntity::pointerHash.equal(it.hash.toString()) },
        pointerStateRef?.let { RelationshipAttestationEntity::pointerStateRef.equal(it.toString()) },
        pointerStateLinearId?.let { RelationshipAttestationEntity::pointerStateLinearId.equal(it.id) },
        pointerHash?.let { RelationshipAttestationEntity::pointerHash.equal(it.toString()) },
        status?.let { RelationshipAttestationEntity::status.equal(it) },
        previousStateRef?.let { RelationshipAttestationEntity::previousStateRef.equal(it.toString()) },
        relationship?.let { RelationshipAttestationEntity::pointerStateRef.equal(it.ref.toString()) },
        hash?.let { RelationshipAttestationEntity::hash.equal(it.toString()) }
    )
}
