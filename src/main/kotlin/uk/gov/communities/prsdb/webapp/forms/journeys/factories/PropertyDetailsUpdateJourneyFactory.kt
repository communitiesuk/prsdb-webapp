package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class PropertyDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun create(propertyOwnershipId: Long) =
        PropertyDetailsUpdateJourney(
            validator,
            journeyDataServiceFactory.create(PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnershipId)),
            propertyOwnershipService,
            propertyOwnershipId,
        )
}
