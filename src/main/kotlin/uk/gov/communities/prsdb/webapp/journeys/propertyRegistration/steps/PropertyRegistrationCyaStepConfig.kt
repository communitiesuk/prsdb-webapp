package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.PAYMENTS
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsPropertyRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OwnershipStructureDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.PropertyManagerSummaryCardHelper
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
    private val ownershipStructureDetailsHelper: OwnershipStructureDetailsHelper,
    private val propertyManagerSummaryCardHelper: PropertyManagerSummaryCardHelper,
) : AbstractCheckYourAnswersStepConfig<PropertyRegistrationJourneyState>() {
    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = "forms/propertyRegistrationCheckAnswersForm"

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> =
        if (state.isDelegatedToLettingAgent()) {
            getDelegatedContent(state)
        } else {
            getNonDelegatedContent(state)
        }

    private fun getNonDelegatedContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val whoProvides =
            state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep.formModelIfReachableOrNull
                ?.whoProvides
        val delegationContent = whoProvides?.let { getLettingAgentDelegationSummaryContent(state, it) } ?: emptyMap()
        return getContent(state, delegationContent) +
            mapOf(
                "showLettingAgentDelegationUnoccupiedPanel" to
                    (!state.occupied.formModel.notNullValue(OccupancyFormModel::occupied) && whoProvides == null),
            )
    }

    private fun getContent(
        state: PropertyRegistrationJourneyState,
        delegationContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val licensingDetails = licensingHelper.getCheckYourAnswersSummaryList(state, state.licensingTask)
        val tenancyDetails = getTenancyDetails(state)
        val occupancyDetails = occupancyDetailsHelper.getOccupancySummaryList(state)
        val gasSafetyContent = complianceDetailsHelper.getGasSafetyCyaContent(state, state.gasSafetyTask)
        val electricalSafetyContent = complianceDetailsHelper.getElectricalSafetyCyaContent(state, state.electricalSafetyTask)
        val complianceContent =
            gasSafetyContent +
                electricalSafetyContent +
                complianceDetailsHelper.getEpcCyaContent(state, state.epcTask)
        return getBaseContent(state, occupancyDetails) +
            delegationContent +
            complianceContent +
            getContentSections(
                state,
                isOccupied,
                licensingDetails,
                tenancyDetails,
                occupancyDetails,
            )
    }

    private fun getDelegatedContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val occupancyDetails = occupancyDetailsHelper.getOccupancySummaryList(state)
        val whoProvides =
            state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep.formModelIfReachableOrNull
                ?.whoProvides
        return getBaseContent(state, occupancyDetails) +
            (whoProvides?.let { getLettingAgentDelegationSummaryContent(state, it) } ?: emptyMap()) +
            mapOf(
                "hideDelegatedSections" to true,
            ) +
            getContentSections(
                state,
                isOccupied,
                licensingDetails = emptyList(),
                tenancyDetails = emptyList(),
                occupancyDetails,
            )
    }

    private fun getBaseContent(
        state: PropertyRegistrationJourneyState,
        occupancyDetails: List<SummaryListRowViewModel>,
    ) = mapOf<String, Any?>(
        "submitButtonText" to getSubmitButtonText(),
        "warningTextKey" to "forms.checkPropertyAnswers.warning",
        "propertyName" to
            state.propertyDetailsTask.addressTask
                .getAddress()
                .singleLineAddress,
        "propertyDetails" to getPropertyDetailsSummaryList(state),
        "occupancyDetails" to occupancyDetails,
    )

    private fun getSubmitButtonText(): String =
        if (featureFlagManager.checkFeature(PAYMENTS)) {
            "forms.buttons.submitAndPay"
        } else {
            "forms.buttons.completeRegistration"
        }

    private fun getContentSections(
        state: PropertyRegistrationJourneyState,
        isOccupied: Boolean,
        licensingDetails: List<SummaryListRowViewModel>,
        tenancyDetails: List<SummaryListRowViewModel>,
        occupancyDetails: List<SummaryListRowViewModel>,
    ): Map<String, Any?> =
        mapOf(
            "aboutPropertyHeadingKey" to "forms.checkPropertyAnswers.aboutYourProperty.heading",
            "correspondenceRows" to getCorrespondenceRows(state),
            "rentedOutHeadingKey" to "forms.checkPropertyAnswers.rentedOut.heading",
            "rentedOutLicensingHeadingKey" to "licensing.checkAnswers.heading",
            "rentedOutGasHeadingKey" to "checkGasSafety.heading",
            "rentedOutElectricalHeadingKey" to "checkElectricalSafety.heading",
            "rentedOutEpcHeadingKey" to "propertyCompliance.epcTask.checkEpcAnswers.heading",
            "rentedOutTenancyHeadingKey" to "tenancyDetails.checkAnswers.heading",
            "rentedOutLicensingRows" to licensingDetails,
            "rentedOutTenancyRows" to tenancyDetails,
            "occupancyDetails" to occupancyDetails,
            "tenancyUnoccupiedBodyTextKey" to if (!isOccupied) "tenancyDetails.checkAnswers.unoccupiedBodyText" else null,
        ) +
            getPeopleResponsibleForThisPropertyContent(state)

    private fun getPeopleResponsibleForThisPropertyContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val associatedPartiesEnabled = featureFlagManager.checkFeature(ASSOCIATED_PARTIES)
        val jointLandlordsTask =
            if (associatedPartiesEnabled) {
                state.ownershipAndLandlordsTask.jointLandlordsTask
            } else {
                state.ownershipAndLandlordsTaskBeforeAssociatedParties.jointLandlordsTask
            }
        val jointLandlordsRow = getJointLandLordsSummaryRow(state, jointLandlordsTask, associatedPartiesEnabled)
        val sectionHeadingKey =
            if (associatedPartiesEnabled) {
                "forms.checkPropertyAnswers.peopleResponsibleForThisProperty.heading"
            } else {
                "forms.checkPropertyAnswers.ownershipAndLandlords.heading"
            }

        val sectionContent =
            if (associatedPartiesEnabled) {
                mapOf(
                    "jointLandlordsRows" to listOf(jointLandlordsRow),
                ) +
                    ownershipStructureDetailsHelper.getOwnershipStructureCyaContent(
                        state,
                        state.ownershipAndLandlordsTask.ownershipStructureTask,
                    ) +
                    getPropertyManagerContent(state)
            } else {
                // TODO PDJB-2016: Remove this branch when ASSOCIATED_PARTIES is removed
                mapOf(
                    "peopleResponsibleForThisPropertyRows" to
                        listOf(getOwnershipTypeRowBeforeAssociatedParties(state), jointLandlordsRow),
                )
            }

        return mapOf("peopleResponsibleForThisPropertyHeadingKey" to sectionHeadingKey) + sectionContent
    }

    private fun getPropertyManagerContent(state: PropertyRegistrationJourneyState): Map<String, Any?> {
        val propertyManagerTask = state.ownershipAndLandlordsTask.propertyManagerTask
        val propertyManagerStep = propertyManagerTask.propertyManagerStep
        val usingPropertyManager =
            propertyManagerStep.formModelIfReachableOrNull?.usingPropertyManager
                ?: throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Check your answers was reached before the property manager step was answered",
                )

        return if (usingPropertyManager) {
            val checkPropertyManagerStep = propertyManagerTask.checkPropertyManagerStep
            mapOf(
                "propertyManagerChangeUrl" to
                    Destination
                        .VisitableStep(checkPropertyManagerStep, state.getCyaJourneyId(checkPropertyManagerStep))
                        .toUrlStringOrNull(),
                "propertyManagerCards" to
                    listOf(propertyManagerSummaryCardHelper.getSummaryCard(propertyManagerTask.propertyManagerDetailsStep.formModel)),
            )
        } else {
            mapOf(
                "propertyManagerRows" to
                    listOf(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "registerProperty.propertyManager.usingPropertyManager.fieldSetHeading",
                            "registerProperty.propertyManager.usingPropertyManager.radios.no",
                            Destination.VisitableStep(propertyManagerStep, state.getCyaJourneyId(propertyManagerStep)),
                        ),
                    ),
            )
        }
    }

    private fun getCorrespondenceRows(state: PropertyRegistrationJourneyState): List<SummaryListRowViewModel> {
        val emailStep = state.correspondenceTask.correspondenceEmailStep
        val email = emailStep.formModel.getEmailAddress { state.loggedInLandlordEmailAtStartOfJourney }
        val addressTask = state.correspondenceTask.addressTask

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.correspondence.emailAddress",
                email,
                Destination.VisitableStep(emailStep, state.getCyaJourneyId(emailStep)),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.correspondence.postalAddress",
                addressTask.getAddress().toMultiLineAddress().split("\n"),
                Destination.VisitableStep(addressTask.lookupAddressStep, state.getCyaJourneyId(addressTask.lookupAddressStep)),
            ),
        )
    }

    private fun getTenancyDetails(state: PropertyRegistrationJourneyState) =
        occupancyDetailsHelper.getCheckYourAnswersSummaryList(
            state,
            messageSource,
            Destination.VisitableStep(
                state.tenancyDetailsTask.householdsAndTenantsTask.households,
                state.getCyaJourneyId(state.tenancyDetailsTask.householdsAndTenantsTask.provideTenancyDetailsLaterStep),
            ),
        )

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun hasJointLandlords(jointLandlordsTask: JointLandlordsPropertyRegistrationTask): Boolean =
        jointLandlordsTask.hasJointLandlordsStep.formModel.notNullValue(
            HasJointLandlordsFormModel::hasJointLandlords,
        )

    private fun getJointLandLordsSummaryRow(
        state: PropertyRegistrationJourneyState,
        jointLandlordsTask: JointLandlordsPropertyRegistrationTask,
        associatedPartiesEnabled: Boolean,
    ): SummaryListRowViewModel {
        return if (hasJointLandlords(jointLandlordsTask)) {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                // TODO PDJB-2016: Remove the flag-off message when ASSOCIATED_PARTIES is removed
                if (associatedPartiesEnabled) {
                    "forms.checkPropertyAnswers.jointLandlordsDetails.invitations"
                } else {
                    "forms.checkPropertyAnswers.jointLandlordsDetails.jointLandlordInvitations"
                },
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

    private fun getPropertyDetailsSummaryList(state: PropertyRegistrationJourneyState) =
        getAddressRows(state) +
            getPropertyTypeRow(state) +
            getBedroomsRow(state)

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
                    "propertyDetails.propertyRecord.propertyDetails.address",
                    address.toMultiLineAddress().split("\n"),
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

    // TODO PDJB-2016: remove once ASSOCIATED_PARTIES is released
    private fun getOwnershipTypeRowBeforeAssociatedParties(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val ownershipTypeStep = state.ownershipAndLandlordsTaskBeforeAssociatedParties.ownershipTypeStep
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "propertyDetails.propertyRecord.ownership.ownershipType",
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
}

@JourneyFrameworkComponent
final class PropertyRegistrationCyaStep(
    stepConfig: PropertyRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}
