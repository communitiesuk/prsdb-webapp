package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.context.MessageSource
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class PropertyComplianceJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcLookupService: EpcLookupService,
    private val propertyComplianceService: PropertyComplianceService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val messageSource: MessageSource,
    private val fullPropertyComplianceConfirmationEmailService: EmailNotificationService<FullPropertyComplianceConfirmationEmail>,
    private val partialPropertyComplianceConfirmationEmailService: EmailNotificationService<PartialPropertyComplianceConfirmationEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    fun create(
        propertyOwnershipId: Long,
        checkingAnswersFor: String? = null,
    ) = PropertyComplianceJourney(
        validator,
        journeyDataService = journeyDataServiceFactory.create(getJourneyDataKey(propertyOwnershipId)),
        propertyOwnershipService,
        epcLookupService,
        propertyComplianceService,
        propertyOwnershipId,
        epcCertificateUrlProvider,
        messageSource,
        fullPropertyComplianceConfirmationEmailService,
        partialPropertyComplianceConfirmationEmailService,
        absoluteUrlProvider,
        checkingAnswersFor,
    )

    companion object {
        fun getJourneyDataKey(propertyOwnershipId: Long): String =
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId)
    }
}
