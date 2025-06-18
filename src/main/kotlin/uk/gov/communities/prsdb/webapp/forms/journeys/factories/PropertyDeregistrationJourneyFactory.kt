package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebComponent
class PropertyDeregistrationJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val confirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>,
) {
    fun create(propertyOwnershipId: Long) =
        PropertyDeregistrationJourney(
            validator,
            journeyDataServiceFactory.create(getJourneyKey(propertyOwnershipId)),
            propertyOwnershipService,
            propertyDeregistrationService,
            confirmationEmailSender,
            propertyOwnershipId,
        )

    companion object {
        fun getJourneyKey(propertyOwnershipId: Long) = DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnershipId)
    }
}
