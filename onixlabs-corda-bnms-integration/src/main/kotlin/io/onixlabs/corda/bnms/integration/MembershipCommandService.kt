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
import io.onixlabs.corda.bnms.contract.Permission
import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.membership.AmendMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.IssueMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.RevokeMembershipFlow
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.identityframework.contract.AbstractClaim
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

class MembershipCommandService(rpc: CordaRPCOps) : RPCService(rpc) {

    fun issueMembership(
        network: Network,
        holder: AbstractParty = ourIdentity,
        identity: Set<AbstractClaim<*>> = emptySet(),
        roles: Set<Role> = emptySet(),
        permissions: Set<Permission> = emptySet(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val membership = Membership(network, holder, identity, roles + permissions, linearId)
        return issueMembership(membership, notary, observers)
    }

    fun issueMembership(
        membership: Membership,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueMembershipFlow::Initiator,
            membership,
            notary,
            observers
        )
    }

    fun amendMembership(
        oldMembership: StateAndRef<Membership>,
        newMembership: Membership,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendMembershipFlow::Initiator,
            oldMembership,
            newMembership,
            observers
        )
    }

    fun revokeMembership(
        membership: StateAndRef<Membership>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeMembershipFlow::Initiator,
            membership,
            observers
        )
    }
}
