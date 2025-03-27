package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class LandlordDeregistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val securityContextService: SecurityContextService,
) {
    fun create(landlordOneLoginId: String) =
        LandlordDeregistrationJourney(
            validator,
            journeyDataServiceFactory.create(DEREGISTER_LANDLORD_JOURNEY_URL),
            landlordDeregistrationService,
            securityContextService,
            landlordOneLoginId,
        )
}
