package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class UpdateJourney<T : StepId>(
    journeyType: JourneyType,
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<T>(journeyType, validator, journeyDataService) {
    abstract val updateStepId: StepId

    protected val originalDataKey = "ORIGINAL_$journeyType.name"

    override fun getUnreachableStepRedirect(journeyData: JourneyData) =
        if (journeyData[originalDataKey] == null) {
            updateStepId.urlPathSegment
        } else {
            last().step.id.urlPathSegment
        }

    override fun iterator(): Iterator<StepDetails<T>> {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val originalData = JourneyDataHelper.getPageData(journeyData, originalDataKey)

        // For any fields where the data is updated, replace the original value with the new value
        val updatedData =
            journeyData.keys
                .union(originalData?.keys ?: setOf())
                .map { key ->
                    key to if (journeyData.containsKey(key)) journeyData[key] else originalData?.get(key)
                }.associate { it }

        return ReachableStepDetailsIterator(updatedData, steps, initialStepId, validator)
    }
}
