package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.isDelegatedToLettingAgent
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

@JourneyFrameworkComponent
class PropertyRegistrationCyaStepConfig(
    private val localCouncilService: LocalCouncilService,
    private val licensingHelper: LicensingDetailsHelper,
    private val occupancyDetailsHelper: OccupancyDetailsHelper,
    private val complianceDetailsHelper: ComplianceDetailsHelper,
    private val messageSource: MessageSource,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractCheckYourAnswersStepConfig<PropertyRegistrationJourneyState>() {
    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String =
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            "forms/restructureAndSkipping/propertyRegistrationCheckAnswersForm"
        } else {
            "forms/restructureAndSkipping/propertyRegistrationCheckAnswersFormLegacy"
        }

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val isRestructured = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        val isDelegatedToLettingAgent = state.isDelegatedToLettingAgent(featureFlagManager)
        val content =
            mutableMapOf<String, Any?>(
                "title" to "registerProperty.title",
                "submitButtonText" to "forms.buttons.completeRegistration",
                "insetText" to true,
                // TODO PDJB-1391: shows a placeholder banner while the letting-agent path reuses this CYA page with
                //  mocked "provide later" values. Replace with the real delegated-details display.
                "lettingAgentDelegationTodo" to isDelegatedToLettingAgent,
                "propertyName" to
                    state.propertyDetailsTask.addressTask
                        .getAddress()
                        .singleLineAddress,
                "propertyDetails" to
                    if (isRestructured) {
                        getRestructuredPropertyDetailsSummaryList(state)
                    } else {
                        getPropertyDetailsSummaryList(state)
                    },
                "licensingDetails" to
                    if (isDelegatedToLettingAgent) {
                        // TODO PDJB-1391: licensing is skipped when a letting agent provides the details, so no real
                        //  answer exists yet. Substitute a placeholder "provide later" row so the page can render.
                        getMockProvideLaterSummaryList("forms.checkPropertyAnswers.propertyDetails.licensingType")
                    } else {
                        licensingHelper.getCheckYourAnswersSummaryList(state, state.licensingTask)
                    },
                "occupancyDetails" to
                    if (isRestructured) {
                        occupancyDetailsHelper.getRestructuredOccupancySummaryList(state)
                    } else {
                        null
                    },
            )

        content["jointLandlordsDetails"] = getJointLandLordsSummaryRow(state)
        if (isDelegatedToLettingAgent) {
            // TODO PDJB-1391: tenancy details are skipped when a letting agent provides the details, so no real
            //  answer exists yet. Substitute a placeholder "provide later" row so the page can render.
            content["tenancyDetails"] =
                getMockProvideLaterSummaryList("forms.checkPropertyAnswers.tenancyDetails.restructureAndSkipping.tenancyDetailsRow")
        } else if (isRestructured) {
            content["tenancyDetails"] =
                occupancyDetailsHelper.getRestructuredCheckYourAnswersSummaryList(
                    state,
                    messageSource,
                    Destination.VisitableStep(
                        state.tenancyDetailsTask.householdsAndTenantsTask.households,
                        state.getCyaJourneyId(state.tenancyDetailsTask.householdsAndTenantsTask.provideTenancyDetailsLaterStep),
                    ),
                )
        } else {
            content["tenancyDetails"] = occupancyDetailsHelper.getCheckYourAnswersSummaryList(state, messageSource)
        }

        if (isDelegatedToLettingAgent) {
            // TODO PDJB-1391: the gas and electrical safety tasks are skipped when a letting agent provides the
            //  details. Their CYA row factories throw when the task was never reached, so substitute placeholder
            //  "provide later" rows. (The EPC factory degrades gracefully to a "no EPC provided" summary.)
            content += getMockDelegatedGasContent()
            content += getMockDelegatedElectricalContent()
        } else {
            content += complianceDetailsHelper.getGasSafetyCyaContent(state, state.gasSafetyTask)
            content += complianceDetailsHelper.getElectricalSafetyCyaContent(state, state.electricalSafetyTask)
        }
        content += complianceDetailsHelper.getEpcCyaContent(state, state.epcTask)

        return content
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getJointLandLordsSummaryRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val jointLandlordsTask = state.ownershipAndLandlordsTask.jointLandlordsTask
        val hasJointLandlords =
            jointLandlordsTask.hasJointLandlordsStep.formModel.notNullValue(
                HasJointLandlordsFormModel::hasJointLandlords,
            )
        return if (hasJointLandlords) {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.invitations",
                jointLandlordsTask.inviteJointLandlordsTask.invitedJointLandlords,
                Destination.VisitableStep(
                    jointLandlordsTask.inviteJointLandlordsTask.checkJointLandlordsStep,
                    state.getCyaJourneyId(jointLandlordsTask.inviteJointLandlordsTask.checkJointLandlordsStep),
                ),
            )
        } else {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.areThereJointLandlords",
                "forms.checkPropertyAnswers.jointLandlordsDetails.noJointLandlords",
                Destination.VisitableStep(
                    jointLandlordsTask.hasJointLandlordsStep,
                    state.getCyaJourneyId(jointLandlordsTask.hasJointLandlordsStep),
                ),
            )
        }
    }

    // TODO PDJB-1391: placeholder used while the letting-agent path reuses this CYA page. The relevant task is
    //  skipped in that flow, so there is no real answer to show yet; this renders a single "provide later" row.
    private fun getMockProvideLaterSummaryList(fieldHeading: String): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading,
                "forms.checkPropertyAnswers.tenancyDetails.provideLater",
                actionUrl = null,
            ),
        )

    // TODO PDJB-1391: placeholder gas safety content for the skipped letting-agent path (matches the keys the real
    //  ComplianceDetailsHelper.getGasSafetyCyaContent produces, so the template renders unchanged).
    private fun getMockDelegatedGasContent(): Map<String, Any?> =
        mapOf(
            "gasSupplyRows" to getMockProvideLaterSummaryList("checkGasSafety.gasCert.fieldHeading"),
            "gasCertRows" to emptyList<SummaryListRowViewModel>(),
            "gasInsetTextKey" to null,
        )

    // TODO PDJB-1391: placeholder electrical safety content for the skipped letting-agent path (matches the keys the
    //  real ComplianceDetailsHelper.getElectricalSafetyCyaContent produces, so the template renders unchanged).
    private fun getMockDelegatedElectricalContent(): Map<String, Any?> =
        mapOf(
            "electricalRows" to getMockProvideLaterSummaryList("checkElectricalSafety.electricalCert.fieldHeading"),
            "electricalInsetTextKey" to null,
        )

    private fun getPropertyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        getAddressRows(state) +
            getPropertyTypeRow(state) +
            getOwnershipTypeRow(state)

    private fun getRestructuredPropertyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        getAddressRows(state) +
            getPropertyTypeRow(state) +
            getBedroomsRow(state) +
            getOwnershipTypeRow(state)

    private fun getBedroomsRow(state: PropertyRegistrationJourneyState) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.bedrooms",
            state.bedrooms.formModel.numberOfBedrooms,
            Destination.VisitableStep(state.bedrooms, state.getCyaJourneyId(state.bedrooms)),
        )

    private fun getAddressRows(state: PropertyRegistrationJourneyState) =
        state.propertyDetailsTask.addressTask.getAddress().let { address ->
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.address",
                    address.singleLineAddress,
                    Destination.VisitableStep(
                        state.propertyDetailsTask.addressTask.lookupAddressStep,
                        state.getCyaJourneyId(state.propertyDetailsTask.addressTask.lookupAddressStep),
                    ),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.localCouncil",
                    localCouncilService.retrieveLocalCouncilById(address.localCouncilId!!).name,
                    Destination.VisitableStep(
                        state.propertyDetailsTask.addressTask.localCouncilStep,
                        state.getCyaJourneyId(state.propertyDetailsTask.addressTask.localCouncilStep),
                    ),
                ),
            )
        }

    private fun getPropertyTypeRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val propertyTypeStep = state.propertyDetailsTask.propertyTypeStep
        val propertyType = propertyTypeStep.formModel.propertyType
        val customType = propertyTypeStep.formModel.customPropertyType
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.type",
            if (propertyType == PropertyType.OTHER) listOf(propertyType, customType) else propertyType,
            Destination.VisitableStep(propertyTypeStep, state.getCyaJourneyId(propertyTypeStep)),
        )
    }

    private fun getOwnershipTypeRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val ownershipTypeStep = state.ownershipAndLandlordsTask.ownershipTypeStep
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            ownershipTypeStep.formModel.ownershipType,
            Destination.VisitableStep(ownershipTypeStep, state.getCyaJourneyId(ownershipTypeStep)),
        )
    }
}

@JourneyFrameworkComponent
final class PropertyRegistrationCyaStep(
    stepConfig: PropertyRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}
