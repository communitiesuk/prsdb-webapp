package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertyForReminderDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData.Companion.createSavedJourneyState

class MockIncompletePropertiesData {
    companion object {
        fun createLandlordIncompleteProperties(
            landlord: Landlord = createLandlord(),
            savedJourneyState: SavedJourneyState = createSavedJourneyState(),
        ): LandlordIncompleteProperties =
            LandlordIncompleteProperties(
                landlord = landlord,
                savedJourneyState = savedJourneyState,
            )

        fun createIncompletePropertyForReminderDataModel(
            landlordEmail: String = "user.name@example.com",
            propertyAddress: String = "1 Test Street, EG1 2AB",
            completeByDate: LocalDate =
                java.time.LocalDate
                    .now()
                    .plusDays(28)
                    .toKotlinLocalDate(),
            savedJourneyStateId: Long = 123L,
        ) = IncompletePropertyForReminderDataModel(
            landlordEmail = landlordEmail,
            propertySingleLineAddress = propertyAddress,
            completeByDate = completeByDate,
            savedJourneyStateId = savedJourneyStateId,
        )
    }
}
