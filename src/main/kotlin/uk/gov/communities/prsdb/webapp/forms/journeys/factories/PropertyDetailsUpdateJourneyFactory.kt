package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class PropertyDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun create(
        propertyOwnershipId: Long,
        stepName: String,
        isChangingAnswer: Boolean,
    ) = PropertyDetailsUpdateJourney(
        validator,
        journeyDataServiceFactory,
        propertyOwnershipService,
        propertyOwnershipId,
        stepName,
        isChangingAnswer,
    )

    companion object {
        fun getJourneyDataKey(
            propertyOwnershipId: Long,
            stepName: String,
        ): String {
            val step = UpdatePropertyDetailsStepId.fromPathSegment(stepName) ?: throwInvalidStepNameException(stepName)
            return getJourneyDataKey(propertyOwnershipId, step.groupIdentifier)
        }

        fun getJourneyDataKey(
            propertyOwnershipId: Long,
            stepGroupId: UpdatePropertyDetailsGroupIdentifier,
        ) = PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) + stepGroupId

        private fun throwInvalidStepNameException(stepName: String): Nothing =
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid PropertyDetailsUpdateJourney step name: $stepName")
    }
}
