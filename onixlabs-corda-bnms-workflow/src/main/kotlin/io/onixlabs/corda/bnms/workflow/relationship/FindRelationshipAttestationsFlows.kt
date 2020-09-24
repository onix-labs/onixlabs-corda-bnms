package io.onixlabs.corda.bnms.workflow.relationship

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStatesFlow
import io.onixlabs.corda.identity.framework.workflow.withExpressions
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
class FindRelationshipAttestationsByStatusFlow(
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<RelationshipAttestation>(
    VaultQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationsByLinearIdFlow(
    linearId: UniqueIdentifier,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<RelationshipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationsByExternalIdFlow(
    externalId: String,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<RelationshipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationsByNetworkFlow(
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<RelationshipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = RelationshipAttestationSchema.RelationshipAttestationEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationsByAttestorFlow(
    attestor: AbstractParty,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<RelationshipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = RelationshipAttestationSchema.RelationshipAttestationEntity::attestor.equal(attestor)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindRelationshipAttestationsByAttestorAndNetworkFlow(
    attestor: AbstractParty,
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<RelationshipAttestation>(
    VaultQueryCriteria(
        contractStateTypes = setOf(RelationshipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).withExpressions(
        RelationshipAttestationSchema.RelationshipAttestationEntity::attestor.equal(attestor),
        RelationshipAttestationSchema.RelationshipAttestationEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)