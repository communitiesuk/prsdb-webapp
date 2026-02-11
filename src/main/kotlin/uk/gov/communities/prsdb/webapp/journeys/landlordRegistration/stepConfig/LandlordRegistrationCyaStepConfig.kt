package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.Destination.Nowhere
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.LandlordRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class LandlordRegistrationCyaStepConfig(
    private val landlordService: LandlordService,
    private val securityContextService: SecurityContextService,
) : AbstractCheckYourAnswersStepConfig<LandlordRegistrationJourneyState>() {
    override fun getStepSpecificContent(state: LandlordRegistrationJourneyState) =
        mapOf(
            "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "insetText" to true,
            "summaryListData" to getSummaryList(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

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

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                state.getName(),
                if (isIdentityVerified) Nowhere() else Destination.VisitableStep(state.nameStep, childJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                state.getDateOfBirth(),
                if (isIdentityVerified) Nowhere() else Destination.VisitableStep(state.dateOfBirthStep, childJourneyId),
            ),
        )
    }

    private fun getEmailAndPhoneRows(state: LandlordRegistrationJourneyState) =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress),
                Destination.VisitableStep(state.emailStep, childJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber),
                Destination.VisitableStep(state.phoneNumberStep, childJourneyId),
            ),
        )

    private fun getAddressRows(state: LandlordRegistrationJourneyState) =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                state.countryOfResidenceStep.formModel.notNullValue(CountryOfResidenceFormModel::livesInEnglandOrWales),
                Destination.VisitableStep(state.countryOfResidenceStep, childJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                state.getAddress().singleLineAddress,
                Destination.VisitableStep(state.lookupAddressStep, childJourneyId),
            ),
        )
}

@JourneyFrameworkComponent
final class LandlordRegistrationCyaStep(
    stepConfig: LandlordRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LandlordRegistrationJourneyState>(stepConfig)
