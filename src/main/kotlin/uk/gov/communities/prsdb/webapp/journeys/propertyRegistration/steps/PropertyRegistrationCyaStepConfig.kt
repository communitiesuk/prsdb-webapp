package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
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
    // TODO PDJB-1340: Remove the legacy template branch when PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed.
    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String =
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            "forms/restructureAndSkipping/propertyRegistrationCheckAnswersForm"
        } else {
            "forms/restructureAndSkipping/propertyRegistrationCheckAnswersFormLegacy"
        }

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val isSkippingEnabled = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        val isDelegateToLettingAgentEnabled = featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)
        val isDelegatedInSkippingFlow =
            isSkippingEnabled && state.isDelegatedToLettingAgent(featureFlagManager)
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val whoProvidesRentalDetails =
            if (isSkippingEnabled && isDelegateToLettingAgentEnabled) {
                state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep.formModelIfReachableOrNull?.whoProvides
            } else {
                null
            }
        val licensingDetails =
            if (isDelegatedInSkippingFlow) {
                getMockProvideLaterSummaryList(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    getProvideLaterMessageKey(isOccupied),
                )
            } else {
                getLicensingDetailsForState(state, isOccupied)
            }
        val tenancyDetails = getTenancyDetails(state, isDelegatedInSkippingFlow, isSkippingEnabled, isOccupied)
        val content =
            mutableMapOf<String, Any?>(
                "title" to "registerProperty.title",
                "submitButtonText" to "forms.buttons.completeRegistration",
                "insetText" to true,
                "propertyName" to
                    state.propertyDetailsTask.addressTask
                        .getAddress()
                        .singleLineAddress,
                "propertyDetails" to
                    if (isSkippingEnabled) {
                        getRestructuredPropertyDetailsSummaryList(state)
                    } else {
                        getPropertyDetailsSummaryList(state)
                    },
                "licensingDetails" to
                    licensingDetails,
                "occupancyDetails" to
                    if (isSkippingEnabled) {
                        occupancyDetailsHelper.getRestructuredOccupancySummaryList(state)
                    } else {
                        null
                    },
                "jointLandlordsDetails" to getJointLandLordsSummaryRow(state),
                "tenancyDetails" to tenancyDetails,
            )

        whoProvidesRentalDetails?.let {
            content += getLettingAgentDelegationSummaryContent(state, it)
        }

        if (isDelegatedInSkippingFlow) {
            content += getMockDelegatedGasContent(isOccupied)
            content += getMockDelegatedElectricalContent(isOccupied)
        } else {
            content += complianceDetailsHelper.getGasSafetyCyaContent(state, state.gasSafetyTask)
            content += complianceDetailsHelper.getElectricalSafetyCyaContent(state, state.electricalSafetyTask)
        }
        content += complianceDetailsHelper.getEpcCyaContent(state, state.epcTask)

        if (isSkippingEnabled) {
            val occupancyDetails = (content["occupancyDetails"] as? List<SummaryListRowViewModel>) ?: emptyList()
            val gasSupplyRows = (content["gasSupplyRows"] as? List<SummaryListRowViewModel>) ?: emptyList()
            val electricalRows = (content["electricalRows"] as? List<SummaryListRowViewModel>) ?: emptyList()

            content +=
                mapOf(
                    "aboutPropertyHeadingKey" to "forms.checkPropertyAnswers.aboutYourProperty.heading",
                    "ownershipAndLandlordsHeadingKey" to "forms.checkPropertyAnswers.ownershipAndLandlords.heading",
                    "ownershipAndLandlordsRows" to
                        listOfNotNull(
                            (content["jointLandlordsDetails"] as? SummaryListRowViewModel),
                        ),
                    "rentedOutHeadingKey" to "forms.checkPropertyAnswers.rentedOut.heading",
                    "rentedOutLicensingHeadingKey" to "forms.checkPropertyAnswers.rentedOut.licensing.heading",
                    "rentedOutGasHeadingKey" to "checkGasSafety.heading",
                    "rentedOutElectricalHeadingKey" to "checkElectricalSafety.heading",
                    "rentedOutEpcHeadingKey" to "propertyCompliance.epcTask.checkEpcAnswers.heading",
                    "rentedOutTenancyHeadingKey" to "forms.checkPropertyAnswers.tenancyDetails.restructureAndSkipping.heading",
                    "rentedOutLicensingRows" to licensingDetails,
                    "rentedOutGasRows" to gasSupplyRows,
                    "rentedOutElectricalRows" to electricalRows,
                    "rentedOutTenancyRows" to tenancyDetails,
                    "occupancyDetails" to occupancyDetails,
                    "tenancyUnoccupiedBodyTextKey" to
                        if (!isOccupied) {
                            "forms.checkPropertyAnswers.tenancyDetails.unoccupiedBodyText"
                        } else {
                            null
                        },
                )
        }

        return content
    }

    private fun getTenancyDetails(
        state: PropertyRegistrationJourneyState,
        isDelegatedInSkippingFlow: Boolean,
        isSkippingEnabled: Boolean,
        isOccupied: Boolean,
    ): List<SummaryListRowViewModel> =
        when {
            isDelegatedInSkippingFlow ->
                getMockProvideLaterSummaryList(
                    "forms.checkPropertyAnswers.tenancyDetails.restructureAndSkipping.tenancyDetailsRow",
                    getProvideLaterMessageKey(isOccupied),
                )

            isSkippingEnabled ->
                occupancyDetailsHelper.getRestructuredCheckYourAnswersSummaryList(
                    state,
                    messageSource,
                    Destination.VisitableStep(
                        state.tenancyDetailsTask.householdsAndTenantsTask.households,
                        state.getCyaJourneyId(state.tenancyDetailsTask.householdsAndTenantsTask.provideTenancyDetailsLaterStep),
                    ),
                )

            else -> occupancyDetailsHelper.getCheckYourAnswersSummaryList(state, messageSource)
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
    private fun getMockProvideLaterSummaryList(
        fieldHeading: String,
        provideLaterMessageKey: String = "forms.checkPropertyAnswers.tenancyDetails.provideLater",
    ): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading,
                provideLaterMessageKey,
                actionUrl = null,
            ),
        )

    // TODO PDJB-1391: placeholder gas safety content for the skipped letting-agent path (matches the keys the real
    //  ComplianceDetailsHelper.getGasSafetyCyaContent produces, so the template renders unchanged).
    private fun getMockDelegatedGasContent(isOccupied: Boolean): Map<String, Any?> =
        mapOf(
            "gasSupplyRows" to getMockProvideLaterSummaryList("checkGasSafety.gasCert.fieldHeading", getProvideLaterMessageKey(isOccupied)),
            "gasCertRows" to emptyList<SummaryListRowViewModel>(),
            "gasInsetTextKey" to null,
        )

    // TODO PDJB-1391: placeholder electrical safety content for the skipped letting-agent path (matches the keys the
    //  real ComplianceDetailsHelper.getElectricalSafetyCyaContent produces, so the template renders unchanged).
    private fun getMockDelegatedElectricalContent(isOccupied: Boolean): Map<String, Any?> =
        mapOf(
            "electricalRows" to
                getMockProvideLaterSummaryList(
                    "checkElectricalSafety.electricalCert.fieldHeading",
                    getProvideLaterMessageKey(isOccupied),
                ),
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

    private fun getLettingAgentDelegationSummaryContent(
        state: PropertyRegistrationJourneyState,
        whoProvides: WhoProvidesRentalDetails,
    ): Map<String, Any?> {
        val whoWillProvideMsgKey =
            when (whoProvides) {
                WhoProvidesRentalDetails.LANDLORD -> "forms.checkPropertyAnswers.lettingAgentDelegation.values.landlord.label"
                WhoProvidesRentalDetails.LETTING_AGENT -> "forms.checkPropertyAnswers.lettingAgentDelegation.values.lettingAgent.label"
            }

        val rows =
            mutableListOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.lettingAgentDelegation.rows.whoWillProvide.label",
                    whoWillProvideMsgKey,
                    Destination.VisitableStep(
                        state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep,
                        state.getCyaJourneyId(state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep),
                    ),
                ),
            )

        if (whoProvides == WhoProvidesRentalDetails.LETTING_AGENT) {
            rows +=
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.lettingAgentDelegation.rows.agentEmail.label",
                    state.whoProvidesDetailsTask.lettingAgentEmailStep.formModel.emailAddress,
                    Destination.VisitableStep(
                        state.whoProvidesDetailsTask.lettingAgentEmailStep,
                        state.getCyaJourneyId(state.whoProvidesDetailsTask.lettingAgentEmailStep),
                    ),
                )
        }

        return mapOf(
            "lettingAgentDelegation" to rows,
            "lettingAgentDelegationBodyText" to (whoProvides == WhoProvidesRentalDetails.LETTING_AGENT),
        )
    }

    private fun getLicensingDetailsForState(
        state: PropertyRegistrationJourneyState,
        isOccupied: Boolean,
    ): List<SummaryListRowViewModel> {
        val licensingTask = state.licensingTask
        if (isOccupied || licensingTask.getLicensingType() != LicensingType.PROVIDE_LATER) {
            return licensingHelper.getCheckYourAnswersSummaryList(state, licensingTask)
        }

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.licensingType",
                "forms.checkPropertyAnswers.propertyDetails.licensingProvideLaterUnoccupied",
                Destination.VisitableStep(licensingTask.licensingTypeStep, state.getCyaJourneyId(licensingTask.licensingTypeStep)),
            ),
        )
    }

    private fun getProvideLaterMessageKey(isOccupied: Boolean): String =
        if (isOccupied) {
            "forms.checkPropertyAnswers.tenancyDetails.provideLater"
        } else {
            "forms.checkPropertyAnswers.tenancyDetails.provideLaterUnoccupied"
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
