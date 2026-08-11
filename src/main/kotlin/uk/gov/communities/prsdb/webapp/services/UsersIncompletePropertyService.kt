package uk.gov.communities.prsdb.webapp.services

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.IncompletePropertiesRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationSingleLineAddress
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompletePropertiesDataModel

@PrsdbWebService
class UsersIncompletePropertyService(
    private val repository: SavedJourneyStateRepository,
    private val incompletePropertiesRepository: IncompletePropertiesRepository,
) {
    fun getCurrentUsersIncompleteProperties(requestedPageIndex: Int): Page<IncompletePropertiesDataModel> {
        val principalName = SecurityContextHolder.getContext().authentication.name
        val pageRequest =
            PageRequest.of(
                requestedPageIndex,
                MAX_ENTRIES_IN_INCOMPLETE_PROPERTIES_PAGE,
                Sort.by("savedJourneyState.createdDate"),
            )
        return incompletePropertiesRepository
            .findByUser_Id(principalName, pageRequest)
            .map { property ->
                IncompletePropertiesDataModel(
                    journeyId = property.savedJourneyState.journeyId,
                    singleLineAddress = property.savedJourneyState.getPropertyRegistrationSingleLineAddress(),
                    completeByDate =
                        CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(property.savedJourneyState),
                )
            }
    }

    fun getCurrentUsersIncompletePropertiesCount(): Int =
        incompletePropertiesRepository.countByUser_Id(SecurityContextHolder.getContext().authentication.name).toInt()

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

    fun addIncompletePropertyForUser(state: SavedJourneyState) {
        val newEntry =
            LandlordIncompleteProperties(
                user = state.user,
                savedJourneyState = state,
            )
        incompletePropertiesRepository.save(newEntry)
    }
}
