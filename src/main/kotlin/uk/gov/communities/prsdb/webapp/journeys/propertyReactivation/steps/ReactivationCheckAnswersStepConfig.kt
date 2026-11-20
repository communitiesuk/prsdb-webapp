package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ReactivationCheckAnswersStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val licensingDetailsHelper: LicensingDetailsHelper,
    private val complianceDetailsHelper: ComplianceDetailsHelper,
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val messageSource: MessageSource,
) : AbstractCheckYourAnswersStepConfig<PropertyReactivationJourneyState>() {
    override fun chooseTemplate(state: PropertyReactivationJourneyState) = "forms/reactivatePropertyCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyReactivationJourneyState): Map<String, Any?> {
        val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(state.propertyOwnershipId)
        val requiredTaskTypes = state.requiredTaskTypes
        val showLicensing = ReactivationTaskType.LICENSING in requiredTaskTypes
        val showGasSafety = ReactivationTaskType.GAS_SAFETY in requiredTaskTypes
        val showElectricalSafety = ReactivationTaskType.ELECTRICAL_SAFETY in requiredTaskTypes
        val showEpc = ReactivationTaskType.EPC in requiredTaskTypes
        val showTenancyDetails = ReactivationTaskType.TENANCY_DETAILS in requiredTaskTypes

        val licensingContent = if (showLicensing) licensingDetailsHelper.getLicensingCyaContent(state, state.licensingTask) else emptyMap()
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
                occupancyDetailsHelper.getTenancyDetailsCyaContent(state, state.tenancyDetailsTask, messageSource)
            } else {
                emptyMap()
            }

        return mapOf(
            "propertyName" to propertyOwnership.address.singleLineAddress,
            "showLicensing" to showLicensing,
            "showGasSafety" to showGasSafety,
            "showElectricalSafety" to showElectricalSafety,
            "showEpc" to showEpc,
            "showTenancyDetails" to showTenancyDetails,
        ) + licensingContent + gasSafetyContent + electricalSafetyContent + epcContent + tenancyContent
    }

    // override this as the next step will handle deleting the journey
    override fun resolveNextDestination(
        state: PropertyReactivationJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination
}

@JourneyFrameworkComponent
final class ReactivationCheckAnswersStep(
    stepConfig: ReactivationCheckAnswersStepConfig,
) : AbstractCheckYourAnswersStep<PropertyReactivationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}
