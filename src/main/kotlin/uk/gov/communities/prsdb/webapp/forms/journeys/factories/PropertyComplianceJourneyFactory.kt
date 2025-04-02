package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class PropertyComplianceJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    fun create(
        propertyOwnershipId: Long,
        principalName: String,
    ) = PropertyComplianceJourney(
        validator,
        journeyDataServiceFactory.create(PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId)),
        principalName,
    )
}
