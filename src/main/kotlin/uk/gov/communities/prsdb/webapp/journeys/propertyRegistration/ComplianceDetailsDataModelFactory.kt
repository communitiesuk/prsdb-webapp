package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.models.dataModels.ElectricalSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GasSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@PrsdbWebComponent
class ComplianceDetailsDataModelFactory(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    fun fromGasSafetyDetailState(state: GasSafetyDetailState): GasSafetyDetailsDataModel {
        val hasGasSupply =
            when (state.hasGasSupplyStep.outcome) {
                HasGasSupplyMode.HAS_SUPPLY -> true
                HasGasSupplyMode.NO_SUPPLY -> false
                HasGasSupplyMode.PROVIDE_LATER ->
                    throw IllegalStateException("Gas safety can only be provided later when registering a property")
                null -> throw NotNullFormModelValueIsNullException("hasGasSupply is null")
            }
        return GasSafetyDetailsDataModel(
            hasGasSupply = hasGasSupply,
            issueDate = state.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
            uploadIds = state.gasUploadIds,
        )
    }

    fun fromElectricalSafetyDetailState(state: ElectricalSafetyDetailState): ElectricalSafetyDetailsDataModel {
        check(state.hasElectricalCertStep.outcome != HasElectricalCertMode.PROVIDE_THIS_LATER) {
            "Electrical safety can only be provided later when registering a property"
        }
        return ElectricalSafetyDetailsDataModel(
            certType = state.mapElectricalCertificateTypeToGlobalCertificateType(),
            eicrExpiryDate = state.getEicrExpiryDateIfReachable()?.toJavaLocalDate(),
            eicIssueDate = state.getEicIssueDateIfReachable()?.toJavaLocalDate(),
            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            eicExpiryDate = state.getEicExpiryDateIfReachable()?.toJavaLocalDate(),
            uploadIds = state.electricalUploadIds,
        )
    }

    fun fromEpcDetailState(state: EpcDetailState): EpcDetailsDataModel {
        check(state.hasEpcStep.outcome != HasEpcMode.PROVIDE_LATER) { "The EPC can only be provided later when registering a property" }
        val acceptedEpc = state.acceptedEpcIfStillAccepted
        return EpcDetailsDataModel(
            certificateUrl = acceptedEpc?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber) },
            expiryDate = acceptedEpc?.expiryDateAsJavaLocalDate,
            energyRating = acceptedEpc?.energyRating,
            tenancyStartedBeforeExpiry = state.epcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull?.tenancyStartedBeforeExpiry,
            exemptionReason = state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
            meesExemptionReason = state.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason,
        )
    }
}
