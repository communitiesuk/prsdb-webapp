package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.Destination.Nowhere
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.LandlordRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig2
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

enum class LandlordRegistrationCheckableElements {
    NAME_AND_DATE_OF_BIRTH,
    EMAIL_AND_PHONE_NUMBER,
    ADDRESS,
}

@JourneyFrameworkComponent
class LandlordRegistrationCyaStepConfig(
    private val landlordService: LandlordService,
    private val securityContextService: SecurityContextService,
) : AbstractCheckYourAnswersStepConfig2<LandlordRegistrationCheckableElements, LandlordRegistrationJourneyState>() {
    override fun getStepSpecificContent(state: LandlordRegistrationJourneyState): Map<String, Any?> {
        LandlordRegistrationCheckableElements.entries.forEach { checkableElement ->
            val newId = state.generateJourneyId("${checkableElement.name} for ${state.journeyId}")
            state.initialiseCyaChildJourney(newId, checkableElement)
        }

        return mapOf(
            "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "insetText" to true,
            "summaryListData" to getSummaryList(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )
    }

    override fun afterStepDataIsAdded(state: LandlordRegistrationJourneyState) {
        landlordService.createLandlord(
            baseUserId = SecurityContextHolder.getContext().authentication.name,
            name = state.getName(),
            email = state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress),
            phoneNumber = state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber),
            addressDataModel = state.getAddress(),
            countryOfResidence = ENGLAND_OR_WALES,
            isVerified = state.getIsIdentityVerified(),
            hasAcceptedPrivacyNotice = state.privacyNoticeStep.formModel.notNullValue(PrivacyNoticeFormModel::agreesToPrivacyNotice),
            nonEnglandOrWalesAddress = null,
            dateOfBirth = state.getDateOfBirth(),
        )

        securityContextService.refreshContext()
    }

    private fun getSummaryList(state: LandlordRegistrationJourneyState) =
        getIdentityRows(state) +
            getEmailAndPhoneRows(state) +
            getAddressRows(state)

    private fun getIdentityRows(state: LandlordRegistrationJourneyState): List<SummaryListRowViewModel> {
        val isIdentityVerified = state.getIsIdentityVerified()
        val identityJourneyId = state.getCyaJourneyId(LandlordRegistrationCheckableElements.NAME_AND_DATE_OF_BIRTH)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                state.getName(),
                if (isIdentityVerified) Nowhere() else Destination.VisitableStep(state.nameStep, identityJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                state.getDateOfBirth(),
                if (isIdentityVerified) Nowhere() else Destination.VisitableStep(state.dateOfBirthStep, identityJourneyId),
            ),
        )
    }

    private fun getEmailAndPhoneRows(state: LandlordRegistrationJourneyState): List<SummaryListRowViewModel> {
        val contactJourneyId = state.getCyaJourneyId(LandlordRegistrationCheckableElements.EMAIL_AND_PHONE_NUMBER)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress),
                Destination.VisitableStep(state.emailStep, contactJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber),
                Destination.VisitableStep(state.phoneNumberStep, contactJourneyId),
            ),
        )
    }

    private fun getAddressRows(state: LandlordRegistrationJourneyState): List<SummaryListRowViewModel> {
        val addressJourneyId = state.getCyaJourneyId(LandlordRegistrationCheckableElements.ADDRESS)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                state.countryOfResidenceStep.formModel.notNullValue(CountryOfResidenceFormModel::livesInEnglandOrWales),
                Destination.VisitableStep(state.countryOfResidenceStep, addressJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                state.getAddress().singleLineAddress,
                Destination.VisitableStep(state.lookupAddressStep, addressJourneyId),
            ),
        )
    }
}

@JourneyFrameworkComponent
final class LandlordRegistrationCyaStep(
    stepConfig: LandlordRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LandlordRegistrationCheckableElements, LandlordRegistrationJourneyState>(stepConfig)
