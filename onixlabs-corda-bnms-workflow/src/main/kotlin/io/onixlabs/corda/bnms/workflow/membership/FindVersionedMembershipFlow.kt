package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.identity.framework.workflow.FindStateFlow
import io.onixlabs.corda.identity.framework.workflow.MAXIMUM_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.withExpressions
import net.corda.core.contracts.StateRef
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

@StartableByRPC
@StartableByService
class FindVersionedMembershipFlow(
    bearer: AbstractParty,
    network: Network,
    previousStateRef: StateRef,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC
) : FindStateFlow<Membership>(builder {
    VaultQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        status = Vault.StateStatus.ALL,
        relevancyStatus = relevancyStatus
    ).withExpressions(
        MembershipEntity::hash.equal(Membership.createMembershipHash(network, bearer, previousStateRef).toString())
    )
}, pageSpecification)