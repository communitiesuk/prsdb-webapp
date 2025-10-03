package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LandlordRegistrationCheckAnswersPage(
    journeyDataService: JourneyDataService,
    missingAnswersRedirect: String,
) : BasicCheckAnswersPage(
        content =
            mapOf(
                "title" to "registerAsALandlord.title",
                "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
                "showWarning" to true,
                "submitButtonText" to "forms.buttons.confirmAndContinue",
            ),
        journeyDataService = journeyDataService,
        shouldDisplaySectionHeader = true,
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    override fun getSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        getIdentityRows(filteredJourneyData) +
            getEmailAndPhoneRows(filteredJourneyData) +
            getAddressRows(filteredJourneyData)

    private fun getIdentityRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> {
        val isIdentityVerified = LandlordRegistrationJourneyDataHelper.isIdentityVerified(filteredJourneyData)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                LandlordRegistrationJourneyDataHelper.getName(filteredJourneyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.Name.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                LandlordRegistrationJourneyDataHelper.getDOB(filteredJourneyData)!!,
                if (isIdentityVerified) null else LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
            ),
        )
    }

    private fun getEmailAndPhoneRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                LandlordRegistrationJourneyDataHelper.getEmail(filteredJourneyData)!!,
                LandlordRegistrationStepId.Email.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                LandlordRegistrationJourneyDataHelper.getPhoneNumber(filteredJourneyData)!!,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            ),
        )

    private fun getAddressRows(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                LandlordRegistrationJourneyDataHelper.getLivesInEnglandOrWales(filteredJourneyData)!!,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                LandlordRegistrationJourneyDataHelper.getAddress(filteredJourneyData)!!.singleLineAddress,
                getContactAddressChangeURLPathSegment(filteredJourneyData),
            ),
        )

    private fun getContactAddressChangeURLPathSegment(journeyData: JourneyData): String =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
            LandlordRegistrationStepId.ManualAddress.urlPathSegment
        } else {
            LandlordRegistrationStepId.LookupAddress.urlPathSegment
        }
}
