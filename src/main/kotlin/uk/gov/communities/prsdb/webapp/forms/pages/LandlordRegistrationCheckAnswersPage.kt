package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LandlordRegistrationCheckAnswersPage(
    journeyDataService: JourneyDataService,
) : CheckAnswersPage(
        content =
            mapOf(
                "title" to "registerAsALandlord.title",
                "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
                "submitButtonText" to "forms.buttons.confirmAndContinue",
            ),
        journeyDataService = journeyDataService,
        shouldDisplaySectionHeader = true,
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

    private fun getAddressRows(filteredJourneyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>().apply {
            val livesInEnglandOrWales = LandlordRegistrationJourneyDataHelper.getLivesInEnglandOrWales(filteredJourneyData)!!
            add(getLivesInEnglandOrWalesRow(livesInEnglandOrWales))
            if (!livesInEnglandOrWales) addAll(getNonEnglandOrWalesAddressRows(filteredJourneyData))
            add(getContactAddressRow(filteredJourneyData, livesInEnglandOrWales))
        }

    private fun getLivesInEnglandOrWalesRow(livesInEnglandOrWales: Boolean) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
            livesInEnglandOrWales,
            LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
        )

    private fun getNonEnglandOrWalesAddressRows(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.countryOfResidence",
                LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesCountryOfResidence(journeyData)!!,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.nonEnglandOrWalesContactAddress",
                LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesAddress(journeyData)!!,
                LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment,
            ),
        )

    private fun getContactAddressRow(
        journeyData: JourneyData,
        livesInEnglandOrWales: Boolean,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            if (livesInEnglandOrWales) {
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress"
            } else {
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesContactAddress"
            },
            LandlordRegistrationJourneyDataHelper.getAddress(journeyData)!!.singleLineAddress,
            getContactAddressChangeURLPathSegment(journeyData, livesInEnglandOrWales),
        )

    private fun getContactAddressChangeURLPathSegment(
        journeyData: JourneyData,
        livesInEnglandOrWales: Boolean,
    ): String =
        if (livesInEnglandOrWales) {
            if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
                LandlordRegistrationStepId.ManualAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupAddress.urlPathSegment
            }
        } else {
            val isContactAddress = true
            if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData, isContactAddress)) {
                LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
            } else {
                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment
            }
        }
}
