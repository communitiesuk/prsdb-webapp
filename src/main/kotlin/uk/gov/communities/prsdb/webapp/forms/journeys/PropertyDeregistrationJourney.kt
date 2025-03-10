package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.getPropertyDetailsPath
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.PropertyDeregistrationJourneyDataHelper.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<DeregisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_DEREGISTRATION,
        // TODO: PRSD-696 - check how we actually want to get this
        journeyDataKey = "${DEREGISTER_PROPERTY_JOURNEY_URL}_1",
        initialStepId = DeregisterPropertyStepId.AreYouSure,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    // TODO: PRSD-696 - get this from journeyPathSegment
    private val propertyOwnershipId = 1.toLong()

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                areYouSureStep(),
                reasonStep(),
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
                            "title" to "deregisterProperty.title",
                            "fieldSetHeading" to "deregisterProperty.areYouSure.fieldSetHeading",
                            // TODO: PRSD-696 - add the actual address here
                            "propertyAddress" to "HARDCODED ADDRESS",
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        labelMsgKey = "forms.radios.option.yes.label",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        labelMsgKey = "forms.radios.option.no.label",
                                    ),
                                ),
                            "backUrl" to getPropertyDetailsPath(propertyOwnershipId),
                        ),
                ),
            // handleSubmitAndRedirect is what will execute
            handleSubmitAndRedirect = { newJourneyData, subPage -> continueToNextActionOrExitJourney(newJourneyData, subPage) },
            // We need this nextAction to make the next step reachable!
            nextAction = { _, _ -> Pair(DeregisterPropertyStepId.Reason, null) },
        )

    private fun reasonStep() =
        Step(
            id = DeregisterPropertyStepId.Reason,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                        ),
                ),
        )

    private fun continueToNextActionOrExitJourney(
        newJourneyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        val currentStep = areYouSureStep()

        if (getWantsToProceed(newJourneyData)!!) {
            return getRedirectForNextStep(currentStep, newJourneyData, subPageNumber)
        }

        return getPropertyDetailsPath(propertyOwnershipId)
    }
}
