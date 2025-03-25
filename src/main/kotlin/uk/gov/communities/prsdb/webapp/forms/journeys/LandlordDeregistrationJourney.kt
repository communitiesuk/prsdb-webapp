package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationCheckUserPropertiesPage
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
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
                            BACK_URL_ATTR_NAME to "/$LANDLORD_DETAILS_PATH_SEGMENT",
                        ),
                    journeyDataService = journeyDataService,
                ) { getHeadingsAndHintsForAreYouSureStep() },
            handleSubmitAndRedirect = { journeyData, _ -> areYouSureContinueOrExitJourney(journeyData) },
            saveAfterSubmit = false,
        )

    fun getHeadingsAndHintsForAreYouSureStep(): Map<String, Any> {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val userHasRegisteredProperties =
            journeyData.getLandlordUserHasRegisteredProperties()
                ?: throw (
                    ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "userHasRegisteredProperties was not found in journeyData",
                    )
                )

        if (!userHasRegisteredProperties) {
            return mapOf(
                "fieldSetHeading" to "forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading",
            )
        }
        return mapOf(
            "fieldSetHeading" to "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHeading",
            "fieldSetHint" to "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHint",
        )
    }

    private fun areYouSureContinueOrExitJourney(journeyData: JourneyData): String {
        if (journeyData.getWantsToProceed()!!) {
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
