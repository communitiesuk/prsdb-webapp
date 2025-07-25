package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper as DataHelper

class PropertyRegistrationCheckAnswersPage(
    journeyDataService: JourneyDataService,
    private val localAuthorityService: LocalAuthorityService,
) : CheckAnswersPage(
        content =
            mapOf(
                "title" to "registerProperty.title",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
        journeyDataService = journeyDataService,
        templateName = "forms/propertyRegistrationCheckAnswersForm",
        shouldDisplaySectionHeader = true,
    ) {
    override fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("propertyName", getPropertyName(filteredJourneyData))
        modelAndView.addObject("propertyDetails", getPropertyDetailsSummaryList(filteredJourneyData))
        modelAndView.addObject("showUprnDetail", !DataHelper.isManualAddressChosen(filteredJourneyData))
    }

    private fun getPropertyName(filteredJourneyData: JourneyData) = DataHelper.getAddress(filteredJourneyData)!!.singleLineAddress

    private fun getPropertyDetailsSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> =
        getAddressRows(filteredJourneyData) +
            getPropertyTypeRow(filteredJourneyData) +
            getOwnershipTypeRow(filteredJourneyData) +
            getLicensingTypeRow(filteredJourneyData) +
            getTenancyRows(filteredJourneyData)

    private fun getAddressRows(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val address = DataHelper.getAddress(journeyData)!!
        return if (DataHelper.isManualAddressChosen(journeyData)) {
            getManualAddressRows(address)
        } else {
            getSelectedAddressRows(address)
        }
    }

    private fun getSelectedAddressRows(address: AddressDataModel): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.address",
                address.singleLineAddress,
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.uprn",
                address.uprn,
                null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId!!).name,
                null,
            ),
        )

    private fun getManualAddressRows(address: AddressDataModel): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.address",
                address.singleLineAddress,
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId!!).name,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
            ),
        )

    private fun getPropertyTypeRow(filteredJourneyData: JourneyData): SummaryListRowViewModel {
        val propertyType = DataHelper.getPropertyType(filteredJourneyData)!!
        val customType = DataHelper.getCustomPropertyType(filteredJourneyData)
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.type",
            getPropertyTypeSummaryValue(propertyType, customType),
            RegisterPropertyStepId.PropertyType.urlPathSegment,
        )
    }

    private fun getPropertyTypeSummaryValue(
        propertyType: PropertyType,
        customType: String?,
    ): Any =
        if (propertyType == PropertyType.OTHER) {
            listOf(propertyType, customType)
        } else {
            propertyType
        }

    private fun getOwnershipTypeRow(filteredJourneyData: JourneyData) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            DataHelper.getOwnershipType(filteredJourneyData)!!,
            RegisterPropertyStepId.OwnershipType.urlPathSegment,
        )

    private fun getLicensingTypeRow(journeyData: JourneyData): SummaryListRowViewModel {
        val licensingType = DataHelper.getLicensingType(journeyData)!!
        val licenceNumber = DataHelper.getLicenseNumber(journeyData)!!
        return SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.licensing",
            getLicensingSummaryValue(licenceNumber, licensingType),
            RegisterPropertyStepId.LicensingType.urlPathSegment,
        )
    }

    private fun getLicensingSummaryValue(
        licenceNumber: String?,
        licensingType: LicensingType,
    ): Any =
        if (licensingType != LicensingType.NO_LICENSING) {
            listOf(licensingType, licenceNumber)
        } else {
            licensingType
        }

    private fun getTenancyRows(journeyData: JourneyData) =
        mutableListOf<SummaryListRowViewModel>().apply {
            val occupied = DataHelper.getIsOccupied(journeyData)!!
            add(getOccupancyStatusRow(occupied))
            if (occupied) addAll(getOccupyingTenantsRows(journeyData))
        }

    private fun getOccupancyStatusRow(occupied: Boolean) =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "forms.checkPropertyAnswers.propertyDetails.occupied",
            occupied,
            RegisterPropertyStepId.Occupancy.urlPathSegment,
        )

    private fun getOccupyingTenantsRows(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.households",
                DataHelper.getNumberOfHouseholds(journeyData),
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.propertyDetails.people",
                DataHelper.getNumberOfTenants(journeyData),
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            ),
        )
}
