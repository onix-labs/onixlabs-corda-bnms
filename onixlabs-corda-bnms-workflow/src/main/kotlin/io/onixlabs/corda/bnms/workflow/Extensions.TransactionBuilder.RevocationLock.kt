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

package io.onixlabs.corda.bnms.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.TransactionBuilder

@Suspendable
fun TransactionBuilder.addLockedRevocationLock(
    revocationLock: RevocationLock<*>
): TransactionBuilder = apply {
    addOutputState(revocationLock, RevocationLockContract.ID)
    addCommand(RevocationLockContract.Lock, revocationLock.owner.owningKey)
}

@Suspendable
fun TransactionBuilder.addUnlockedRevocationLock(
    revocationLock: StateAndRef<RevocationLock<*>>
): TransactionBuilder = apply {
    addInputState(revocationLock)
    addCommand(RevocationLockContract.Unlock, revocationLock.state.data.owner.owningKey)
}