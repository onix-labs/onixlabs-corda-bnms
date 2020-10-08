package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationEntity
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationSchemaV1
import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestation
import io.onixlabs.corda.identity.framework.contract.LinearAttestationPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

@BelongsToContract(RelationshipAttestationContract::class)
class RelationshipAttestation private constructor(
    val network: Network,
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: LinearAttestationPointer<Relationship>,
    status: AttestationStatus,
    linearId: UniqueIdentifier,
    previousStateRef: StateRef?
) : EvolvableAttestation<LinearAttestationPointer<Relationship>>(
    attestor,
    attestees,
    pointer,
    status,
    linearId,
    previousStateRef
) {

    constructor(
        attestor: AbstractParty,
        relationship: StateAndRef<Relationship>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        linearId: UniqueIdentifier = UniqueIdentifier(),
        previousStateRef: StateRef? = null
    ) : this(
        relationship.state.data.network,
        attestor,
        (relationship.state.data.participants - attestor).toSet(),
        LinearAttestationPointer(relationship),
        status,
        linearId,
        previousStateRef
    )

    override fun amend(status: AttestationStatus, previousStateRef: StateRef): RelationshipAttestation {
        return RelationshipAttestation(network, attestor, attestees, pointer, status, linearId, previousStateRef)
    }

    override fun amend(
        pointer: LinearAttestationPointer<Relationship>,
        status: AttestationStatus,
        previousStateRef: StateRef
    ): RelationshipAttestation {
        return RelationshipAttestation(network, attestor, attestees, pointer, status, linearId, previousStateRef)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipAttestationSchemaV1 -> RelationshipAttestationEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            attestor = attestor,
            pointer = pointer.pointer.toString(),
            pointerType = pointer.type.canonicalName,
            networkValue = network.value,
            normalizedNetworkValue = network.normalizedValue,
            networkHash = network.hash.toString(),
            networkOperator = network.operator,
            status = status,
            hash = hash.toString()
        )
        else -> super.generateMappedObject(schema)
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return super.supportedSchemas() + RelationshipAttestationSchemaV1
    }
}