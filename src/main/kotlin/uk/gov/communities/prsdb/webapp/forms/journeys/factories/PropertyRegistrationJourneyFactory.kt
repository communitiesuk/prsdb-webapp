package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class PropertyRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val addressLookupService: AddressLookupService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val localAuthorityService: LocalAuthorityService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
) {
    fun create(principalName: String) =
        PropertyRegistrationJourney(
            validator,
            journeyDataServiceFactory.create(REGISTER_PROPERTY_JOURNEY_URL),
            addressLookupService,
            propertyRegistrationService,
            localAuthorityService,
            landlordService,
            absoluteUrlProvider,
            confirmationEmailSender,
        )
}
