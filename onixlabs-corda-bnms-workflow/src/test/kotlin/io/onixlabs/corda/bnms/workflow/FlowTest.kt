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

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipMember
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.internal.vault.DummyLinearContract
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FlowTest {

    protected val NETWORK by lazy { Network("Example Network") }
    protected val MEMBERS by lazy { setOf(RelationshipMember(partyA), RelationshipMember(partyB)) }
    protected val MEMBERSHIP by lazy { Membership(NETWORK, partyA) }
    protected val RELATIONSHIP by lazy { Relationship(NETWORK, MEMBERS) }

    protected val REVOCATION_LOCK by lazy { RevocationLock(partyA, DummyLinearContract.State()) }

    private lateinit var _network: MockNetwork
    protected val network: MockNetwork get() = _network

    private lateinit var _notaryNode: StartedMockNode
    protected val notaryNode: StartedMockNode get() = _notaryNode
    private lateinit var _notaryParty: Party
    protected val notaryParty: Party get() = _notaryParty

    private lateinit var _nodeA: StartedMockNode
    protected val nodeA: StartedMockNode get() = _nodeA
    private lateinit var _partyA: Party
    protected val partyA: Party get() = _partyA

    private lateinit var _nodeB: StartedMockNode
    protected val nodeB: StartedMockNode get() = _nodeB
    private lateinit var _partyB: Party
    protected val partyB: Party get() = _partyB

    private lateinit var _nodeC: StartedMockNode
    protected val nodeC: StartedMockNode get() = _nodeC
    private lateinit var _partyC: Party
    protected val partyC: Party get() = _partyC

    private lateinit var _operatorNode: StartedMockNode
    protected val operatorNode: StartedMockNode get() = _operatorNode
    private lateinit var _operatorParty: Party
    protected val operatorParty: Party get() = _operatorParty

    protected open fun initialize() = Unit
    protected open fun finalize() = Unit

    @BeforeAll
    private fun setup() {
        _network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("io.onixlabs.corda.identityframework.contract"),
                    TestCordapp.findCordapp("io.onixlabs.corda.identityframework.workflow"),
                    TestCordapp.findCordapp("io.onixlabs.corda.bnms.contract"),
                    TestCordapp.findCordapp("io.onixlabs.corda.bnms.workflow")
                ),
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
            )
        )

        _notaryNode = network.defaultNotaryNode
        _nodeA = network.createPartyNode(CordaX500Name("PartyA", "London", "GB"))
        _nodeB = network.createPartyNode(CordaX500Name("PartyB", "New York", "US"))
        _nodeC = network.createPartyNode(CordaX500Name("PartyC", "Paris", "FR"))
        _operatorNode = network.createPartyNode(CordaX500Name("Network Operator", "London", "GB"))

        _notaryParty = notaryNode.info.singleIdentity()
        _partyA = nodeA.info.singleIdentity()
        _partyB = nodeB.info.singleIdentity()
        _partyC = nodeC.info.singleIdentity()
        _operatorParty = operatorNode.info.singleIdentity()

        initialize()
    }

    @AfterAll
    private fun tearDown() {
        network.stopNodes()
        finalize()
    }
}
