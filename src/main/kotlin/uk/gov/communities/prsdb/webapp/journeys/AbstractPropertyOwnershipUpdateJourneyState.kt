package uk.gov.communities.prsdb.webapp.journeys

import java.security.Principal
import java.util.UUID

abstract class AbstractPropertyOwnershipUpdateJourneyState(
    private val journeyStateService: JourneyStateService,
    private val updateJourneyName: String,
) : AbstractJourneyState(journeyStateService) {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    fun discardIfLastModifiedDateChanged(
        seed: Any?,
        currentLastModifiedDate: String,
    ) {
        val journeyId = generateJourneyId(seed)
        val storedLastModifiedDate = journeyStateService.getStoredStringValueOrNull(journeyId, LAST_MODIFIED_DATE_KEY)
        if (storedLastModifiedDate != null && storedLastModifiedDate != currentLastModifiedDate) {
            journeyStateService.discardJourney(journeyId)
        }
    }

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
        const val LAST_MODIFIED_DATE_KEY = "lastModifiedDate"

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
