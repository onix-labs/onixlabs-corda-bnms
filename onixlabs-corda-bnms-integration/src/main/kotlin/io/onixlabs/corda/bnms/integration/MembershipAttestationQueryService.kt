package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
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

class MembershipAttestationQueryService(rpc: CordaRPCOps) : Service(rpc) {

    fun findMembershipAttestationByLinearId(
        linearId: UniqueIdentifier,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<MembershipAttestation>? {
        return rpc.startFlow(
            ::FindMembershipAttestationByLinearIdFlow,
            linearId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationByExternalId(
        externalId: String,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<MembershipAttestation>? {
        return rpc.startFlow(
            ::FindMembershipAttestationByExternalIdFlow,
            externalId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationByHash(
        hash: SecureHash,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<MembershipAttestation>? {
        return rpc.startFlow(
            ::FindMembershipAttestationByHashFlow,
            hash,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationByHolder(
        holder: AbstractParty,
        network: Network,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<MembershipAttestation>? {
        return rpc.startFlow(
            ::FindMembershipAttestationByHolderFlow,
            holder,
            network,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationByMembership(
        membership: StateAndRef<Membership>,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<MembershipAttestation>? {
        return rpc.startFlow(
            ::FindMembershipAttestationByMembershipFlow,
            membership,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByStatus(
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByStatusFlow,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByLinearId(
        linearId: UniqueIdentifier,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByLinearIdFlow,
            linearId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByExternalId(
        externalId: String,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByExternalIdFlow,
            externalId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByNetwork(
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByNetworkFlow,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByHolder(
        holder: AbstractParty,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByHolderFlow,
            holder,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByHolderAndNetwork(
        holder: AbstractParty,
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByHolderAndNetworkFlow,
            holder,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByAttestor(
        attestor: AbstractParty,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByAttestorFlow,
            attestor,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findMembershipAttestationsByAttestorAndNetwork(
        attestor: AbstractParty,
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<MembershipAttestation>> {
        return rpc.startFlow(
            ::FindMembershipAttestationsByAttestorAndNetworkFlow,
            attestor,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }
}