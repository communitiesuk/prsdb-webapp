package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.RegisteredAddressCache

@Component
class LandlordRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataService: JourneyDataService,
    private val addressLookupService: AddressLookupService,
    private val registeredAddressCache: RegisteredAddressCache,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val emailNotificationService: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
) {
    fun create() =
        LandlordRegistrationJourney(
            validator,
            journeyDataService,
            addressLookupService,
            registeredAddressCache,
            landlordService,
            absoluteUrlProvider,
            emailNotificationService,
        )
}
