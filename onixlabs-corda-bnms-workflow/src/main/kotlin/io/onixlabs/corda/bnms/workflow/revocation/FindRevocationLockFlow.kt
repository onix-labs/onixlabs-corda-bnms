package io.onixlabs.corda.bnms.workflow.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.onixlabs.corda.bnms.workflow.FindStateFlow
import io.onixlabs.corda.bnms.workflow.MAX_PAGE_SPECIFICATION
import io.onixlabs.corda.claims.workflow.withExpressions
import net.corda.core.contracts.LinearState
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

@StartableByRPC
@StartableByService
class FindRevocationLockFlow<T : LinearState>(
    owner: AbstractParty,
    linearState: T,
    pageSpecification: PageSpecification = MAX_PAGE_SPECIFICATION
) : FindStateFlow<RevocationLock<T>>(builder {
    VaultQueryCriteria(
        contractStateTypes = setOf(RevocationLock::class.java),
        status = Vault.StateStatus.UNCONSUMED,
        relevancyStatus = Vault.RelevancyStatus.RELEVANT
    ).withExpressions(
        RevocationLockEntity::owner.equal(owner),
        RevocationLockEntity::linearId.equal(linearState.linearId.id),
        RevocationLockEntity::canonicalName.equal(linearState.javaClass.canonicalName)
    )
}, pageSpecification)