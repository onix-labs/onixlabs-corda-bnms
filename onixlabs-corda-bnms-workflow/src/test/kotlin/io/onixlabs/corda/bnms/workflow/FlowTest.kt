package io.onixlabs.corda.bnms.workflow

import io.onixlabs.corda.bnms.contract.Network
import io.onixlabs.corda.bnms.contract.membership.Membership
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FlowTest : AutoCloseable {

    protected companion object {

        val cordapps = listOf(
            "io.onixlabs.corda.claims.contract",
            "io.onixlabs.corda.claims.workflow",
            "io.onixlabs.corda.bnms.contract.membership",
            "io.onixlabs.corda.bnms.contract.relationship",
            "io.onixlabs.corda.bnms.contract.revocation",
            "io.onixlabs.corda.bnms.workflow.membership",
            "io.onixlabs.corda.bnms.workflow.relationship",
            "io.onixlabs.corda.bnms.workflow.revocation"
        )

        val MEMBER_A = TestIdentity(CordaX500Name("Member A", "London", "GB"))
        val MEMBER_B = TestIdentity(CordaX500Name("Member B", "New York", "US"))
        val MEMBER_C = TestIdentity(CordaX500Name("Member C", "Paris", "FR"))
        val OPERATOR = TestIdentity(CordaX500Name("Operator", "London", "GB"))

        fun partiesOf(vararg identities: TestIdentity) = identities.map { it.party }
        fun keysOf(vararg identities: TestIdentity) = identities.map { it.publicKey }
    }

    protected val network: MockNetwork get() = _network

    protected val notaryNode: StartedMockNode get() = _notaryNode
    protected val memberNodeA: StartedMockNode get() = _memberNodeA
    protected val memberNodeB: StartedMockNode get() = _memberNodeB
    protected val memberNodeC: StartedMockNode get() = _memberNodeC
    protected val operatorNode: StartedMockNode get() = _operatorNode

    protected val notaryParty: Party get() = _notaryParty
    protected val memberPartyA: Party get() = _memberPartyA
    protected val memberPartyB: Party get() = _memberPartyB
    protected val memberPartyC: Party get() = _memberPartyC
    protected val operatorParty: Party get() = _operatorParty

    private lateinit var _network: MockNetwork

    private lateinit var _notaryNode: StartedMockNode
    private lateinit var _memberNodeA: StartedMockNode
    private lateinit var _memberNodeB: StartedMockNode
    private lateinit var _memberNodeC: StartedMockNode
    private lateinit var _operatorNode: StartedMockNode

    private lateinit var _notaryParty: Party
    private lateinit var _memberPartyA: Party
    private lateinit var _memberPartyB: Party
    private lateinit var _memberPartyC: Party
    private lateinit var _operatorParty: Party

    val centralizedNetwork by lazy { Network("Centralized Network", operatorParty) }
    val decentralizedNetwork by lazy { Network("Decentralized Network") }

    @BeforeAll
    private fun setup() {
        _network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = cordapps.map { TestCordapp.findCordapp(it) },
                networkParameters = testNetworkParameters(
                    minimumPlatformVersion = 5
                )
            )
        )

        _notaryNode = network.defaultNotaryNode
        _memberNodeA = network.createPartyNode(MEMBER_A.name)
        _memberNodeB = network.createPartyNode(MEMBER_B.name)
        _memberNodeC = network.createPartyNode(MEMBER_C.name)
        _operatorNode = network.createPartyNode(OPERATOR.name)

        _notaryParty = notaryNode.info.singleIdentity()
        _memberPartyA = memberNodeA.info.singleIdentity()
        _memberPartyB = memberNodeB.info.singleIdentity()
        _memberPartyC = memberNodeC.info.singleIdentity()
        _operatorParty = operatorNode.info.singleIdentity()

        initialize()
    }

    @AfterAll
    private fun tearDown() {
        network.stopNodes()
        finalize()
    }

    override fun close() = finalize()
    protected open fun initialize() = Unit
    protected open fun finalize() = Unit
}