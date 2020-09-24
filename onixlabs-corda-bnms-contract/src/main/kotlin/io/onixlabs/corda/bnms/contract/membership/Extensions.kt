package io.onixlabs.corda.bnms.contract.membership

import io.onixlabs.corda.identity.framework.contract.AttestationStatus
import io.onixlabs.corda.identity.framework.contract.LinearAttestationPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

fun StateAndRef<Membership>.getNextOutput(): Membership {
    return state.data.copy(previousStateRef = ref)
}

fun StateAndRef<Membership>.attest(
    attestor: AbstractParty,
    status: AttestationStatus,
    linearId: UniqueIdentifier = UniqueIdentifier()
): MembershipAttestation {
    return MembershipAttestation(attestor, this, status, linearId)
}

fun StateAndRef<Membership>.accept(
    attestor: AbstractParty,
    linearId: UniqueIdentifier = UniqueIdentifier()
): MembershipAttestation = attest(attestor, AttestationStatus.ACCEPTED, linearId)

fun StateAndRef<Membership>.reject(
    attestor: AbstractParty,
    linearId: UniqueIdentifier = UniqueIdentifier()
): MembershipAttestation = attest(attestor, AttestationStatus.REJECTED, linearId)

fun StateAndRef<MembershipAttestation>.amend(status: AttestationStatus)
        : MembershipAttestation = this.state.data.amend(status, ref)

fun StateAndRef<MembershipAttestation>.amend(status: AttestationStatus, state: StateAndRef<Membership>)
        : MembershipAttestation = this.state.data.amend(LinearAttestationPointer(state), status, ref)

fun StateAndRef<MembershipAttestation>.accept()
        : MembershipAttestation = amend(AttestationStatus.ACCEPTED)

fun StateAndRef<MembershipAttestation>.accept(state: StateAndRef<Membership>)
        : MembershipAttestation = amend(AttestationStatus.ACCEPTED, state)

fun StateAndRef<MembershipAttestation>.reject()
        : MembershipAttestation = amend(AttestationStatus.REJECTED)

fun StateAndRef<MembershipAttestation>.reject(state: StateAndRef<Membership>)
        : MembershipAttestation = amend(AttestationStatus.REJECTED, state)