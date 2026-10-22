package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ChangeOrgTeamMemberAccessLevelFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@JourneyFrameworkComponent
class AccessLevelStepConfig(
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
) : AbstractRequestableStepConfig<
        Complete,
        ChangeOrgTeamMemberAccessLevelFormModel,
        ChangeOrgTeamMemberAccessLevelJourneyState,
        >() {
    override val formModelClass = ChangeOrgTeamMemberAccessLevelFormModel::class

    override fun getStepSpecificContent(state: ChangeOrgTeamMemberAccessLevelJourneyState): Map<String, Any?> {
        val member =
            organisationalLandlordUserService.getOrganisationalLandlordUserOrNull(state.otherOrganisationalLandlordUserId)
                ?: throw PrsdbWebException(
                    "No organisational landlord user found with id ${state.otherOrganisationalLandlordUserId}",
                )
        return mapOf(
            "memberName" to member.name,
            "currentLevelHintKey" to
                when (member.role) {
                    OrganisationalLandlordUserRole.ADMIN -> "changeOrgTeamMemberAccessLevel.hint.currentLevel.administrator"
                    OrganisationalLandlordUserRole.EDITOR -> "changeOrgTeamMemberAccessLevel.hint.currentLevel.editor"
                },
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "removeAccessUrl" to
                ChangeOrgTeamMemberAccessLevelController.getRemoveOrgUserPath(state.otherOrganisationalLandlordUserId),
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = OrganisationalLandlordUserRole.ADMIN,
                        labelMsgKey = "changeOrgTeamMemberAccessLevel.radios.option.administrator.label",
                        hintMsgKey = "changeOrgTeamMemberAccessLevel.radios.option.administrator.hint",
                    ),
                    RadiosButtonViewModel(
                        value = OrganisationalLandlordUserRole.EDITOR,
                        labelMsgKey = "changeOrgTeamMemberAccessLevel.radios.option.editor.label",
                        hintMsgKey = "changeOrgTeamMemberAccessLevel.radios.option.editor.hint",
                    ),
                ),
        )
    }

    override fun chooseTemplate(state: ChangeOrgTeamMemberAccessLevelJourneyState) = "forms/changeOrgTeamMemberAccessLevelForm"

    override fun mode(state: ChangeOrgTeamMemberAccessLevelJourneyState): Complete? =
        getFormModelFromStateOrNull(state)?.role?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class AccessLevelStep(
    stepConfig: AccessLevelStepConfig,
) : RequestableStep<
        Complete,
        ChangeOrgTeamMemberAccessLevelFormModel,
        ChangeOrgTeamMemberAccessLevelJourneyState,
        >(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "access-level"
    }
}
