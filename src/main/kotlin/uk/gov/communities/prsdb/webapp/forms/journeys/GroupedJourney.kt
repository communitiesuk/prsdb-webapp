package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.GroupedStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class GroupedJourney<T : GroupedStepId<*>>(
    journeyType: JourneyType,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<T>(
        journeyType = journeyType,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override fun isDestinationAllowedWhenChangingAnswerTo(
        destinationStep: T?,
        stepBeingChanged: T?,
    ): Boolean =
        destinationStep != null &&
            destinationStep.groupIdentifier == stepBeingChanged?.groupIdentifier &&
            isDestinationAfterOtherStep(destinationStep, stepBeingChanged)

    protected fun isDestinationAfterOtherStep(
        destinationStep: T?,
        otherStep: T?,
    ): Boolean =
        destinationStep != null &&
            fold(null) { destinationIsAfterOtherStep, stepDetails ->
                destinationIsAfterOtherStep
                    ?: when (stepDetails.step.id) {
                        destinationStep -> false
                        otherStep -> true
                        else -> destinationIsAfterOtherStep
                    }
            } ?: false
}
