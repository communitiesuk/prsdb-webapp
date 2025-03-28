package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.DEREGISTRATION_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationCheckUserPropertiesPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

class LandlordDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val securityContextService: SecurityContextService,
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
                reasonStep(),
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
                ),
            // handleSubmitAndRedirect will execute. It does not have to redirect to the step specified in nextAction.
            handleSubmitAndRedirect = { journeyData, subPage -> areYouSureContinueOrExitJourney(journeyData, subPage) },
            // This gets checked when determining whether the next step is reachable
            nextAction = { _, _ -> Pair(DeregisterLandlordStepId.Reason, null) },
            saveAfterSubmit = false,
        )

    private fun reasonStep() =
        Step(
            id = DeregisterLandlordStepId.Reason,
            page =
                Page(
                    formModel = LandlordDeregistrationReasonFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterLandlord.title",
                            "fieldSetHeading" to "forms.reason.landlordDeregistration.fieldSetHeading",
                            "limit" to DEREGISTRATION_REASON_MAX_LENGTH,
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> deregisterLandlordAndProperties() },
            saveAfterSubmit = false,
        )

    private fun areYouSureContinueOrExitJourney(
        journeyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        if (journeyData.getWantsToProceed()!!) {
            if (!journeyData.getLandlordUserHasRegisteredProperties()!!) {
                return deregisterLandlord()
            }
            val areYouSureStep = steps.single { it.id == DeregisterLandlordStepId.AreYouSure }
            return getRedirectForNextStep(areYouSureStep, journeyData, subPageNumber)
        }
        return "/$LANDLORD_DETAILS_PATH_SEGMENT"
    }

    private fun deregisterLandlordAndProperties(): String {
        // TODO: PRSD-891
        return "/${REGISTER_LANDLORD_JOURNEY_URL}"
    }

    private fun deregisterLandlord(): String {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        landlordDeregistrationService.deregisterLandlord(baseUserId)

        refreshUserRoles()

        // TODO: PRSD-705 - redirect to confirmation page
        return "/${REGISTER_LANDLORD_JOURNEY_URL}"
    }

    private fun refreshUserRoles() {
        securityContextService.refreshContext()
    }

    companion object {
        val initialStepId = DeregisterLandlordStepId.CheckForUserProperties
    }
}
