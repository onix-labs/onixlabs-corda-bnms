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

package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.core.workflow.FindStateFlow
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

@StartableByRPC
@StartableByService
class FindMembershipAttestationFlow(
    linearId: UniqueIdentifier? = null,
    externalId: String? = null,
    attestor: AbstractParty? = null,
    holder: AbstractParty? = null,
    network: Network? = null,
    networkValue: String? = null,
    networkOperator: AbstractParty? = null,
    networkHash: SecureHash? = null,
    pointer: AttestationPointer<Membership>? = null,
    pointerStateRef: StateRef? = null,
    pointerStateLinearId: UniqueIdentifier? = null,
    pointerHash: SecureHash? = null,
    status: AttestationStatus? = null,
    previousStateRef: StateRef? = null,
    hash: SecureHash? = null,
    membership: StateAndRef<Membership>? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION
) : FindStateFlow<MembershipAttestation>() {
    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(contractStateType),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).andWithExpressions(
        linearId?.let { MembershipAttestationEntity::linearId.equal(it.id) },
        externalId?.let { MembershipAttestationEntity::externalId.equal(it) },
        attestor?.let { MembershipAttestationEntity::attestor.equal(it) },
        holder?.let { MembershipAttestationEntity::holder.equal(it) },
        network?.let { MembershipAttestationEntity::networkHash.equal(it.hash.toString()) },
        networkValue?.let { MembershipAttestationEntity::networkValue.equal(it) },
        networkOperator?.let { MembershipAttestationEntity::networkOperator.equal(it) },
        networkHash?.let { MembershipAttestationEntity::networkHash.equal(it.toString()) },
        pointer?.let { MembershipAttestationEntity::pointerHash.equal(it.hash.toString()) },
        pointerStateRef?.let { MembershipAttestationEntity::pointerStateRef.equal(it.toString()) },
        pointerStateLinearId?.let { MembershipAttestationEntity::pointerStateLinearId.equal(it.id) },
        pointerHash?.let { MembershipAttestationEntity::pointerHash.equal(it.toString()) },
        status?.let { MembershipAttestationEntity::status.equal(it) },
        previousStateRef?.let { MembershipAttestationEntity::previousStateRef.equal(it.toString()) },
        membership?.let { MembershipAttestationEntity::pointerStateRef.equal(it.ref.toString()) },
        hash?.let { MembershipAttestationEntity::hash.equal(it.toString()) }
    )
}
