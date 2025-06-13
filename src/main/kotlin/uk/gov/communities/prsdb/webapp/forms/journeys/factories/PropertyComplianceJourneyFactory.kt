package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.WebComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@WebComponent
class PropertyComplianceJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val epcLookupService: EpcLookupService,
    private val propertyComplianceService: PropertyComplianceService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    fun create(propertyOwnershipId: Long) =
        PropertyComplianceJourney(
            validator,
            journeyDataService = journeyDataServiceFactory.create(getJourneyDataKey(propertyOwnershipId)),
            propertyOwnershipService,
            epcLookupService,
            propertyComplianceService,
            propertyOwnershipId,
            epcCertificateUrlProvider,
        )

    companion object {
        fun getJourneyDataKey(propertyOwnershipId: Long): String =
            PropertyComplianceController.getPropertyCompliancePath(propertyOwnershipId)
    }
}
