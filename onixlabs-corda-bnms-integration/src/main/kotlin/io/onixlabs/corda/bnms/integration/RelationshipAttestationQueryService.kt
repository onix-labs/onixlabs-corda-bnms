package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.workflow.relationship.*
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

class RelationshipAttestationQueryService(rpc: CordaRPCOps) : Service(rpc) {

    fun findRelationshipAttestationByLinearId(
        linearId: UniqueIdentifier,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<RelationshipAttestation>? {
        return rpc.startFlow(
            ::FindRelationshipAttestationByLinearIdFlow,
            linearId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationByExternalId(
        externalId: String,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<RelationshipAttestation>? {
        return rpc.startFlow(
            ::FindRelationshipAttestationByExternalIdFlow,
            externalId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationByHash(
        hash: SecureHash,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<RelationshipAttestation>? {
        return rpc.startFlow(
            ::FindRelationshipAttestationByHashFlow,
            hash,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationByRelationship(
        relationship: StateAndRef<Relationship>,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<RelationshipAttestation>? {
        return rpc.startFlow(
            ::FindRelationshipAttestationByRelationshipFlow,
            relationship,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationsByStatus(
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<RelationshipAttestation>> {
        return rpc.startFlow(
            ::FindRelationshipAttestationsByStatusFlow,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationsByLinearId(
        linearId: UniqueIdentifier,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<RelationshipAttestation>> {
        return rpc.startFlow(
            ::FindRelationshipAttestationsByLinearIdFlow,
            linearId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationsByExternalId(
        externalId: String,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<RelationshipAttestation>> {
        return rpc.startFlow(
            ::FindRelationshipAttestationsByExternalIdFlow,
            externalId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationsByNetwork(
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<RelationshipAttestation>> {
        return rpc.startFlow(
            ::FindRelationshipAttestationsByNetworkFlow,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationsByAttestor(
        attestor: AbstractParty,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<RelationshipAttestation>> {
        return rpc.startFlow(
            ::FindRelationshipAttestationsByAttestorFlow,
            attestor,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipAttestationsByAttestorAndNetwork(
        attestor: AbstractParty,
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<RelationshipAttestation>> {
        return rpc.startFlow(
            ::FindRelationshipAttestationsByAttestorAndNetworkFlow,
            attestor,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }
}