package uk.gov.communities.prsdb.webapp.forms.journeys.factories

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDetailsUpdateJourney
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Component
class PropertyDetailsUpdateJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun create(
        propertyOwnershipId: Long,
        stepName: String,
    ) = PropertyDetailsUpdateJourney(
        validator,
        journeyDataServiceFactory.create(getJourneyDataKey(propertyOwnershipId, stepName)),
        propertyOwnershipService,
        propertyOwnershipId,
    )

    fun getJourneyDataKey(
        propertyOwnershipId: Long,
        stepName: String,
    ) = when (stepName) {
        UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment ->
            PropertyDetailsController.getUpdatePropertyDetailsPath(
                propertyOwnershipId,
            ) + "-OWNERSHIP"
        UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
        UpdatePropertyDetailsStepId.UpdateSelectiveLicence.urlPathSegment,
        UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence.urlPathSegment,
        UpdatePropertyDetailsStepId.UpdateHmoMandatoryLicence.urlPathSegment,
        UpdatePropertyDetailsStepId.CheckYourLicensing.urlPathSegment,
        ->
            PropertyDetailsController.getUpdatePropertyDetailsPath(
                propertyOwnershipId,
            ) + "-LICENSING"
        UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment,
        UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment,
        UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment,
        UpdatePropertyDetailsStepId.CheckYourOccupancy.urlPathSegment,
        ->
            PropertyDetailsController.getUpdatePropertyDetailsPath(
                propertyOwnershipId,
            ) + "-OCCUPANCY"
        else -> throw PrsdbWebException("Invalid step name $stepName")
    }
}
