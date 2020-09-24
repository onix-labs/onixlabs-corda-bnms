package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStatesFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.*

@StartableByRPC
@StartableByService
class FindMembershipsByStatusFlow(
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Membership>(
    VaultQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipsByLinearIdFlow(
    linearId: UniqueIdentifier,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Membership>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipsByExternalIdFlow(
    externalId: String,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Membership>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipsByNetworkFlow(
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Membership>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = MembershipSchema.MembershipEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipsByHolderFlow(
    holder: AbstractParty,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Membership>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = MembershipSchema.MembershipEntity::holder.equal(holder)
    ), pageSpecification
)