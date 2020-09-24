package io.onixlabs.corda.bnms.workflow

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.Permission
import io.onixlabs.corda.bnms.contract.Role
import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipMember
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.identity.framework.contract.StaticClaimPointer
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
    protected val IDENTITY by lazy { emptySet<StaticClaimPointer<*>>() }
    protected val ROLES by lazy { setOf(Role.USER) }
    protected val PERMISSIONS by lazy { setOf(Permission("ACCESS_LEVEL", "SUPER_USER")) }
    protected val MEMBERS by lazy { setOf(RelationshipMember(partyA), RelationshipMember(partyB)) }
    protected val MEMBERSHIP by lazy { Membership(NETWORK, partyA, IDENTITY, ROLES, PERMISSIONS) }
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

    protected open fun initialize() = Unit
    protected open fun finalize() = Unit

    @BeforeAll
    private fun setup() {
        _network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("io.onixlabs.corda.identity.framework.contract"),
                    TestCordapp.findCordapp("io.onixlabs.corda.identity.framework.workflow"),
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

        _notaryParty = notaryNode.info.singleIdentity()
        _partyA = nodeA.info.singleIdentity()
        _partyB = nodeB.info.singleIdentity()
        _partyC = nodeC.info.singleIdentity()

        initialize()
    }

    @AfterAll
    private fun tearDown() {
        network.stopNodes()
        finalize()
    }
}