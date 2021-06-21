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

package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.NetworkState
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationSchemaV1
import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.AttestationPointer
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import io.onixlabs.corda.identityframework.contract.toAttestationPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

@BelongsToContract(MembershipAttestationContract::class)
class MembershipAttestation internal constructor(
    override val network: Network,
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: AttestationPointer<Membership>,
    status: AttestationStatus,
    metadata: Map<String, String>,
    linearId: UniqueIdentifier,
    previousStateRef: StateRef?
) : Attestation<Membership>(
    attestor,
    attestees,
    pointer,
    status,
    metadata,
    linearId,
    previousStateRef
), NetworkState {

    constructor(
        attestor: AbstractParty,
        membership: StateAndRef<Membership>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        previousStateRef: StateRef? = null
    ) : this(
        membership.state.data.network,
        attestor,
        setOf(membership.state.data.holder),
        membership.toAttestationPointer(),
        status,
        metadata,
        linearId,
        previousStateRef
    )

    val holder: AbstractParty
        get() = attestees.single()

    val isNetworkOperator: Boolean
        get() = holder == network.operator

    override fun amend(
        previousStateRef: StateRef,
        status: AttestationStatus,
        pointer: AttestationPointer<Membership>,
        metadata: Map<String, String>
    ): MembershipAttestation {
        return MembershipAttestation(
            network,
            attestor,
            attestees,
            pointer,
            status,
            metadata,
            linearId,
            previousStateRef
        )
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipAttestationSchemaV1 -> MembershipAttestationEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            attestor = attestor,
            holder = holder,
            networkValue = network.value,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            pointerStateRef = pointer.stateRef.toString(),
            pointerStateClass = pointer.stateClass.canonicalName,
            pointerStateLinearId = pointer.stateLinearId!!.id,
            pointerHash = pointer.hash.toString(),
            status = status,
            previousStateRef = previousStateRef?.toString(),
            hash = hash.toString()
        )
        else -> super.generateMappedObject(schema)
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return super.supportedSchemas() + MembershipAttestationSchemaV1
    }
}
