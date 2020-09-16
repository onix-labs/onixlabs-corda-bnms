package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.identity.framework.workflow.FindStateFlow
import io.onixlabs.corda.identity.framework.workflow.MAXIMUM_PAGE_SPEC
import io.onixlabs.corda.identity.framework.workflow.withExpressions
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

@StartableByRPC
@StartableByService
class FindLatestMembershipAttestationByHolderFlow(
    holder: AbstractParty,
    network: Network,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAXIMUM_PAGE_SPEC
) : FindStateFlow<MembershipAttestation>(builder {
    VaultQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        status = Vault.StateStatus.UNCONSUMED,
        relevancyStatus = relevancyStatus
    ).withExpressions(
        MembershipAttestationEntity::attestee.equal(holder),
        MembershipAttestationEntity::networkHash.equal(network.hash)
    )
}, pageSpecification)