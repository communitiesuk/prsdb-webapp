package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationCheckUserPropertiesPage
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.DeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.DeregistrationJourneyDataExtensions.Companion.getWantsToProceedLandlordDeregistration
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordWithNoPropertiesDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

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
            page = LandlordDeregistrationCheckUserPropertiesPage(),
            nextAction = { _, _ -> Pair(DeregisterLandlordStepId.AreYouSure, null) },
            saveAfterSubmit = false,
        )

    private fun areYouSureStep() =
        Step(
            id = DeregisterLandlordStepId.AreYouSure,
            page =
                LandlordDeregistrationAreYouSurePage(
                    commonContent =
                        mapOf(
                            "title" to "deregisterLandlord.title",
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
                ) { getAreYouSureFormModel() },
            handleSubmitAndRedirect = { newJourneyData, _ -> areYouSureContinueOrExitJourney(newJourneyData) },
            saveAfterSubmit = false,
        )

    // TODO: PRSD-703 - check if the user has registered properties and return this version if they have none
    // TODO: PRSD-705 - return a "with properties" version if the user has registered properties
    private fun getAreYouSureFormModel(): KClass<out FormModel> = LandlordWithNoPropertiesDeregistrationAreYouSureFormModel::class

    private fun areYouSureContinueOrExitJourney(journeyData: JourneyData): String {
        if (journeyData.getWantsToProceedLandlordDeregistration()!!) {
            if (!journeyData.getLandlordUserHasRegisteredProperties()!!) {
                return deregisterLandlord()
            }
            // TODO: PRSD-704 - continue to reason page if user has registered properties
        }
        return "/$LANDLORD_DETAILS_PATH_SEGMENT"
    }

    private fun deregisterLandlord(): String {
        // TODO: PRSD-703 - implement this
        //      delete from landlord table
        //      delete from one-login table (check if they are another type of user first)
        //      refresh user roles to remove landlord permissions

        // TODO: PRSD-705 - redirect to confirmation page
        return "/${REGISTER_LANDLORD_JOURNEY_URL}"
    }

    companion object {
        val initialStepId = DeregisterLandlordStepId.CheckForUserProperties
    }
}
