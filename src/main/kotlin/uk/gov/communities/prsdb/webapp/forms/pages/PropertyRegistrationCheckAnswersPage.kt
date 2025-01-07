package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FormSummaryViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper as DataHelper

class PropertyRegistrationCheckAnswersPage(
    private val addressDataService: AddressDataService,
    private val journeyDataService: JourneyDataService,
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
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)
    }

    private fun getPropertyName(journeyData: JourneyData) =
        DataHelper.getAddress(journeyDataService, journeyData, addressDataService)!!.singleLineAddress

    private fun getPropertyDetailsSummary(journeyData: JourneyData): List<FormSummaryViewModel> =
        getAddressDetails(journeyData) +
            getPropertyTypeDetails(journeyData) +
            getOwnershipTypeDetails(journeyData) +
            getLicensingTypeDetails(journeyData) +
            getTenancyDetails(journeyData) +
            getLandlordTypeDetails(journeyData)

    private fun getAddressDetails(journeyData: JourneyData): List<FormSummaryViewModel> {
        val address = DataHelper.getAddress(journeyDataService, journeyData, addressDataService)!!
        return if (DataHelper.isManualAddressChosen(journeyDataService, journeyData)) {
            val custodianCode = DataHelper.getCustodianCode(journeyDataService, journeyData)!!
            getManualAddressDetails(address.singleLineAddress, custodianCode)
        } else {
            getSelectedAddressDetails(address)
        }
    }

    private fun getSelectedAddressDetails(address: AddressDataModel): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                address.singleLineAddress,
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.uprn",
                address.uprn,
                null,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                getLocalAuthority(address.custodianCode).displayName,
                null,
            ),
        )

    private fun getManualAddressDetails(
        singleLineAddress: String,
        custodianCode: String,
    ): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                singleLineAddress,
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                getLocalAuthority(custodianCode).displayName,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
            ),
        )

    private fun getPropertyTypeDetails(journeyData: JourneyData): FormSummaryViewModel {
        val propertyType = DataHelper.getPropertyType(journeyDataService, journeyData)!!
        val customType = DataHelper.getCustomPropertyType(journeyDataService, journeyData)
        return FormSummaryViewModel(
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
        FormSummaryViewModel(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            DataHelper.getOwnershipType(journeyDataService, journeyData)!!,
            RegisterPropertyStepId.OwnershipType.urlPathSegment,
        )

    private fun getLicensingTypeDetails(journeyData: JourneyData): FormSummaryViewModel {
        val licensingType = DataHelper.getLicensingType(journeyDataService, journeyData)!!
        val licenceNumber = DataHelper.getLicenseNumber(journeyDataService, journeyData)!!
        val licensingSummaryValue = getLicensingSummaryValue(licenceNumber, licensingType)
        return FormSummaryViewModel(
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

    private fun getTenancyDetails(journeyData: JourneyData): List<FormSummaryViewModel> {
        val occupied = DataHelper.getIsOccupied(journeyDataService, journeyData)!!
        return if (occupied) {
            getOccupyingTenantsDetails(journeyData)
        } else {
            listOf(
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    RegisterPropertyStepId.Occupancy.urlPathSegment,
                ),
            )
        }
    }

    private fun getLandlordTypeDetails(journeyData: JourneyData): FormSummaryViewModel {
        val landlordType = DataHelper.getLandlordType(journeyDataService, journeyData)!!
        return FormSummaryViewModel(
            "forms.checkPropertyAnswers.propertyDetails.landlordType",
            landlordType,
            RegisterPropertyStepId.LandlordType.urlPathSegment,
        )
    }

    private fun getLocalAuthority(custodianCode: String?) = LOCAL_AUTHORITIES.single { it.custodianCode == custodianCode }

    private fun getOccupyingTenantsDetails(journeyData: JourneyData): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                true,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                DataHelper.getNumberOfHouseholds(journeyDataService, journeyData)!!,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.people",
                DataHelper.getNumberOfTenants(journeyDataService, journeyData)!!,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            ),
        )
}
