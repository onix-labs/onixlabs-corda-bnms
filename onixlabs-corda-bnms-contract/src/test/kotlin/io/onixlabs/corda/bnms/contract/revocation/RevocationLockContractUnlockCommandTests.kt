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

package io.onixlabs.corda.bnms.contract.revocation

import io.onixlabs.corda.bnms.contract.ContractTest
import io.onixlabs.corda.bnms.contract.IDENTITY_A
import io.onixlabs.corda.bnms.contract.IDENTITY_B
import io.onixlabs.corda.bnms.contract.REVOCATION_LOCK
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RevocationLockContractUnlockCommandTests : ContractTest() {

    @Test
    fun `On revocation lock unlocking, the transaction must include the Unlock command`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                fails()
                command(keysOf(IDENTITY_A), RevocationLockContract.Unlock)
                verifies()
            }
        }
    }

    @Test
    fun `On revocation lock unlocking, only one revocation lock state must be consumed`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Unlock)
                failsWith(RevocationLockContract.Unlock.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock unlocking, zero revocation lock states must be created`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                output(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_A), RevocationLockContract.Unlock)
                failsWith(RevocationLockContract.Unlock.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On revocation lock unlocking, the owner of the revocation lock state must sign the transaction`() {
        services.ledger {
            transaction {
                input(RevocationLockContract.ID, REVOCATION_LOCK)
                command(keysOf(IDENTITY_B), RevocationLockContract.Unlock)
                failsWith(RevocationLockContract.Unlock.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
