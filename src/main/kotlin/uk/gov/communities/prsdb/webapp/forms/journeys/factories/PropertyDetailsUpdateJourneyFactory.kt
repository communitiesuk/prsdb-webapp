package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class PropertyDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun create(
        propertyOwnershipId: Long,
        stepName: String,
    ) = PropertyDetailsUpdateJourney(
        validator,
        journeyDataServiceFactory.create(getJourneyDataKey(propertyOwnershipId, stepName)),
        propertyOwnershipService,
        propertyOwnershipId,
        stepName,
    )

    private fun getJourneyDataKey(
        propertyOwnershipId: Long,
        stepName: String,
    ): String {
        val step = UpdatePropertyDetailsStepId.fromPathSegment(stepName) ?: throwInvalidStepNameException(stepName)
        return PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId) + step.groupIdentifier.identifierString
    }

    private fun throwInvalidStepNameException(stepName: String): Nothing =
        throw PrsdbWebException("Invalid PropertyDetailsUpdateJourney step name: $stepName")
}
