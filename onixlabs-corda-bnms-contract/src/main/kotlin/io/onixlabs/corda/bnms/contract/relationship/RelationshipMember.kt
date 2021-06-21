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

package io.onixlabs.corda.bnms.contract.relationship

import io.onixlabs.corda.bnms.contract.Setting
import io.onixlabs.corda.bnms.contract.cast
import io.onixlabs.corda.identityframework.contract.AbstractClaim
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class RelationshipMember(
    val member: AbstractParty,
    val settings: Set<Setting<*>> = emptySet(),
    val claims: Set<AbstractClaim<*>> = emptySet()
) {
    inline fun <reified T : Setting<*>> getSetting(property: String): T? {
        return settings.filterIsInstance<T>().singleOrNull { it.property == property.toUpperCase() }
    }

    inline fun <reified T : Any> getSettingByValueType(property: String): Setting<T>? {
        return settings.singleOrNull { it.property == property.toUpperCase() }?.cast()
    }

    inline fun <reified T : AbstractClaim<*>> getClaim(property: String): T? {
        return claims.filterIsInstance<T>().singleOrNull { it.property == property }
    }
}