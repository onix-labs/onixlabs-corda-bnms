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

package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.identityframework.contract.Claim
import java.util.*

open class Setting<T : Any>(property: String, value: T) : Claim<T>(property.toUpperCase(), value) {

    internal companion object {
        const val NETWORK = "NETWORK"
        const val ROLE = "ROLE"
        const val PERMISSION = "PERMISSION"
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Setting<*>
                && other.javaClass == javaClass
                && property == other.property
                && value == other.value)
    }

    override fun hashCode(): Int {
        return Objects.hash(property, value)
    }
}