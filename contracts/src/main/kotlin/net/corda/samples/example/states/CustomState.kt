package net.corda.samples.example.states

import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.samples.example.contracts.CustomContract

@BelongsToContract(CustomContract::class)
data class CustomState(val amount: Int,
                       val participantA: Party,
                       val participantB: Party,
                       override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants: List<AbstractParty> get() = listOf(participantA, participantB)
}
