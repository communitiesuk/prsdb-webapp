package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper as DataHelper

class PropertyRegistrationCheckAnswersPage(
    private val addressDataService: AddressDataService,
    private val localAuthorityService: LocalAuthorityService,
) : Page(
        NoInputFormModel::class,
        "forms/propertyRegistrationCheckAnswersForm",
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        ),
    ) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        journeyData!!
        val propertyName = getPropertyName(journeyData)
        val propertyDetails = getPropertyDetailsSummary(journeyData)

        model.addAttribute("propertyDetails", propertyDetails)
        model.addAttribute("propertyName", propertyName)
        model.addAttribute("showUprnDetail", !DataHelper.isManualAddressChosen(journeyData))
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)
    }

    private fun getPropertyName(journeyData: JourneyData) = DataHelper.getAddress(journeyData, addressDataService)!!.singleLineAddress

    private fun getPropertyDetailsSummary(journeyData: JourneyData): List<SummaryListRowViewModel> =
        getAddressDetails(journeyData) +
            getPropertyTypeDetails(journeyData) +
            getOwnershipTypeDetails(journeyData) +
            getLicensingTypeDetails(journeyData) +
            getTenancyDetails(journeyData) +
            getLandlordTypeDetails(journeyData)

    private fun getAddressDetails(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val address = DataHelper.getAddress(journeyData, addressDataService)!!
        return if (DataHelper.isManualAddressChosen(journeyData)) {
            getManualAddressDetails(address)
        } else {
            getSelectedAddressDetails(address)
        }
    }

    private fun getSelectedAddressDetails(address: AddressDataModel): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                address.singleLineAddress,
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.uprn",
                address.uprn,
                null,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId!!).name,
                null,
            ),
        )

    private fun getManualAddressDetails(address: AddressDataModel): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                address.singleLineAddress,
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthorityService.retrieveLocalAuthorityById(address.localAuthorityId!!).name,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
            ),
        )

    private fun getPropertyTypeDetails(journeyData: JourneyData): SummaryListRowViewModel {
        val propertyType = DataHelper.getPropertyType(journeyData)!!
        val customType = DataHelper.getCustomPropertyType(journeyData)
        return SummaryListRowViewModel(
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

    private fun getOwnershipTypeDetails(journeyData: JourneyData) =
        SummaryListRowViewModel(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            DataHelper.getOwnershipType(journeyData)!!,
            RegisterPropertyStepId.OwnershipType.urlPathSegment,
        )

    private fun getLicensingTypeDetails(journeyData: JourneyData): SummaryListRowViewModel {
        val licensingType = DataHelper.getLicensingType(journeyData)!!
        val licenceNumber = DataHelper.getLicenseNumber(journeyData)!!
        val licensingSummaryValue = getLicensingSummaryValue(licenceNumber, licensingType)
        return SummaryListRowViewModel(
            "forms.checkPropertyAnswers.propertyDetails.licensing",
            licensingSummaryValue,
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

    private fun getTenancyDetails(journeyData: JourneyData): List<SummaryListRowViewModel> {
        val occupied = DataHelper.getIsOccupied(journeyData)!!
        return if (occupied) {
            getOccupyingTenantsDetails(journeyData)
        } else {
            listOf(
                SummaryListRowViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    RegisterPropertyStepId.Occupancy.urlPathSegment,
                ),
            )
        }
    }

    private fun getLandlordTypeDetails(journeyData: JourneyData): SummaryListRowViewModel {
        val landlordType = DataHelper.getLandlordType(journeyData)!!
        return SummaryListRowViewModel(
            "forms.checkPropertyAnswers.propertyDetails.landlordType",
            landlordType,
            RegisterPropertyStepId.LandlordType.urlPathSegment,
        )
    }

    private fun getOccupyingTenantsDetails(journeyData: JourneyData): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                true,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                DataHelper.getNumberOfHouseholds(journeyData),
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            ),
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.people",
                DataHelper.getNumberOfTenants(journeyData),
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            ),
        )
}
