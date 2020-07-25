package io.onixlabs.corda.bnms.workflow

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema.MembershipEntity
import io.onixlabs.corda.claims.workflow.withExpressions
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
class FindMembershipFlow(
    bearer: AbstractParty,
    network: Network,
    previousStateRef: StateRef? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAX_PAGE_SPECIFICATION
) : FindStateFlow<Membership>(builder {
    if (previousStateRef == null) {
        VaultQueryCriteria(
            contractStateTypes = setOf(Membership::class.java),
            status = stateStatus,
            relevancyStatus = relevancyStatus
        ).withExpressions(
            MembershipEntity::bearer.equal(bearer),
            MembershipEntity::networkHash.equal(network.hash)
        )
    } else {
        VaultQueryCriteria(
            contractStateTypes = setOf(Membership::class.java),
            status = stateStatus,
            relevancyStatus = relevancyStatus
        ).withExpressions(
            MembershipEntity::hash.equal(Membership.createMembershipHash(network, bearer, previousStateRef).toString())
        )
    }
}, pageSpecification)