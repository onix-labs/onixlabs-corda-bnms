package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStateFlow
import io.onixlabs.corda.identity.framework.workflow.MAXIMUM_PAGE_SPEC
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria

@StartableByRPC
@StartableByService
class FindRelationshipAttestationByLinearIdFlow(
    linearId: UniqueIdentifier,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<RelationshipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationByExternalIdFlow(
    externalId: String,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<RelationshipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationByHashFlow(
    hash: SecureHash,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<RelationshipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        expression = RelationshipAttestationSchema.RelationshipAttestationEntity::hash.equal(hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationByRelationshipFlow(
    Relationship: StateAndRef<Relationship>,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC
) : FindStateFlow<RelationshipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        status = Vault.StateStatus.ALL,
        relevancyStatus = relevancyStatus,
        expression = RelationshipAttestationSchema.RelationshipAttestationEntity::pointer.equal(Relationship.ref.toString())
    ), pageSpecification
)