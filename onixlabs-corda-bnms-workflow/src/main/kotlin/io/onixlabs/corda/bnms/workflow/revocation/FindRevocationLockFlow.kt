/*
 * Copyright 2020-2021 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.workflow.revocation

import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.core.workflow.FindStateFlow
import io.onixlabs.corda.core.workflow.andWithExpressions
import net.corda.core.contracts.LinearState
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria

@StartableByRPC
@StartableByService
class FindRevocationLockFlow<T : LinearState>(owner: AbstractParty, state: T) : FindStateFlow<RevocationLock<T>>() {
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION

    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(contractStateType),
        relevancyStatus = Vault.RelevancyStatus.RELEVANT,
        status = Vault.StateStatus.UNCONSUMED
    ).andWithExpressions(
        RevocationLockEntity::owner.equal(owner),
        RevocationLockEntity::pointerStateLinearId.equal(state.linearId.id),
        RevocationLockEntity::pointerStateExternalId.equal(state.linearId.externalId),
        RevocationLockEntity::pointerStateClass.equal(state.javaClass.canonicalName)
    )
}
