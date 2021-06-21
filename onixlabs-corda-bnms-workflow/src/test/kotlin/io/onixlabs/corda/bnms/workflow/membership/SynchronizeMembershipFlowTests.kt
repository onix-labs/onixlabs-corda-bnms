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

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.accept
import io.onixlabs.corda.bnms.workflow.FlowTest
import io.onixlabs.corda.bnms.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class SynchronizeMembershipFlowTests : FlowTest() {

    private lateinit var membership: StateAndRef<Membership>
    private lateinit var membershipAndAttestations: Pair<StateAndRef<Membership>, Set<StateAndRef<MembershipAttestation>>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                val membership = Membership(NETWORK, partyA)
                IssueMembershipFlow.Initiator(membership, observers = setOf(partyB))
            }
            .run(nodeB) {
                membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.accept(partyB)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(nodeC) {
                val membership = Membership(NETWORK, partyC)
                IssueMembershipFlow.Initiator(membership, observers = setOf(partyB))
            }
            .run(nodeB) {
                val membership = it.tx.outRefsOfType<Membership>().single()
                val attestation = membership.accept(partyB)
                IssueMembershipAttestationFlow.Initiator(attestation)
            }
            .run(nodeA) {
                SynchronizeMembershipFlow.Initiator(membership, partyC)
            }
            .finally { membershipAndAttestations = it ?: fail("Synchronization failed.") }
    }

    @Test
    fun `Party A has recorded Party C's Membership`() {
        Pipeline
            .create(network)
            .run(nodeA) {
                FindMembershipFlow(holder = partyC, network = NETWORK)
            }
            .finally { assert(it != null) }
    }

    @Test
    fun `Party A has recorded Party B's MembershipAttestation for Party C`() {
        Pipeline
            .create(network)
            .run(nodeA) {
                FindMembershipAttestationFlow(holder = partyC, network = NETWORK, attestor = partyB)
            }
            .finally { assert(it != null) }
    }

    @Test
    fun `Party C has recorded Party A's Membership`() {
        Pipeline
            .create(network)
            .run(nodeC) {
                FindMembershipFlow(holder = partyA, network = NETWORK)
            }
            .finally { assert(it != null) }
    }

    @Test
    fun `Party C has recorded Party B's MembershipAttestation for Party A`() {
        Pipeline
            .create(network)
            .run(nodeA) {
                FindMembershipAttestationFlow(holder = partyA, network = NETWORK, attestor = partyB)
            }
            .finally { assert(it != null) }
    }
}
