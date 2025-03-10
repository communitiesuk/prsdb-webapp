package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@Component
class PropertyDeregistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun create(propertyOwnershipId: Long) =
        PropertyDeregistrationJourney(
            validator,
            journeyDataService,
            propertyOwnershipService,
            propertyOwnershipId,
        )
}
