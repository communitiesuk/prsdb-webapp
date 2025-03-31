package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.ProvideComplianceController
import uk.gov.communities.prsdb.webapp.forms.journeys.ComplianceProvisionJourney
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class ComplianceProvisionJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    fun create(propertyOwnershipId: Long) =
        ComplianceProvisionJourney(
            validator,
            journeyDataServiceFactory.create(ProvideComplianceController.getProvideCompliancePath(propertyOwnershipId)),
        )
}
