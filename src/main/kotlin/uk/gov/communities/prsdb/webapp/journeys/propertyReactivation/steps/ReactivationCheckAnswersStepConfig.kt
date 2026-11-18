package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ReactivationCheckAnswersStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val complianceDetailsHelper: ComplianceDetailsHelper,
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<PropertyReactivationJourneyState>() {
    override fun chooseTemplate(state: PropertyReactivationJourneyState) = "forms/reactivatePropertyCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyReactivationJourneyState): Map<String, Any?> {
        val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(state.propertyOwnershipId)
        val requiredTaskTypes = state.requiredTaskTypes
        val showGasSafety = ReactivationTaskType.GAS_SAFETY in requiredTaskTypes
        val showElectricalSafety = ReactivationTaskType.ELECTRICAL_SAFETY in requiredTaskTypes
        val showEpc = ReactivationTaskType.EPC in requiredTaskTypes
        val showTenancyDetails = ReactivationTaskType.TENANCY_DETAILS in requiredTaskTypes

        val gasSafetyContent = if (showGasSafety) complianceDetailsHelper.getGasSafetyCyaContent(state, state.gasSafetyTask) else emptyMap()
        val electricalSafetyContent =
            if (showElectricalSafety) {
                complianceDetailsHelper.getElectricalSafetyCyaContent(state, state.electricalSafetyTask)
            } else {
                emptyMap()
            }
        val epcContent = if (showEpc) complianceDetailsHelper.getEpcCyaContent(state, state.epcTask) else emptyMap()
        val tenancyContent =
            if (showTenancyDetails) {
                occupancyDetailsHelper.getTenancyDetailsCyaContent(state, messageSource)
            } else {
                emptyMap()
            }

        // TODO PDJB-2042: add licensing rows once the licensing task (PDJB-2037) is merged
        return mapOf(
            "propertyName" to propertyOwnership.address.singleLineAddress,
            "showGasSafety" to showGasSafety,
            "showElectricalSafety" to showElectricalSafety,
            "showEpc" to showEpc,
            "showTenancyDetails" to showTenancyDetails,
        ) + gasSafetyContent + electricalSafetyContent + epcContent + tenancyContent
    }
}

@JourneyFrameworkComponent
final class ReactivationCheckAnswersStep(
    stepConfig: ReactivationCheckAnswersStepConfig,
) : AbstractCheckYourAnswersStep<PropertyReactivationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}
