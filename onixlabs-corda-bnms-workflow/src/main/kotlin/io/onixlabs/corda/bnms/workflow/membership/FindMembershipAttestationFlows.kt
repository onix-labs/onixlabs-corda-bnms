package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.FindStateFlow
import io.onixlabs.corda.identity.framework.workflow.MAXIMUM_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.withExpressions
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.*

@StartableByRPC
@StartableByService
class FindMembershipAttestationByLinearIdFlow(
    linearId: UniqueIdentifier,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<MembershipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        linearId = listOf(linearId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationByExternalIdFlow(
    externalId: String,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<MembershipAttestation>(
    LinearStateQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        externalId = listOf(externalId)
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationByHashFlow(
    hash: SecureHash,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC
) : FindStateFlow<MembershipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        relevancyStatus = relevancyStatus,
        status = Vault.StateStatus.UNCONSUMED,
        expression = MembershipAttestationSchema.MembershipAttestationEntity::hash.equal(hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationByHolderFlow(
    holder: AbstractParty,
    network: Network,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC
) : FindStateFlow<MembershipAttestation>(
    VaultQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        status = Vault.StateStatus.UNCONSUMED,
        relevancyStatus = relevancyStatus
    ).withExpressions(
        MembershipAttestationSchema.MembershipAttestationEntity::attestee.equal(holder),
        MembershipAttestationSchema.MembershipAttestationEntity::networkHash.equal(network.hash.toString())
    ), pageSpecification
)

@StartableByRPC
@StartableByService
class FindMembershipAttestationByMembershipFlow(
    membership: StateAndRef<Membership>,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC
) : FindStateFlow<MembershipAttestation>(
    VaultCustomQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        status = Vault.StateStatus.ALL,
        relevancyStatus = relevancyStatus,
        expression = MembershipAttestationSchema.MembershipAttestationEntity::pointer.equal(membership.ref.toString())
    ), pageSpecification
)