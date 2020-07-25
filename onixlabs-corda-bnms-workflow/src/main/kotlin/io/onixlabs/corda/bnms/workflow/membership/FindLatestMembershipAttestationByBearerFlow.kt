package io.onixlabs.corda.bnms.workflow.membership

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.onixlabs.corda.bnms.workflow.FindStateFlow
import io.onixlabs.corda.bnms.workflow.MAX_PAGE_SPECIFICATION
import io.onixlabs.corda.claims.workflow.withExpressions
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

@StartableByRPC
@StartableByService
class FindLatestMembershipAttestationByBearerFlow(
    bearer: AbstractParty,
    network: Network,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    pageSpecification: PageSpecification = MAX_PAGE_SPECIFICATION
) : FindStateFlow<MembershipAttestation>(builder {
    VaultQueryCriteria(
        contractStateTypes = setOf(MembershipAttestation::class.java),
        status = Vault.StateStatus.UNCONSUMED,
        relevancyStatus = relevancyStatus
    ).withExpressions(
        MembershipAttestationEntity::attestee.equal(bearer),
        MembershipAttestationEntity::networkHash.equal(network.hash)
    )
}, pageSpecification)