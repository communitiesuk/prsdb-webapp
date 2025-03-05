package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<DeregisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_DEREGISTRATION,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    final override val initialStepId = DeregisterPropertyStepId.AreYouSure

    override val journeyPathSegment = DEREGISTER_PROPERTY_JOURNEY_URL

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                areYouSureStep(),
            ),
        )

    private fun areYouSureStep() =
        Step(
            id = DeregisterPropertyStepId.AreYouSure,
            page =
                Page(
                    formModel = PropertyDeregistrationAreYouSureFormModel::class,
                    templateName = "forms/areYouSureForm",
                    content =
                        mapOf(
                            "title" to "deRegisterProperty.title",
                        ),
                ),
        )
}
