package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class LandlordDeregistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    fun create() =
        LandlordDeregistrationJourney(
            validator,
            journeyDataServiceFactory.create(DEREGISTER_LANDLORD_JOURNEY_URL),
        )
}
