package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationContract
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import io.onixlabs.corda.bnms.contract.relationship.Relationship
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestation
import io.onixlabs.corda.bnms.contract.relationship.RelationshipAttestationContract
import io.onixlabs.corda.bnms.contract.relationship.RelationshipContract
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.node.NotaryInfo
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.contracts.DummyContract
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TestLedgerDSLInterpreter
import net.corda.testing.dsl.TestTransactionDSLInterpreter
import net.corda.testing.node.MockServices
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

private typealias DSL = LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>

abstract class ContractTest : AutoCloseable {

    protected companion object {

        val cordapps = listOf(
            "io.onixlabs.corda.bnms.contract.membership",
            "io.onixlabs.corda.bnms.contract.relationship",
            "io.onixlabs.corda.bnms.contract.revocation",
            "net.corda.testing.contracts"
        )

        val contracts = listOf(
            MembershipContract.ID,
            MembershipAttestationContract.ID,
            RelationshipContract.ID,
            RelationshipAttestationContract.ID,
            RevocationLockContract.ID,
            DummyContract.PROGRAM_ID
        )

        fun partiesOf(vararg identities: TestIdentity) = identities.map { it.party }
        fun keysOf(vararg identities: TestIdentity) = identities.map { it.publicKey }
    }

    private lateinit var _services: MockServices
    protected val services: MockServices get() = _services

    override fun close() = finalize()
    protected open fun initialize() = Unit
    protected open fun finalize() = Unit

    @BeforeEach
    private fun setup() {
        val networkParameters = testNetworkParameters(
            minimumPlatformVersion = 4,
            notaries = listOf(NotaryInfo(TestIdentity(DUMMY_NOTARY_NAME, 20).party, true))
        )
        _services = MockServices(cordapps, IDENTITY_A, networkParameters, IDENTITY_B, IDENTITY_C)
        contracts.forEach { _services.addMockCordapp(it) }
        initialize()
    }

    @AfterEach
    private fun tearDown() = close()

    protected fun DSL.createDummyOutput(
        label: String = SecureHash.randomSHA256().toString()
    ): StateAndRef<DummyState> {
        transaction {
            output(DummyContract.PROGRAM_ID, label, DummyState(participants = partiesOf(IDENTITY_A)))
            command(keysOf(IDENTITY_A), DummyContract.Commands.Create())
            verifies()
        }

        return retrieveOutputStateAndRef(DummyState::class.java, label)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun DSL.initialize(
        membershipState: Membership,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<Membership>, Membership> {
        transaction {
            output(MembershipContract.ID, label, membershipState)
            command(listOf(membershipState.bearer.owningKey), MembershipContract.Issue)
            verifies()
        }

        val input = retrieveOutputStateAndRef(Membership::class.java, label)

        return Pair(input, input.getNextOutput())
    }

    @Suppress("UNCHECKED_CAST")
    protected fun DSL.initialize(
        relationshipState: Relationship,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<Relationship>, Relationship> {
        transaction {
            val keys = relationshipState.participants.map { it.owningKey }
            output(RelationshipContract.ID, label, relationshipState)
            relationshipState.participants.forEach {
                output(RevocationLockContract.ID, RevocationLock.create(it, relationshipState))
            }
            command(keys, RevocationLockContract.Create)
            command(keys, RelationshipContract.Issue)
            verifies()
        }

        val input = retrieveOutputStateAndRef(Relationship::class.java, label)

        return Pair(input, input.getNextOutput())
    }

    protected fun DSL.initialize(
        membershipState: Membership,
        attestor: AbstractParty,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<Membership>, MembershipAttestation> {
        transaction {
            output(MembershipContract.ID, label, membershipState)
            command(listOf(membershipState.bearer.owningKey), MembershipContract.Issue)
            verifies()
        }

        val membership = retrieveOutputStateAndRef(Membership::class.java, label)
        val attestation = MembershipAttestation.create(attestor, membership, AttestationStatus.ACCEPTED)
        return Pair(membership, attestation)
    }

    protected fun DSL.initialize(
        relationshipState: Relationship,
        attestor: AbstractParty,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<Relationship>, RelationshipAttestation> {
        transaction {
            output(RelationshipContract.ID, label, relationshipState)
            relationshipState.participants.forEach {
                output(RevocationLockContract.ID, RevocationLock.create(it, relationshipState))
            }
            val keys = relationshipState.participants.map { it.owningKey }
            command(keys, RevocationLockContract.Create)
            command(keys, RelationshipContract.Issue)
            verifies()
        }

        val relationship = retrieveOutputStateAndRef(Relationship::class.java, label)
        val attestation = RelationshipAttestation.create(attestor, relationship, AttestationStatus.ACCEPTED)
        return Pair(relationship, attestation)
    }
}