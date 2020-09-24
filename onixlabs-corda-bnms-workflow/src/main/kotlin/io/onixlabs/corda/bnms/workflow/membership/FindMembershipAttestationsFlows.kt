package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema
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
class FindMembershipAttestationsByStatusFlow(
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    VaultQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByLinearIdFlow(
    linearId: UniqueIdentifier,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByExternalIdFlow(
    externalId: String,
    stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByNetworkFlow(
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = MembershipAttestationSchema.MembershipAttestationEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByHolderFlow(
    holder: AbstractParty,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = MembershipAttestationSchema.MembershipAttestationEntity::attestee.equal(holder)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByHolderAndNetworkFlow(
    holder: AbstractParty,
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    VaultQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).withExpressions(
        MembershipAttestationSchema.MembershipAttestationEntity::attestee.equal(holder),
        MembershipAttestationSchema.MembershipAttestationEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByAttestorFlow(
    attestor: AbstractParty,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus,
        expression = MembershipAttestationSchema.MembershipAttestationEntity::attestor.equal(attestor)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationsByAttestorAndNetworkFlow(
    attestor: AbstractParty,
    network: Network,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStatesFlow<MembershipAttestation>(
    VaultQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).withExpressions(
        MembershipAttestationSchema.MembershipAttestationEntity::attestor.equal(attestor),
        MembershipAttestationSchema.MembershipAttestationEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)