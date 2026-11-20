package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingAndTenancyDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel

@PrsdbWebComponent
class PropertyReactivationDataModelFactory(
    private val licensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory,
) {
    fun fromJourneyState(state: PropertyReactivationJourneyState): PropertyReactivationDataModel {
        val requiredTaskTypes = state.requiredTaskTypes
        return PropertyReactivationDataModel(
            licensing = if (LICENSING in requiredTaskTypes) getLicensing(state) else null,
            tenancyDetails = if (TENANCY_DETAILS in requiredTaskTypes) getTenancyDetails(state) else null,
        )
    }

    private fun getLicensing(state: PropertyReactivationJourneyState) =
        licensingAndTenancyDetailsDataModelFactory.fromLicensingState(state.licensingTask)

    private fun getTenancyDetails(state: PropertyReactivationJourneyState) =
        licensingAndTenancyDetailsDataModelFactory.fromTenancyDetailsState(state.tenancyDetailsTask)
}
