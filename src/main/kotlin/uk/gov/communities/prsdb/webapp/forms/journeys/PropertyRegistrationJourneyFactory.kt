package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@Component
class PropertyRegistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataService: JourneyDataService,
    private val addressLookupService: AddressLookupService,
    private val addressDataService: AddressDataService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val localAuthorityService: LocalAuthorityService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyRegistrationConfirmationEmail>,
) {
    fun create(principalName: String): PropertyRegistrationJourney {
        val propertyRegistrationJourney =
            PropertyRegistrationJourney(
                validator,
                journeyDataService,
                addressLookupService,
                addressDataService,
                propertyRegistrationService,
                localAuthorityService,
                landlordService,
                absoluteUrlProvider,
                confirmationEmailSender,
            )
        propertyRegistrationJourney.loadJourneyDataIfNotLoaded(principalName)
        return propertyRegistrationJourney
    }
}
