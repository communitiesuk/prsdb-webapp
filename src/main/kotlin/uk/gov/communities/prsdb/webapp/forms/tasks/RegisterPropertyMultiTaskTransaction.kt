package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.stereotype.Service
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Service
class RegisterPropertyMultiTaskTransaction(
    journeyDataService: JourneyDataService,
    propertyRegistrationJourney: PropertyRegistrationJourney,
    validator: Validator,
) : MultiTaskTransaction<RegisterPropertyStepId>(journeyDataService) {
    public override val taskLists =
        listOf(
            TransactionSection(
                PropertyRegistrationSectionId.PROPERTY_DETAILS,
                RegisterPropertiesTaskList(propertyRegistrationJourney, journeyDataService, validator),
            ),
            TransactionSection(
                PropertyRegistrationSectionId.CHECK_AND_SUBMIT,
                CheckAndSubmitPropertiesTaskList(propertyRegistrationJourney, journeyDataService, validator),
            ),
        )
    override val journeyType: JourneyType = JourneyType.PROPERTY_REGISTRATION
    override val taskListUrlSegment: String = RegisterPropertyStepId.TaskList.urlPathSegment
}
