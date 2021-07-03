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

import io.onixlabs.corda.core.toClass
import io.onixlabs.corda.core.toTypedClass
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Represents the base class for obtaining setting type information implementations.
 *
 * @param T The underlying setting type.
 * @property settingType Obtains the most derived setting type in the type hierarchy.
 * @property valueType Obtains the value type of the setting, or null if the value type was specified as a wildcard.
 */
@PublishedApi
internal abstract class SettingTypeInfo<T : Setting<*>> : Comparable<SettingTypeInfo<T>> {

    val settingType: Class<T>
        get() = getSettingTypeInternal().toTypedClass()

    val valueType: Class<*>?
        get() = getValueTypeInternal()

    override fun compareTo(other: SettingTypeInfo<T>): Int = 0

    /**
     * Gets the most derived setting type in the type hierarchy.
     */
    private fun getSettingTypeInternal(): Type {
        val superClass = javaClass.genericSuperclass
        check(superClass !is Class<*>) { "Internal error: TypeReference constructed without actual type information" }
        return (superClass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * Gets the value type of the setting, or null if the value type was specified as a wildcard.
     *
     * The reason for returning a nullable [Class] is because settings could be specified as:
     * - Setting<*> - Settings of unknown value type, or settings regardless of value type.
     * - Setting<Any> - Settings specifically of [Any] value type.
     */
    private tailrec fun getValueTypeInternal(settingType: Type = getSettingTypeInternal()): Class<*>? {
        return if (settingType.toClass() == Setting::class.java) {
            val parameterizedType = settingType as ParameterizedType
            val argument = parameterizedType.actualTypeArguments[0]
            if (argument is WildcardType) null else argument.toClass()
        } else getValueTypeInternal(settingType.toClass().genericSuperclass)
    }
}
