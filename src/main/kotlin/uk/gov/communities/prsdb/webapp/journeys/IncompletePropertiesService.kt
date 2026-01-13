package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.datetime.toKotlinLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbService
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesForReminderDataModel
import java.time.LocalDate

@PrsdbService
class IncompletePropertiesService(
    private val landlordRepository: LandlordRepository,
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
) {
    fun addIncompletePropertyToLandlord(state: SavedJourneyState) {
        landlordRepository.findByBaseUser_Id(state.user.id)?.let { landlord ->
            val newEntry =
                LandlordIncompleteProperties(
                    landlord = landlord,
                    savedJourneyState = state,
                )
            landlordIncompletePropertiesRepository.save(newEntry)
        }
    }

    fun getIncompletePropertiesOlderThanDays(days: Int): List<IncompletePropertiesForReminderDataModel> {
        val incompleteProperties =
            landlordIncompletePropertiesRepository.findBySavedJourneyState_CreatedDateBefore(
                DateTimeHelper.getJavaInstantFromLocalDate(LocalDate.now().minusDays(days.toLong())),
            )

        return incompleteProperties.map { incompleteProperty ->
            IncompletePropertiesForReminderDataModel(
                incompleteProperty.landlord.email,
                incompleteProperty.savedJourneyState.getPropertyRegistrationSingleLineAddress(),
                // TODO PRSD-1030 - get real complete by date
                LocalDate.now().plusDays(7L).toKotlinLocalDate(),
            )
        }
    }
}
