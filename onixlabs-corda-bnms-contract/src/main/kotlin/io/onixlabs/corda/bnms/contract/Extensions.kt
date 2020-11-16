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

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty

val Iterable<AbstractParty>.identityHash: SecureHash
    get() = SecureHash.sha256(toSortedSet(IdentityComparator).joinToString())

inline fun <T, K> Iterable<T>.isDistinctBy(selector: (T) -> K): Boolean {
    return count() == distinctBy(selector).count()
}

inline fun <reified T : Any> Setting<*>.cast(): Setting<T> {
    return Setting(property, T::class.java.cast(value))
}

private object IdentityComparator : Comparator<AbstractParty> {
    override fun compare(p0: AbstractParty?, p1: AbstractParty?): Int {
        return (p0?.hashCode() ?: 0).compareTo(p1?.hashCode() ?: 0)
    }
}
