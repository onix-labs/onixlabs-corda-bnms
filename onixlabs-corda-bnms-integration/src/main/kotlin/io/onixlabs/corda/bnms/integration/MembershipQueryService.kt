package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.membership.FindLatestMembershipFlow
import io.onixlabs.corda.bnms.workflow.membership.FindVersionedMembershipFlow
import io.onixlabs.corda.identity.framework.workflow.MAXIMUM_PAGE_SPEC
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration

class MembershipQueryService(rpc: CordaRPCOps) : Service(rpc) {

    fun findLatestMembership(
        holder: AbstractParty,
        network: Network,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC,
        timeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlow(
            ::FindLatestMembershipFlow,
            holder,
            network,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(timeout)
    }

    fun findVersionedMembership(
        holder: AbstractParty,
        network: Network,
        previousStateRef: StateRef,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC,
        timeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlow(
            ::FindVersionedMembershipFlow,
            holder,
            network,
            previousStateRef,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(timeout)
    }
}