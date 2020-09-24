package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStateFlow
import io.onixlabs.corda.identity.framework.workflow.MAXIMUM_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.withExpressions
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.builder

@StartableByRPC
@StartableByService
class FindMembershipByLinearIdFlow(
    linearId: UniqueIdentifier,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<Membership>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipByExternalIdFlow(
    externalId: String,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<Membership>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipByHashFlow(
    hash: SecureHash,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<Membership>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.ALL,
        expression = MembershipSchema.MembershipEntity::hash.equal(hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipByHolderFlow(
    holder: AbstractParty,
    network: Network,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC
) : FindStateFlow<Membership>(builder {
    QueryCriteria.VaultQueryCriteria(
        contractStateTypes = setOf(Membership::class.java),
        status = Vault.StateStatus.UNCONSUMED,
        relevancyStatus = relevancyStatus
    ).withExpressions(
        MembershipSchema.MembershipEntity::holder.equal(holder),
        MembershipSchema.MembershipEntity::networkHash.equal(network.hash.toString())
    )
}, pageSpecification)