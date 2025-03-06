package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateLandlordDetailsJourney
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.RegisteredAddressCache

@Component
class LandlordDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataService: JourneyDataService,
    private val addressLookupService: AddressLookupService,
    private val landlordService: LandlordService,
    private val registeredAddressCache: RegisteredAddressCache,
) {
    fun create(landlordBaseUserId: String) =
        UpdateLandlordDetailsJourney(
            validator,
            journeyDataService,
            addressLookupService,
            landlordService,
            registeredAddressCache,
            landlordBaseUserId,
        )
}
