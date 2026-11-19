package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.ELECTRICAL_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.EPC
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.GAS_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationElectricalSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationEpcDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationGasSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationLicensingDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationTenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RentFrequencyFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@PrsdbWebComponent
class PropertyReactivationDataModelFactory(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
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

    private fun getLicensing(state: PropertyReactivationJourneyState): ReactivationLicensingDataModel {
        val licensingType = state.licensingTask.getLicensingType()
        check(licensingType != LicensingType.PROVIDE_LATER) { "Licensing cannot be provided later when reactivating a property" }
        return ReactivationLicensingDataModel(licensingType, state.licensingTask.getLicenceNumberOrNull())
    }

    private fun getGasSafety(state: PropertyReactivationJourneyState): ReactivationGasSafetyDataModel {
        val gasSafetyDetailsTask = state.gasSafetyTask.gasSafetyDetailsTask
        val hasGasSupply =
            when (gasSafetyDetailsTask.hasGasSupplyStep.outcome) {
                HasGasSupplyMode.HAS_SUPPLY -> true
                HasGasSupplyMode.NO_SUPPLY -> false
                HasGasSupplyMode.PROVIDE_LATER, null ->
                    throw IllegalStateException("The gas supply question must be answered when reactivating a property")
            }
        return ReactivationGasSafetyDataModel(
            hasGasSupply = hasGasSupply,
            issueDate = gasSafetyDetailsTask.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
            uploadIds = gasSafetyDetailsTask.gasUploadIds,
        )
    }

    private fun getElectricalSafety(state: PropertyReactivationJourneyState): ReactivationElectricalSafetyDataModel {
        val electricalSafetyDetailsTask = state.electricalSafetyTask.electricalSafetyDetailsTask
        check(electricalSafetyDetailsTask.hasElectricalCertStep.outcome != HasElectricalCertMode.PROVIDE_THIS_LATER) {
            "Electrical safety cannot be provided later when reactivating a property"
        }
        return ReactivationElectricalSafetyDataModel(
            certType = electricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType(),
            eicrExpiryDate = electricalSafetyDetailsTask.getEicrExpiryDateIfReachable()?.toJavaLocalDate(),
            eicIssueDate = electricalSafetyDetailsTask.getEicIssueDateIfReachable()?.toJavaLocalDate(),
            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            eicExpiryDate = electricalSafetyDetailsTask.getEicExpiryDateIfReachable()?.toJavaLocalDate(),
            uploadIds = electricalSafetyDetailsTask.electricalUploadIds,
        )
    }

    private fun getEpc(state: PropertyReactivationJourneyState): ReactivationEpcDataModel {
        val epcDetailsTask = state.epcTask.epcDetailsTask
        check(
            epcDetailsTask.hasEpcStep.outcome != HasEpcMode.PROVIDE_LATER,
        ) { "The EPC cannot be provided later when reactivating a property" }
        val acceptedEpc = epcDetailsTask.acceptedEpcIfStillAccepted
        return ReactivationEpcDataModel(
            certificateUrl = acceptedEpc?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber) },
            expiryDate = acceptedEpc?.expiryDateAsJavaLocalDate,
            energyRating = acceptedEpc?.energyRating,
            tenancyStartedBeforeExpiry =
                epcDetailsTask.epcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull?.tenancyStartedBeforeExpiry,
            exemptionReason = epcDetailsTask.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
            meesExemptionReason = epcDetailsTask.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason,
        )
    }

    private fun getTenancyDetails(state: PropertyReactivationJourneyState): ReactivationTenancyDetailsDataModel {
        val tenancyDetailsTask = state.tenancyDetailsTask
        val billsIncluded = tenancyDetailsTask.rentIncludesBillsTask.getBillsIncludedOrNull()
        return ReactivationTenancyDetailsDataModel(
            numberOfHouseholds =
                tenancyDetailsTask.householdsAndTenantsTask.households.formModel
                    .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                    .toInt(),
            numberOfPeople =
                tenancyDetailsTask.householdsAndTenantsTask.tenants.formModel
                    .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                    .toInt(),
            billsIncludedList = billsIncluded?.standardBillsIncludedListAsString,
            customBillsIncluded = billsIncluded?.customBillsIncluded,
            furnishedStatus = tenancyDetailsTask.furnishedStatus.formModel.notNullValue(FurnishedStatusFormModel::furnishedStatus),
            rentFrequency =
                tenancyDetailsTask.rentFrequencyAndAmountTask.rentFrequency.formModel
                    .notNullValue(RentFrequencyFormModel::rentFrequency),
            customRentFrequency = tenancyDetailsTask.rentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected(),
            rentAmount =
                tenancyDetailsTask.rentFrequencyAndAmountTask.rentAmount.formModel.rentAmount
                    .toBigDecimal(),
        )
    }
}
