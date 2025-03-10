package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateLandlordDetailsJourney
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class LandlordDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val addressLookupService: AddressLookupService,
    private val landlordService: LandlordService,
) {
    fun create(landlordBaseUserId: String) =
        UpdateLandlordDetailsJourney(
            validator,
            journeyDataServiceFactory.create(UPDATE_LANDLORD_DETAILS_URL),
            addressLookupService,
            landlordService,
            landlordBaseUserId,
        )
}
