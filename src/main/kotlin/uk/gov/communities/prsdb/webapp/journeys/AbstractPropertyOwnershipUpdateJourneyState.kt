package uk.gov.communities.prsdb.webapp.journeys

import java.security.Principal
import java.util.UUID

abstract class AbstractPropertyOwnershipUpdateJourneyState(
    journeyStateService: JourneyStateService,
    private val updateJourneyName: String,
) : AbstractJourneyState(journeyStateService) {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    override fun generateJourneyId(seed: Any?): String {
        val ownershipUserPair: Pair<Long, Principal>? = convertSeedToOwnershipUserPairOrNull(seed)
        val token: UUID? = convertSeedToTokenOrNull(seed)
        val seedString =
            when {
                ownershipUserPair != null ->
                    generateSeedForPropertyOwnershipAndUser(ownershipUserPair.first, ownershipUserPair.second, updateJourneyName)
                token != null -> generateSeedForToken(token, updateJourneyName)
                else -> null
            }
        return super.generateJourneyId(seedString)
    }

    private fun convertSeedToOwnershipUserPairOrNull(seed: Any?): Pair<Long, Principal>? =
        (seed as? Pair<*, *>)?.let {
            (it.first as? Long)?.let { ownershipId ->
                (it.second as? Principal)?.let { user ->
                    Pair(ownershipId, user)
                }
            }
        }

    private fun convertSeedToTokenOrNull(seed: Any?): UUID? = seed as? UUID

    companion object {
        fun generateSeedForPropertyOwnershipAndUser(
            ownershipId: Long,
            user: Principal,
            updateJourneyName: String,
        ): String = "Update $updateJourneyName for property $ownershipId by user ${user.name}"

        fun generateSeedForToken(
            token: UUID,
            updateJourneyName: String,
        ): String = "Update $updateJourneyName with token $token"
    }
}
