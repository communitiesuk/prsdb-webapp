package uk.gov.communities.prsdb.webapp.journeys

import java.security.Principal

abstract class AbstractUpdateJourneyState(
    private val journeyStateService: JourneyStateService,
    private val delegateProvider: JourneyStateDelegateProvider,
    private val updateJourneyName: String,
) : AbstractJourneyState(journeyStateService) {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    override fun generateJourneyId(seed: Any?): String {
        val ownershipUserPair: Pair<Long, Principal>? = convertSeedToOwnershipUserPairOrNull(seed)

        return super.generateJourneyId(
            ownershipUserPair?.let {
                generateSeedForPropertyOwnershipAndUser(it.first, it.second, updateJourneyName)
            },
        )
    }

    private fun convertSeedToOwnershipUserPairOrNull(seed: Any?): Pair<Long, Principal>? =
        (seed as? Pair<*, *>)?.let {
            (it.first as? Long)?.let { ownershipId ->
                (it.second as? Principal)?.let { user ->
                    Pair(ownershipId, user)
                }
            }
        }

    companion object {
        fun generateSeedForPropertyOwnershipAndUser(
            ownershipId: Long,
            user: Principal,
            updateJourneyName: String,
        ): String = "Update $updateJourneyName for property $ownershipId by user ${user.name}"
    }
}
