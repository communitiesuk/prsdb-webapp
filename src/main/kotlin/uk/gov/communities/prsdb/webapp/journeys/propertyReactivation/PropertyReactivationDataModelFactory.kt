package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.ELECTRICAL_SAFETY
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.EPC
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.GAS_SAFETY
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ComplianceDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingAndTenancyDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel

@PrsdbWebComponent
class PropertyReactivationDataModelFactory(
    private val licensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory,
    private val complianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory,
) {
    fun fromJourneyState(state: PropertyReactivationJourneyState): PropertyReactivationDataModel {
        val requiredTaskTypes = state.requiredTaskTypes
        return PropertyReactivationDataModel(
            licensing = if (LICENSING in requiredTaskTypes) getLicensing(state) else null,
            gasSafety = if (GAS_SAFETY in requiredTaskTypes) getGasSafety(state) else null,
            electricalSafety = if (ELECTRICAL_SAFETY in requiredTaskTypes) getElectricalSafety(state) else null,
            epc = if (EPC in requiredTaskTypes) getEpc(state) else null,
            tenancyDetails = if (TENANCY_DETAILS in requiredTaskTypes) getTenancyDetails(state) else null,
        )
    }

    private fun getLicensing(state: PropertyReactivationJourneyState) =
        licensingAndTenancyDetailsDataModelFactory.fromLicensingState(state.licensingTask)

    private fun getGasSafety(state: PropertyReactivationJourneyState) =
        complianceDetailsDataModelFactory.fromGasSafetyDetailState(state.gasSafetyTask.gasSafetyDetailsTask)

    private fun getElectricalSafety(state: PropertyReactivationJourneyState) =
        complianceDetailsDataModelFactory.fromElectricalSafetyDetailState(state.electricalSafetyTask.electricalSafetyDetailsTask)

    private fun getEpc(state: PropertyReactivationJourneyState) =
        complianceDetailsDataModelFactory.fromEpcDetailState(state.epcTask.epcDetailsTask)

    private fun getTenancyDetails(state: PropertyReactivationJourneyState) =
        licensingAndTenancyDetailsDataModelFactory.fromTenancyDetailsState(state.tenancyDetailsTask)
}
