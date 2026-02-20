package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.FindPropertySearchResult
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.PropertySearchResultDataModel
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.AddressSearchState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FindPropertyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class FindPropertyStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<FindPropertySearchResult, FindPropertyFormModel, AddressSearchState>() {
    override val formModelClass = FindPropertyFormModel::class

    override fun getStepSpecificContent(state: AddressSearchState) = mapOf<String, Any?>()

    override fun chooseTemplate(state: AddressSearchState) = "findProperty"

    override fun mode(state: AddressSearchState): FindPropertySearchResult? {
        val results = state.searchResults ?: return null
        return if (results.isEmpty()) {
            FindPropertySearchResult.NO_RESULTS
        } else {
            FindPropertySearchResult.RESULTS_FOUND
        }
    }

    override fun afterStepDataIsAdded(state: AddressSearchState) {
        val formModel = getFormModelFromState(state)
        val searchTerm = "${formModel.houseNameOrNumber} ${formModel.postcode}"
        val results =
            propertyOwnershipService.searchForProperties(
                searchTerm = searchTerm,
                localCouncilBaseUserId = "",
                restrictToLocalCouncil = false,
            )
        state.searchResults =
            results.content.map { property ->
                PropertySearchResultDataModel(
                    id = property.id,
                    address = property.address,
                    registrationNumber = property.registrationNumber,
                    localCouncil = property.localCouncil,
                    landlordName = property.landlord.name,
                )
            }
    }
}

@JourneyFrameworkComponent
final class FindPropertyStep(
    stepConfig: FindPropertyStepConfig,
) : RequestableStep<FindPropertySearchResult, FindPropertyFormModel, AddressSearchState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "find-property"
    }
}
