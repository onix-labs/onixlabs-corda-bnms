package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.workflow.membership.*
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration

class MembershipQueryService(rpc: CordaRPCOps) : Service(rpc) {

    fun findMembershipByLinearId(
        linearId: UniqueIdentifier,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlow(
            ::FindMembershipByLinearIdFlow,
            linearId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipByExternalId(
        externalId: String,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlow(
            ::FindMembershipByExternalIdFlow,
            externalId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipByHash(
        hash: SecureHash,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlow(
            ::FindMembershipByHashFlow,
            hash,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipByHolder(
        holder: AbstractParty,
        network: Network,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Membership>? {
        return rpc.startFlow(
            ::FindMembershipByHolderFlow,
            holder,
            network,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipsByStatus(
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Membership>> {
        return rpc.startFlow(
            ::FindMembershipsByStatusFlow,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipsByLinearId(
        linearId: UniqueIdentifier,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Membership>> {
        return rpc.startFlow(
            ::FindMembershipsByLinearIdFlow,
            linearId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipsByExternalId(
        externalId: String,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Membership>> {
        return rpc.startFlow(
            ::FindMembershipsByExternalIdFlow,
            externalId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipsByNetwork(
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Membership>> {
        return rpc.startFlow(
            ::FindMembershipsByNetworkFlow,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipsByHolder(
        holder: AbstractParty,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Membership>> {
        return rpc.startFlow(
            ::FindMembershipsByHolderFlow,
            holder,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }
}