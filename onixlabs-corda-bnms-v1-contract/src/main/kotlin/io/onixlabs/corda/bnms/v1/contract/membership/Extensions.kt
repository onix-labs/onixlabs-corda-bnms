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

package io.onixlabs.corda.bnms.v1.contract.membership

import io.onixlabs.corda.identityframework.v1.contract.AttestationStatus
import io.onixlabs.corda.identityframework.v1.contract.toAttestationPointer
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

fun StateAndRef<Membership>.getNextOutput(): Membership {
    return state.data.copy(previousStateRef = ref)
}

fun StateAndRef<Membership>.attest(
    attestor: AbstractParty,
    status: AttestationStatus,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = MembershipAttestation(attestor, this, status, metadata, linearId)

fun StateAndRef<Membership>.accept(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = attest(attestor, AttestationStatus.ACCEPTED, metadata, linearId)

fun StateAndRef<Membership>.reject(
    attestor: AbstractParty,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier()
) = attest(attestor, AttestationStatus.REJECTED, metadata, linearId)

fun StateAndRef<MembershipAttestation>.amend(
    status: AttestationStatus,
    metadata: Map<String, String> = this.state.data.metadata
) = this.state.data.amend(ref, status, state.data.pointer, metadata)

fun StateAndRef<MembershipAttestation>.amend(
    membership: StateAndRef<Membership>,
    status: AttestationStatus,
    metadata: Map<String, String> = this.state.data.metadata
) = this.state.data.amend(ref, status, membership.toAttestationPointer(), metadata)

fun StateAndRef<MembershipAttestation>.accept(
    metadata: Map<String, String> = this.state.data.metadata
) = amend(AttestationStatus.ACCEPTED, metadata)

fun StateAndRef<MembershipAttestation>.accept(
    membership: StateAndRef<Membership>,
    metadata: Map<String, String> = this.state.data.metadata
) = amend(membership, AttestationStatus.ACCEPTED, metadata)

fun StateAndRef<MembershipAttestation>.reject(
    metadata: Map<String, String> = this.state.data.metadata
) = amend(AttestationStatus.REJECTED, metadata)

fun StateAndRef<MembershipAttestation>.reject(
    membership: StateAndRef<Membership>,
    metadata: Map<String, String> = this.state.data.metadata
) = amend(membership, AttestationStatus.REJECTED, metadata)
