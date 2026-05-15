package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.LandlordIncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationSingleLineAddress
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel

@PrsdbWebService
class IncompletePropertyForLandlordService(
    private val repository: SavedJourneyStateRepository,
    private val landlordRepository: LandlordRepository,
    private val landlordIncompletePropertiesRepository: LandlordIncompletePropertiesRepository,
) {
    fun getIncompletePropertiesForLandlord(
        principalName: String,
        requestedPageIndex: Int,
    ): Page<IncompletePropertiesDataModel> {
        val pageRequest =
            PageRequest.of(
                requestedPageIndex,
                MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE,
                Sort.by("savedJourneyState.createdDate"),
            )
        return landlordIncompletePropertiesRepository
            .findByLandlord_BaseUser_Id(principalName, pageRequest)
            .map { property ->
                IncompletePropertiesDataModel(
                    journeyId = property.savedJourneyState.journeyId,
                    singleLineAddress = property.savedJourneyState.getPropertyRegistrationSingleLineAddress(),
                    completeByDate =
                        CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(property.savedJourneyState),
                )
            }
    }

    fun deleteIncompleteProperty(
        journeyId: String,
        principalName: String,
    ) = repository.deleteByJourneyIdAndUser_Id(journeyId, principalName)

    fun getAddressData(
        incompletePropertyId: String,
        principalName: String,
    ): String =
        repository
            .findByJourneyIdAndUser_Id(incompletePropertyId, principalName)
            ?.getPropertyRegistrationSingleLineAddress()
            ?: throw IllegalArgumentException(
                "Incomplete property not found for id: $incompletePropertyId and principal: $principalName",
            )

    fun isIncompletePropertyAvailable(
        incompletePropertyId: String,
        principalName: String,
    ): Boolean = repository.existsByJourneyIdAndUser_Id(incompletePropertyId, principalName)

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
}
