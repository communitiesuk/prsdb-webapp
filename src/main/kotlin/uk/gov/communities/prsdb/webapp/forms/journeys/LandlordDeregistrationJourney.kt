package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.CheckUserPropertiesPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.DeregistrationJourneyDataExtensions.Companion.getWantsToProceedLandlordDeregistration
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordWithNoPropertiesDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LandlordDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<DeregisterLandlordStepId>(
        journeyType = JourneyType.LANDLORD_DEREGISTRATION,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                checkForUserPropertiesStep(),
                areYouSureStep(),
            ),
        )

    private fun checkForUserPropertiesStep() =
        Step(
            id = DeregisterLandlordStepId.CheckForUserProperties,
            page = CheckUserPropertiesPage(),
            nextAction = { _, _ -> Pair(DeregisterLandlordStepId.AreYouSure, null) },
            saveAfterSubmit = false,
        )

    private fun areYouSureStep() =
        Step(
            id = DeregisterLandlordStepId.AreYouSure,
            page =
                Page(
                    formModel = LandlordWithNoPropertiesDeregistrationAreYouSureFormModel::class,
                    templateName = "forms/areYouSureForm",
                    content =
                        mapOf(
                            "title" to "deregisterLandlord.title",
                            "fieldSetHeading" to "forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading",
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        valueStr = "yes",
                                        labelMsgKey = "forms.radios.option.yes.label",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        valueStr = "no",
                                        labelMsgKey = "forms.radios.option.no.label",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to LANDLORD_DETAILS_PATH_SEGMENT,
                        ),
                ),
            handleSubmitAndRedirect = { newJourneyData, _ -> areYouSureContinueOrExitJourney(newJourneyData) },
            saveAfterSubmit = false,
        )

    private fun areYouSureContinueOrExitJourney(journeyData: JourneyData): String {
        if (journeyData.getWantsToProceedLandlordDeregistration()!!) {
            return deregisterLandlord()
        }
        return "/$LANDLORD_DETAILS_PATH_SEGMENT"
    }

    private fun deregisterLandlord(): String {
        // TODO: PRSD-703

        // TODO: PRSD-705 - redirect to confirmation page
        return "/${REGISTER_LANDLORD_JOURNEY_URL}"
    }

    companion object {
        val initialStepId = DeregisterLandlordStepId.CheckForUserProperties
    }
}
