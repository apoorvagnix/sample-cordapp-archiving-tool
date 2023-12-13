package net.corda.samples.example.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.samples.example.contracts.CustomContract
import net.corda.samples.example.states.CustomState
import java.util.*

object CustomConsumeFlow {

    @InitiatingFlow
    @StartableByRPC
    class ConsumeFlow(private val stateLinearId: UniqueIdentifier) : FlowLogic<SignedTransaction>() {

        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(stateLinearId))
            val stateAndRef = serviceHub.vaultService.queryBy<CustomState>(queryCriteria).states.singleOrNull()
                ?: throw FlowException("State with id $stateLinearId not found.")

            val inputState = stateAndRef.state.data

            val notary = stateAndRef.state.notary
            val consumeCommand =
                Command(CustomContract.Commands.Consume(), inputState.participants.map { it.owningKey })

            val txBuilder = TransactionBuilder(notary)
                .addInputState(stateAndRef)
                .addCommand(consumeCommand)

            txBuilder.verify(serviceHub)

            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            val sessions = (inputState.participants - ourIdentity).map { initiateFlow(it as Party) }
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))

            return subFlow(FinalityFlow(fullySignedTx, sessions))
        }
    }

    @InitiatedBy(ConsumeFlow::class)
    class CustomConsumeResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    // Additional transaction checks can be added here
                }
            }

            val txId = subFlow(signedTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
        }
    }
}
