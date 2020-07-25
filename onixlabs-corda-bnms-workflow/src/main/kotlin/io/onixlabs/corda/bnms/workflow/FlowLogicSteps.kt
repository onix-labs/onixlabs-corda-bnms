package io.onixlabs.corda.bnms.workflow

import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.utilities.ProgressTracker.Step

internal object INITIALIZING : Step("Initializing flow.")

internal object GENERATING : Step("Generating transaction.")

internal object VERIFYING : Step("Verifying transaction.")

internal object SIGNING : Step("Signing transaction.")

internal object COUNTERSIGNING : Step("Gathering counter-party signatures.") {
    override fun childProgressTracker() = CollectSignaturesFlow.tracker()
}

internal object FINALIZING : Step("Finalizing and recording signed transaction.") {
    override fun childProgressTracker() = FinalityFlow.tracker()
}