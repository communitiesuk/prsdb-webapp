package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
interface JointLandlordsPropertyRegistrationStrategy {
    fun <T> ifEnabledOrElse(provider: IfEnabledConfig<T>.() -> Unit): T

    fun ifEnabled(action: () -> Unit)

    fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel>
}

@Primary
@PrsdbWebService("joint-landlords-property-registration-flag-off")
class JointLandlordsPropertyRegistrationStrategyImplFlagOff : JointLandlordsPropertyRegistrationStrategy {
    override fun <T> ifEnabledOrElse(provider: IfEnabledConfig<T>.() -> Unit): T {
        val config = IfEnabledConfig<T>()
        config.provider()
        return config.ifDisabledProvider!!()
    }

    override fun ifEnabled(action: () -> Unit) {}

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> = emptyList()
}

@PrsdbWebService("joint-landlords-property-registration-flag-on")
class JointLandlordsPropertyRegistrationStrategyImplFlagOn : JointLandlordsPropertyRegistrationStrategy {
    override fun <T> ifEnabledOrElse(provider: IfEnabledConfig<T>.() -> Unit): T {
        val config = IfEnabledConfig<T>()
        config.provider()
        return config.ifEnabledProvider!!()
    }

    override fun ifEnabled(action: () -> Unit) {
        action()
    }

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> =
        listOf(
            TaskListItemViewModel.fromTask(
                "registerProperty.taskList.register.inviteJointLandlords",
                state.jointLandlordsTask,
                "registerProperty.taskList.register.inviteJointLandlords.hint",
            ),
        )
}

class IfEnabledConfig<T> {
    internal var ifEnabledProvider: (() -> T)? = null
    internal var ifDisabledProvider: (() -> T)? = null

    fun ifEnabled(provider: () -> T) {
        ifEnabledProvider = provider
    }

    fun ifDisabled(provider: () -> T) {
        ifDisabledProvider = provider
    }
}
