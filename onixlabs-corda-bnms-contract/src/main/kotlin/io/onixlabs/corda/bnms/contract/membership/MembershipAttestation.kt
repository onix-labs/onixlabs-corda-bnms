package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationSchemaV1
import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestation
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationSchema.EvolvableAttestationSchemaV1
import io.onixlabs.corda.identity.framework.contract.LinearAttestationPointer
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

@BelongsToContract(MembershipAttestationContract::class)
class MembershipAttestation private constructor(
    val network: Network,
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: LinearAttestationPointer<Membership>,
    status: AttestationStatus,
    linearId: UniqueIdentifier,
    previousStateRef: StateRef?
) : EvolvableAttestation<LinearAttestationPointer<Membership>>(
    attestor,
    attestees,
    pointer,
    status,
    linearId,
    previousStateRef
) {

    constructor(
        network: Network,
        attestor: AbstractParty,
        membership: StateAndRef<Membership>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        linearId: UniqueIdentifier = UniqueIdentifier(),
        previousStateRef: StateRef? = null
    ) : this(
        network,
        attestor,
        setOf(membership.state.data.holder),
        LinearAttestationPointer(membership),
        status,
        linearId,
        previousStateRef
    )

    override fun amend(status: AttestationStatus, previousStateRef: StateRef): MembershipAttestation {
        return MembershipAttestation(network, attestor, attestees, pointer, status, linearId, previousStateRef)
    }

    override fun amend(
        pointer: LinearAttestationPointer<Membership>,
        status: AttestationStatus,
        previousStateRef: StateRef
    ): MembershipAttestation {
        return MembershipAttestation(network, attestor, attestees, pointer, status, linearId, previousStateRef)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is EvolvableAttestationSchemaV1 -> super.generateMappedObject(schema)
        is MembershipAttestationSchemaV1 -> MembershipAttestationEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            attestor = attestor,
            attestee = attestees.single(),
            pointer = pointer.pointer.toString(),
            pointerType = pointer.type.canonicalName,
            networkName = network.value,
            normalizedNetworkName = network.normalizedValue,
            networkHash = network.hash.toString(),
            networkOperator = network.operator,
            status = status,
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(MembershipAttestationSchemaV1)
    }
}