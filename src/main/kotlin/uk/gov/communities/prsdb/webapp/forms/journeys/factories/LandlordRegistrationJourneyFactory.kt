package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.services.SecurityContextResetter

@Component
class LandlordRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val addressLookupService: AddressLookupService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val emailNotificationService: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
    private val securityContextResetter: SecurityContextResetter,
) {
    fun create() =
        LandlordRegistrationJourney(
            validator,
            journeyDataServiceFactory.create(REGISTER_LANDLORD_JOURNEY_URL),
            addressLookupService,
            landlordService,
            absoluteUrlProvider,
            emailNotificationService,
            securityContextResetter,
        )
}
