package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStatesFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria

@StartableByRPC
@StartableByService
class FindRelationshipsByStatusFlow(
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Relationship>(
    QueryCriteria.VaultQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipsByLinearIdFlow(
    linearId: UniqueIdentifier,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Relationship>(
    QueryCriteria.LinearStateQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipsByExternalIdFlow(
    externalId: String,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Relationship>(
    QueryCriteria.LinearStateQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipsByNetworkFlow(
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<Relationship>(
    QueryCriteria.VaultCustomQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = RelationshipSchema.RelationshipEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)