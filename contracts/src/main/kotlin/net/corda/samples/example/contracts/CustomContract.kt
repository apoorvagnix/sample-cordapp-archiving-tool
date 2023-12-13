package net.corda.samples.example.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class CustomContract : Contract {
    companion object {
        @JvmStatic
        val ID = "net.corda.samples.example.contracts.CustomContract"
    }

    interface Commands : CommandData {
        class Issue : Commands
        class Consume : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {
                // Add issuance contract rules
            }
            is Commands.Consume -> requireThat {
                // Add consumption contract rules
            }
        }
    }
}
