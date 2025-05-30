package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class PropertyComplianceJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcLookupService: EpcLookupService,
    private val propertyComplianceService: PropertyComplianceService,
) {
    fun create(propertyOwnershipId: Long) =
        PropertyComplianceJourney(
            validator,
            journeyDataService = journeyDataServiceFactory.create(getJourneyDataKey(propertyOwnershipId)),
            propertyOwnershipService,
            epcLookupService,
            propertyComplianceService,
            propertyOwnershipId,
        )

    companion object {
        fun getJourneyDataKey(propertyOwnershipId: Long): String =
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId)
    }
}
