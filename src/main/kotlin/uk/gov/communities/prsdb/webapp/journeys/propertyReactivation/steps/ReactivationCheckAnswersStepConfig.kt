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

        // TODO PDJB-2042: add licensing rows once the licensing task (PDJB-2037) is merged
        return buildMap {
            put("propertyName", propertyOwnership.address.singleLineAddress)
            put("showGasSafety", showGasSafety)
            put("showElectricalSafety", showElectricalSafety)
            put("showEpc", showEpc)
            put("showTenancyDetails", showTenancyDetails)
            if (showGasSafety) putAll(complianceDetailsHelper.getGasSafetyCyaContent(state, state.gasSafetyTask))
            if (showElectricalSafety) putAll(complianceDetailsHelper.getElectricalSafetyCyaContent(state, state.electricalSafetyTask))
            if (showEpc) putAll(complianceDetailsHelper.getEpcCyaContent(state, state.epcTask))
            if (showTenancyDetails) {
                put("tenancyRows", occupancyDetailsHelper.getTenancyDetailsSummaryList(state, state.tenancyDetailsTask, messageSource))
            }
        }
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
