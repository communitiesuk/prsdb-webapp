package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebService
class PropertyComplianceUpdateJourneyFactory(
    val validator: Validator,
    val journeyDataServiceFactory: JourneyDataServiceFactory,
    val propertyComplianceService: PropertyComplianceService,
    val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    val epcLookupService: EpcLookupService,
) {
    fun create(
        stepName: String,
        propertyOwnershipId: Long,
        checkingAnswersFor: String? = null,
    ) = PropertyComplianceUpdateJourney(
        validator = validator,
        journeyDataService =
            journeyDataServiceFactory.create(
                getJourneyDataKey(
                    propertyOwnershipId,
                    PropertyComplianceStepId.entries.find { it.urlPathSegment == stepName }!!.groupIdentifier,
                ),
            ),
        stepName = stepName,
        propertyOwnershipId = propertyOwnershipId,
        propertyComplianceService = propertyComplianceService,
        epcCertificateUrlProvider = epcCertificateUrlProvider,
        epcLookupService = epcLookupService,
        checkingAnswersForStep = checkingAnswersFor,
    )

    private fun getJourneyDataKey(
        propertyOwnershipId: Long,
        stepGroupId: PropertyComplianceGroupIdentifier,
    ) = PropertyComplianceController
        .getUpdatePropertyComplianceBasePath(propertyOwnershipId) + stepGroupId.name
}
