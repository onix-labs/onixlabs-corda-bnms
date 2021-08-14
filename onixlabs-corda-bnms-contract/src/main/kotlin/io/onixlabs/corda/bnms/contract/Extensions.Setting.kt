///*
// * Copyright 2020-2021 ONIXLabs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.onixlabs.corda.bnms.contract
//
///**
// * Only declared so that I can fit method signatures on a single line.
// */
//private typealias Settings = Iterable<Setting<*>>
//
///**
// * Casts a [Setting] of an unknown value type to a [Setting] of type [T].
// *
// * @param T The underlying value type of the cast [Setting].
// * @param type The value type to cast to.
// * @return Returns a [Setting] of type [T].
// * @throws ClassCastException if the value type cannot be cast to type [T].
// */
//fun <T : Any> Setting<*>.cast(type: Class<T>): Setting<T> {
//    return Setting(property, type.cast(value))
//}
//
///**
// * Casts a [Setting] of an unknown value type to a [Setting] of type [T].
// *
// * @param T The underlying value type of the cast [Setting].
// * @return Returns a [Setting] of type [T].
// * @throws ClassCastException if the value type cannot be cast to type [T].
// */
//inline fun <reified T : Any> Setting<*>.cast(): Setting<T> {
//    return cast(T::class.java)
//}
//
///**
// * Casts an [Iterable] of [Setting] of an unknown value type to an [Iterable] of [Setting] of type [T].
// *
// * @param T The underlying value type of the cast [Setting].
// * @param type The value type to cast to.
// * @return Returns an [Iterable] of [Setting] of type [T].
// * @throws ClassCastException if the value type cannot be cast to type [T].
// */
//fun <T : Any> Settings.cast(type: Class<T>): List<Setting<T>> {
//    return map { it.cast(type) }
//}
//
///**
// * Casts an [Iterable] of [Setting] of an unknown value type to an [Iterable] of [Setting] of type [T].
// *
// * @param T The underlying value type of the cast [Setting].
// * @return Returns an [Iterable] of [Setting] of type [T].
// * @throws ClassCastException if the value type cannot be cast to type [T].
// */
//inline fun <reified T : Any> Settings.cast(): List<Setting<T>> {
//    return cast(T::class.java)
//}
//
///**
// * Filters an [Iterable] of [Setting] by the specified setting type, and optionally the setting value type.
// *
// * @param V The underlying value type.
// * @param S The underlying setting type.
// * @param settingType The setting type.
// * @param valueType The value type, or null if the setting type is derived, or the value type is unknown or a wildcard.
// * @return Returns an [Iterable] of [Setting] by the specified setting type, and optionally the setting value type.
// */
//fun <V, S : Setting<in V>> Settings.filterByType(settingType: Class<S>, valueType: Class<in V>? = null): List<S> {
//    val settingsFilteredBySettingType = filter { it.javaClass == settingType }.filterIsInstance(settingType)
//    return if (valueType != null) settingsFilteredBySettingType.filter { it.value?.javaClass == valueType }
//    else settingsFilteredBySettingType
//}
//
///**
// * Filters an [Iterable] of [Setting] by the specified setting type, and optionally the setting value type.
// *
// * @param T The underlying setting type.
// * @return Returns an [Iterable] of [Setting] by the specified setting type, and optionally the setting value type.
// */
//inline fun <reified T : Setting<*>> Iterable<Setting<*>>.filterByType(): List<T> {
//    val typeInfo = object : SettingTypeInfo<T>() {}
//    return filterByType(typeInfo.settingType, typeInfo.valueType)
//}
//
///**
// * Filters an [Iterable] of [Setting] by the specified property.
// *
// * @param T The underlying setting type.
// * @param property The property to filter by.
// * @param ignoreCase Determines whether to ignore the property case when filtering; for example when filtering by a normalized property.
// * @return Returns an [Iterable] of [Setting] by the specified property.
// */
//fun <T : Setting<*>> Iterable<T>.filterByProperty(property: String, ignoreCase: Boolean = false): List<T> {
//    return filter { it.property.equals(property, ignoreCase) }
//}
