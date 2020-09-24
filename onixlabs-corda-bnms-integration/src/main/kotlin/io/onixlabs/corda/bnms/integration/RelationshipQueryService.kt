package io.onixlabs.corda.bnms.integration

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.workflow.relationship.*
import io.onixlabs.corda.identity.framework.workflow.DEFAULT_PAGE_SPEC
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration

class RelationshipQueryService(rpc: CordaRPCOps) : Service(rpc) {

    fun findRelationshipByLinearId(
        linearId: UniqueIdentifier,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Relationship>? {
        return rpc.startFlow(
            ::FindRelationshipByLinearIdFlow,
            linearId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipByExternalId(
        externalId: String,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Relationship>? {
        return rpc.startFlow(
            ::FindRelationshipByExternalIdFlow,
            externalId,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipByHash(
        hash: SecureHash,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<Relationship>? {
        return rpc.startFlow(
            ::FindRelationshipByHashFlow,
            hash,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipsByStatus(
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Relationship>> {
        return rpc.startFlow(
            ::FindRelationshipsByStatusFlow,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipsByLinearId(
        linearId: UniqueIdentifier,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Relationship>> {
        return rpc.startFlow(
            ::FindRelationshipsByLinearIdFlow,
            linearId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipsByExternalId(
        externalId: String,
        stateStatus: Vault.StateStatus = Vault.StateStatus.ALL,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Relationship>> {
        return rpc.startFlow(
            ::FindRelationshipsByExternalIdFlow,
            externalId,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }

    fun findRelationshipsByNetwork(
        network: Network,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPEC,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<Relationship>> {
        return rpc.startFlow(
            ::FindRelationshipsByNetworkFlow,
            network,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)
    }
}