package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordRegistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class IndividualLandlordRegistrationCyaStepConfig(
    private val landlordRegistrationService: LandlordRegistrationService,
    private val securityContextService: SecurityContextService,
) : AbstractCheckYourAnswersStepConfig<LandlordRegistrationState>() {
    override fun getStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "insetText" to false,
            "summaryListData" to getSummaryList(state),
        )

    override fun afterStepDataIsAdded(state: LandlordRegistrationState) {
        landlordRegistrationService.registerIndividualLandlord(
            baseUserId = SecurityContextHolder.getContext().authentication.name,
            name = state.identityTask.getName(),
            email =
                state.emailStep.formModel
                    .notNullValue(EmailFormModel::emailAddress),
            phoneNumber =
                state.phoneNumberStep.formModel.notNullValue(
                    PhoneNumberFormModel::phoneNumber,
                ),
            address = state.individualLandlordLocationTask.addressTask.getAddress(),
            countryOfResidence = ENGLAND_OR_WALES,
            isVerified = state.identityTask.getIsIdentityVerified(),
            hasAcceptedPrivacyNotice = state.privacyNoticeStep.formModel.notNullValue(PrivacyNoticeFormModel::agreesToPrivacyNotice),
            nonEnglandOrWalesAddress = null,
            dateOfBirth = state.identityTask.getDateOfBirth(),
        )

        securityContextService.refreshContext()
    }

    // Overrides AbstractCheckYourAnswersStepConfig, which deleted the journey
    // We don't want to delete the journey at this stage when this page is included within another journey,
    // such as accepting a joint landlord invitation
    override fun resolveNextDestination(
        state: LandlordRegistrationState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getSummaryList(state: LandlordRegistrationState) =
        getIdentityRows(state) +
            getEmailAndPhoneRows(state) +
            getAddressRows(state)

    private fun getIdentityRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> {
        val isIdentityVerified = state.identityTask.getIsIdentityVerified()
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                state.identityTask.getName(),
                if (isIdentityVerified) {
                    Destination.Nowhere()
                } else {
                    Destination.VisitableStep(state.identityTask.nameStep, state.getCyaJourneyId(state.identityTask.nameStep))
                },
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                state.identityTask.getDateOfBirth(),
                if (isIdentityVerified) {
                    Destination.Nowhere()
                } else {
                    Destination.VisitableStep(
                        state.identityTask.dateOfBirthStep,
                        state.getCyaJourneyId(state.identityTask.dateOfBirthStep),
                    )
                },
            ),
        )
    }

    private fun getEmailAndPhoneRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                state.emailStep.formModel
                    .notNullValue(EmailFormModel::emailAddress),
                Destination.VisitableStep(
                    state.emailStep,
                    state.getCyaJourneyId(state.emailStep),
                ),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                state.phoneNumberStep.formModel
                    .notNullValue(PhoneNumberFormModel::phoneNumber),
                Destination.VisitableStep(
                    state.phoneNumberStep,
                    state.getCyaJourneyId(state.phoneNumberStep),
                ),
            ),
        )

    private fun getAddressRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                state.individualLandlordLocationTask.countryOfResidenceStep.formModel.notNullValue(
                    CountryOfResidenceFormModel::livesInEnglandOrWales,
                ),
                Destination.VisitableStep(
                    state.individualLandlordLocationTask.countryOfResidenceStep,
                    state.getCyaJourneyId(state.individualLandlordLocationTask.countryOfResidenceStep),
                ),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                state.individualLandlordLocationTask.addressTask
                    .getAddress()
                    .singleLineAddress,
                Destination.VisitableStep(
                    state.individualLandlordLocationTask.addressTask.lookupAddressStep,
                    state.getCyaJourneyId(state.individualLandlordLocationTask.addressTask.lookupAddressStep),
                ),
            ),
        )
}

@JourneyFrameworkComponent
final class IndividualLandlordRegistrationCyaStep(
    stepConfig: IndividualLandlordRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LandlordRegistrationState>(stepConfig)
