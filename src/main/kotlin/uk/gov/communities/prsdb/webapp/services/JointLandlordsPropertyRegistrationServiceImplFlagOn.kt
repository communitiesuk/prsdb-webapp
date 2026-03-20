package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.services.interfaces.JointLandlordsPropertyRegistrationService

@PrsdbWebService("joint-landlords-property-registration-flag-on")
class JointLandlordsPropertyRegistrationServiceImplFlagOn : JointLandlordsPropertyRegistrationService {
    override fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*> = state.jointLandlordsTask

    override fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *> = state.jointLandlordsTask.firstStep

    override fun addJointLandlordsJourneyTaskIfEnabled(addTask: () -> Unit) {
        addTask()
    }

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> =
        listOf(
            TaskListItemViewModel.fromTask(
                "registerProperty.taskList.register.inviteJointLandlords",
                state.jointLandlordsTask,
                "registerProperty.taskList.register.inviteJointLandlords.hint",
            ),
        )

    override fun addJointLandlordsCyaContent(
        state: PropertyRegistrationJourneyState,
        content: MutableMap<String, Any?>,
    ) {
        content["jointLandlordsDetails"] = getJointLandLordsSummaryRow(state)
    }

    override fun getJointLandlordEmailsForRegistration(state: PropertyRegistrationJourneyState): List<String>? =
        state.invitedJointLandlordEmailsMap?.values?.toList()

    private fun getJointLandLordsSummaryRow(state: PropertyRegistrationJourneyState): SummaryListRowViewModel {
        val hasJointLandlords = state.hasJointLandlordsStep.formModel.notNullValue(HasJointLandlordsFormModel::hasJointLandlords)
        return if (hasJointLandlords) {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.invitations",
                state.invitedJointLandlords,
                Destination(state.hasJointLandlordsStep),
            )
        } else {
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.areThereJointLandlords",
                "forms.checkPropertyAnswers.jointLandlordsDetails.noJointLandlords",
                Destination(state.hasJointLandlordsStep),
            )
        }
    }
}
