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
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    final override val initialStepId = DeregisterPropertyStepId.AreYouSure

    // TODO: PRSD-696 - get this from journeyPathSegment
    private val propertyOwnershipId = 1.toLong()

    // TODO: PRSD-696 - Check how this is going to work after refactor - is this getting passed in in the factory and how?
    override val journeyPathSegment = "$DEREGISTER_PROPERTY_JOURNEY_URL/$propertyOwnershipId"

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
