package io.onixlabs.corda.bnms.contract

import io.onixlabs.corda.bnms.contract.membership.Membership
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestation
import io.onixlabs.corda.bnms.contract.membership.MembershipAttestationContract
import io.onixlabs.corda.bnms.contract.membership.MembershipContract
import io.onixlabs.corda.bnms.contract.relationship.*
import io.onixlabs.corda.bnms.contract.revocation.RevocationLock
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockContract
import io.onixlabs.corda.bnms.contract.revocation.RevocationLockPointer
import io.onixlabs.corda.identity.framework.contract.EvolvableAttestationContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NotaryInfo
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TestLedgerDSLInterpreter
import net.corda.testing.dsl.TestTransactionDSLInterpreter
import net.corda.testing.node.MockServices
import org.junit.jupiter.api.BeforeEach

abstract class ContractTest {

    companion object {
        val IDENTITY_A = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
        val IDENTITY_B = TestIdentity(CordaX500Name("PartyB", "New York", "US"))
        val IDENTITY_C = TestIdentity(CordaX500Name("PartyC", "Paris", "FR"))
        val OPERATOR_A = TestIdentity(CordaX500Name("Operator", "London", "GB"))

        val CENTRALIZED_NETWORK = Network("Centralized Network", OPERATOR_A.party)
        val DECENTRALIZED_NETWORK = Network("Decentralized Network")
        val INVALID_NETWORK = Network("Invalid Network")

        val CENTRALIZED_MEMBERSHIP_OPERATOR = Membership(
            network = CENTRALIZED_NETWORK,
            holder = OPERATOR_A.party,
            roles = setOf(Role.NETWORK_OPERATOR)
        )

        val CENTRALIZED_MEMBERSHIP_A = Membership(
            network = CENTRALIZED_NETWORK,
            holder = IDENTITY_A.party
        )

        val CENTRALIZED_MEMBERSHIP_B = Membership(
            network = CENTRALIZED_NETWORK,
            holder = IDENTITY_B.party
        )

        val CENTRALIZED_MEMBERSHIP_C = Membership(
            network = CENTRALIZED_NETWORK,
            holder = IDENTITY_C.party
        )

        val DECENTRALIZED_MEMBERSHIP_A = Membership(
            network = DECENTRALIZED_NETWORK,
            holder = IDENTITY_A.party
        )

        val DECENTRALIZED_MEMBERSHIP_B = Membership(
            network = DECENTRALIZED_NETWORK,
            holder = IDENTITY_B.party
        )

        val DECENTRALIZED_MEMBERSHIP_C = Membership(
            network = DECENTRALIZED_NETWORK,
            holder = IDENTITY_C.party
        )

        val RELATIONSHIP = Relationship(
            network = CENTRALIZED_NETWORK,
            members = setOf(
                RelationshipMember(IDENTITY_A.party),
                RelationshipMember(IDENTITY_B.party)
            )
        )

        val INVALID_RELATIONSHIP = Relationship(
            network = INVALID_NETWORK,
            members = setOf(
                RelationshipMember(IDENTITY_A.party),
                RelationshipMember(IDENTITY_B.party)
            )
        )

        val REVOCATION_LOCK = RevocationLock(
            IDENTITY_A.party,
            RevocationLockPointer(UniqueIdentifier(), Relationship::class.java)
        )

        val EMPTY_REF = StateRef(SecureHash.zeroHash, 0)

        private val cordapps = listOf(
            "io.onixlabs.corda.identity.framework.contract",
            "io.onixlabs.corda.bnms.contract"
        )

        private val contracts = listOf(
            MembershipContract.ID,
            RelationshipContract.ID,
            RevocationLockContract.ID,
            MembershipAttestationContract.ID,
            RelationshipAttestationContract.ID
        )

        fun keysOf(vararg identities: TestIdentity) = identities.map { it.publicKey }
    }

    private lateinit var _services: MockServices
    protected val services: MockServices get() = _services

    @BeforeEach
    private fun setup() {
        val networkParameters = testNetworkParameters(
            minimumPlatformVersion = 5,
            notaries = listOf(NotaryInfo(TestIdentity(DUMMY_NOTARY_NAME, 20).party, true))
        )
        _services = MockServices(cordapps, IDENTITY_A, networkParameters, IDENTITY_B, IDENTITY_C)
        contracts.forEach { _services.addMockCordapp(it) }
    }

    protected fun StateAndRef<Membership>.withWrongAttestee(
        attestor: AbstractParty,
        holder: AbstractParty
    ): MembershipAttestation {
        val stateAndRef = copy(state = state.copy(data = state.data.copy(holder = holder)))
        return MembershipAttestation(attestor, stateAndRef)
    }

    protected fun StateAndRef<Membership>.withWrongAttestee(
        attestation: StateAndRef<MembershipAttestation>,
        holder: AbstractParty
    ): MembershipAttestation {
        val stateAndRef = copy(state = state.copy(data = state.data.copy(holder = holder)))
        val attestor = attestation.state.data.attestor
        val linearId = attestation.state.data.linearId
        val ref = attestation.ref
        return MembershipAttestation(
            attestor,
            stateAndRef,
            linearId = linearId,
            previousStateRef = ref
        )
    }

    protected fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        membership: Membership
    ): StateAndRef<Membership> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(MembershipContract.ID, label, membership)
            command(listOf(membership.holder.owningKey), MembershipContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(Membership::class.java, label)
    }

    protected fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        attestation: MembershipAttestation,
        membership: StateAndRef<Membership>
    ): StateAndRef<MembershipAttestation> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(MembershipAttestationContract.ID, label, attestation)
            reference(membership.ref)
            command(listOf(attestation.attestor.owningKey), EvolvableAttestationContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(MembershipAttestation::class.java, label)
    }

    protected fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        relationship: Relationship
    ): StateAndRef<Relationship> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(RelationshipContract.ID, label, relationship)
            relationship.createRevocationLocks().forEach { output(RevocationLockContract.ID, it) }
            command(relationship.participants.map { it.owningKey }, RelationshipContract.Issue)
            command(relationship.participants.map { it.owningKey }, RevocationLockContract.Create)
            verifies()
        }

        return retrieveOutputStateAndRef(Relationship::class.java, label)
    }

    protected fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        attestation: RelationshipAttestation,
        relationship: StateAndRef<Relationship>
    ): StateAndRef<RelationshipAttestation> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(RelationshipAttestationContract.ID, label, attestation)
            reference(relationship.ref)
            command(attestation.participants.map { it.owningKey }, EvolvableAttestationContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(RelationshipAttestation::class.java, label)
    }
}