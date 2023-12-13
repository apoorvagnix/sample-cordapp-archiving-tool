package net.corda.samples.example.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.samples.example.contracts.CustomContract
import net.corda.samples.example.states.CustomState

object CustomIssueFlow {

    @InitiatingFlow
    @StartableByRPC
    class IssueFlow(
        private val amount: Int,
        private val participantB: Party
    ) : FlowLogic<UniqueIdentifier>() {

        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call(): UniqueIdentifier {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val issueCommand =
                Command(CustomContract.Commands.Issue(), listOf(ourIdentity.owningKey, participantB.owningKey))

            val state = CustomState(amount, ourIdentity, participantB)

            val txBuilder = TransactionBuilder(notary)
                .addOutputState(state, CustomContract.ID)
                .addCommand(issueCommand)

            txBuilder.verify(serviceHub)

            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            val otherPartySession = initiateFlow(participantB)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, listOf(otherPartySession)))

            val finalizedTx = subFlow(FinalityFlow(fullySignedTx, listOf(otherPartySession)))

            // Extract the linearId from the transaction
            val outputState = finalizedTx.tx.outputsOfType<CustomState>().single()
            return outputState.linearId;
        }
    }

    @InitiatedBy(IssueFlow::class)
    class CustomIssueResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signedTransactionFlow = object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    // Add any additional checks here
                }
            }

            val txId = subFlow(signedTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
        }
    }
}