package uk.gov.communities.prsdb.webapp.journeys

import java.security.Principal

abstract class AbstractPropertyOwnershipUpdateJourneyState(
    private val journeyStateService: JourneyStateService,
    private val updateJourneyName: String,
) : AbstractJourneyState(journeyStateService) {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    fun discardIfLastModifiedDateChanged(
        seed: PropertyOwnershipUpdateJourneySeed,
        currentLastModifiedDate: String,
    ) {
        val journeyId = generateJourneyId(seed)
        val storedLastModifiedDate = journeyStateService.getStoredStringValueOrNull(journeyId, LAST_MODIFIED_DATE_KEY)
        if (storedLastModifiedDate != null && storedLastModifiedDate != currentLastModifiedDate) {
            journeyStateService.discardJourney(journeyId)
        }
    }

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipSeed = seed as? PropertyOwnershipUpdateJourneySeed
        val seedString =
            propertyOwnershipSeed?.let {
                val user = it.user
                if (user != null) {
                    generateSeedForPropertyOwnershipAndUser(it.ownershipId, user, updateJourneyName)
                } else {
                    generateSeedForPropertyOwnership(it.ownershipId, updateJourneyName)
                }
            }
        return super.generateJourneyId(seedString)
    }

    companion object {
        const val LAST_MODIFIED_DATE_KEY = "lastModifiedDate"

        fun generateSeedForPropertyOwnershipAndUser(
            ownershipId: Long,
            user: Principal,
            updateJourneyName: String,
        ): String = "Update $updateJourneyName for property $ownershipId by user ${user.name}"

        fun generateSeedForPropertyOwnership(
            ownershipId: Long,
            updateJourneyName: String,
        ): String = "Update $updateJourneyName for property $ownershipId"
    }
}

data class PropertyOwnershipUpdateJourneySeed(
    val ownershipId: Long,
    val user: Principal? = null,
)
