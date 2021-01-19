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

package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipAttestationFlow
import io.onixlabs.corda.bnms.workflow.membership.FindMembershipAttestationsFlow
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.identityframework.contract.AttestationPointer
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration

class MembershipAttestationQueryService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun findMembershipAttestation(
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
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<MembershipAttestation>? {
        return rpc.startFlowDynamic(
            FindMembershipAttestationFlow::class.java,
            linearId,
            externalId,
            attestor,
            holder,
            network,
            networkValue,
            networkOperator,
            networkHash,
            pointer,
            pointerStateRef,
            pointerStateLinearId,
            pointerHash,
            status,
            previousStateRef,
            hash,
            membership,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestations(
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
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlowDynamic(
            FindMembershipAttestationsFlow::class.java,
            linearId,
            externalId,
            attestor,
            holder,
            network,
            networkValue,
            networkOperator,
            networkHash,
            pointer,
            pointerStateRef,
            pointerStateLinearId,
            pointerHash,
            status,
            previousStateRef,
            hash,
            membership,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }
}
