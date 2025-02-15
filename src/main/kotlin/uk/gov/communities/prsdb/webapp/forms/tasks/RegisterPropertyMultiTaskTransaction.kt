package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Service
class RegisterPropertyMultiTaskTransaction(
    journeyDataService: JourneyDataService,
    registerPropertiesTaskList: RegisterPropertiesTaskList,
    checkAndSubmitPropertiesTaskList: CheckAndSubmitPropertiesTaskList,
) : MultiTaskTransaction<RegisterPropertyStepId>(journeyDataService) {
    override val taskLists =
        listOf(
            TransactionSection("registerProperty.taskList.register.heading", registerPropertiesTaskList),
            TransactionSection("registerProperty.taskList.checkAndSubmit.heading", checkAndSubmitPropertiesTaskList),
        )
    override val journeyType: JourneyType = JourneyType.PROPERTY_REGISTRATION
    override val taskListUrlSegment: String = RegisterPropertyStepId.TaskList.urlPathSegment
}
