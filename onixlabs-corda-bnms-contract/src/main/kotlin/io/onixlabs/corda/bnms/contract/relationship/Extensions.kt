package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import io.onixlabs.corda.identity.framework.contract.LinearAttestationPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

fun StateAndRef<Relationship>.getNextOutput(): Relationship {
    return state.data.copy(previousStateRef = ref)
}

fun StateAndRef<Relationship>.attest(
    attestor: AbstractParty,
    status: AttestationStatus,
    linearId: UniqueIdentifier = UniqueIdentifier()
): RelationshipAttestation {
    return RelationshipAttestation(attestor, this, status, linearId)
}

fun StateAndRef<Relationship>.accept(
    attestor: AbstractParty,
    linearId: UniqueIdentifier = UniqueIdentifier()
): RelationshipAttestation = attest(attestor, AttestationStatus.ACCEPTED, linearId)

fun StateAndRef<Relationship>.reject(
    attestor: AbstractParty,
    linearId: UniqueIdentifier = UniqueIdentifier()
): RelationshipAttestation = attest(attestor, AttestationStatus.REJECTED, linearId)

fun StateAndRef<RelationshipAttestation>.amend(status: AttestationStatus)
        : RelationshipAttestation = this.state.data.amend(status, ref)

fun StateAndRef<RelationshipAttestation>.amend(status: AttestationStatus, state: StateAndRef<Relationship>)
        : RelationshipAttestation = this.state.data.amend(LinearAttestationPointer(state), status, ref)

fun StateAndRef<RelationshipAttestation>.accept()
        : RelationshipAttestation = amend(AttestationStatus.ACCEPTED)

fun StateAndRef<RelationshipAttestation>.accept(state: StateAndRef<Relationship>)
        : RelationshipAttestation = amend(AttestationStatus.ACCEPTED, state)

fun StateAndRef<RelationshipAttestation>.reject()
        : RelationshipAttestation = amend(AttestationStatus.REJECTED)

fun StateAndRef<RelationshipAttestation>.reject(state: StateAndRef<Relationship>)
        : RelationshipAttestation = amend(AttestationStatus.REJECTED, state)