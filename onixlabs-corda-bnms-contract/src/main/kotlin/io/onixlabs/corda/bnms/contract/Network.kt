/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.core.contract.Hashable
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

class Network(
    value: String,
    val operator: AbstractParty? = null
) : Setting<String>(NETWORK, value.toUpperCase()), Hashable {

    override val hash: SecureHash
        get() = SecureHash.sha256("$value$operator")
}
