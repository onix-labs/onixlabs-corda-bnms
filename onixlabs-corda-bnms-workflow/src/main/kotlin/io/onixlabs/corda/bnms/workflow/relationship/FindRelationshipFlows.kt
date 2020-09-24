package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStateFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria

@StartableByRPC
@StartableByService
class FindRelationshipByLinearIdFlow(
    linearId: UniqueIdentifier,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<Relationship>(
    QueryCriteria.LinearStateQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipByExternalIdFlow(
    externalId: String,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<Relationship>(
    QueryCriteria.LinearStateQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipByHashFlow(
    hash: SecureHash,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<Relationship>(
    QueryCriteria.VaultCustomQueryCriteria(
        contractStateTypes = setOf(Relationship::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.ALL,
        expression = RelationshipSchema.RelationshipEntity::hash.equal(hash.toString())
    ), pageSpecification
)