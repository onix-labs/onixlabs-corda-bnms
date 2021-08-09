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

package io.onixlabs.corda.bnms.contract.settings

import io.onixlabs.corda.bnms.contract.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SettingExtensionTests {

    private val settings: Iterable<Setting<*>> = listOf(
        Setting("a", 123),
        Setting("b", 456),
        Setting("c", "Hello, World!"),
        Setting("d", true),
        Setting("e", false),
        StringSetting("f", "abc"),
        StringSetting("g", "xyz"),
        Role("Admin"),
        Role("Guest"),
        Permission("CAN_DO_THIS"),
        Permission("CAN_DO_THAT")
    )

    @Test
    fun `filterByType should filter all settings of type Setting where the value type is unknown`() {
        val result = settings.filterByType(Setting::class.java)
        assertEquals(5, result.size)
    }

    @Test
    fun `filterByType should filter all settings of type Setting where the value type is Integer`() {
        val result = settings.filterByType(Setting::class.java, Integer::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all settings of type Setting where the value type is String`() {
        val result = settings.filterByType(Setting::class.java, String::class.java)
        assertEquals(1, result.size)
    }

    @Test
    fun `filterByType should filter all settings of type Setting where the value type is Boolean`() {
        val result = settings.filterByType(Setting::class.java, java.lang.Boolean::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all settings of type StringSetting`() {
        val result = settings.filterByType(StringSetting::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all settings of type Role`() {
        val result = settings.filterByType(Role::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all settings of type Permission`() {
        val result = settings.filterByType(Permission::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type Setting where the value type is unknown`() {
        val result = settings.filterByType<Setting<*>>()
        assertEquals(5, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type Setting where the value type is Integer`() {
        val result = settings.filterByType<Setting<Int>>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type Setting where the value type is String`() {
        val result = settings.filterByType<Setting<String>>()
        assertEquals(1, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type Setting where the value type is Boolean`() {
        val result = settings.filterByType<Setting<Boolean>>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type StringSetting`() {
        val result = settings.filterByType<StringSetting>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type Role`() {
        val result = settings.filterByType<Role>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all settings of type Permission`() {
        val result = settings.filterByType<Permission>()
        assertEquals(2, result.size)
    }
}
