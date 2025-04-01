package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
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
            journeyDataServiceFactory.create(DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnershipId)),
            propertyOwnershipService,
            propertyDeregistrationService,
            confirmationEmailSender,
            propertyOwnershipId,
        )
}
