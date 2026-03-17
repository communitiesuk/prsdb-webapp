package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
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
    fun getIncompletePropertiesForLandlord(principalName: String): List<IncompletePropertiesDataModel> =
        landlordRepository.findByBaseUser_Id(principalName)?.let { landlord ->
            landlord.incompleteProperties
                .sortedBy { it.createdDate }
                .map { savedState ->
                    IncompletePropertiesDataModel(
                        journeyId = savedState.journeyId,
                        singleLineAddress = savedState.getPropertyRegistrationSingleLineAddress(),
                        completeByDate = CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(savedState),
                    )
                }
        } ?: throw IllegalArgumentException("Landlord not found for principal: $principalName")

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
