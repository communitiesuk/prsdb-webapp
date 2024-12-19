package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.FormSummaryDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
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
        DataHelper.getAddress(journeyDataService, journeyData, addressDataService)?.singleLineAddress

    private fun getPropertyDetailsSummary(journeyData: JourneyData): List<FormSummaryDataModel> =
        getAddressDetails(journeyData) +
            getPropertyTypeDetails(journeyData) +
            getOwnershipTypeDetails(journeyData) +
            getLicensingTypeDetails(journeyData) +
            getTenancyDetails(journeyData) +
            getLandlordTypeDetails(journeyData)

    private fun getAddressDetails(journeyData: JourneyData): List<FormSummaryDataModel> {
        val address = DataHelper.getAddress(journeyDataService, journeyData, addressDataService)
        val custodianCode = address?.custodianCode ?: DataHelper.getCustodianCode(journeyDataService, journeyData)
        return listOfNotNull(
            FormSummaryDataModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                address?.singleLineAddress,
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
            ),
            address?.uprn?.let {
                // Only include the UPRN summary if the UPRN is present
                FormSummaryDataModel(
                    "forms.checkPropertyAnswers.propertyDetails.uprn",
                    address.uprn,
                    null,
                )
            },
            FormSummaryDataModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                getLocalAuthority(custodianCode).displayName,
                getChangeLocalAuthorityUrl(journeyData),
            ),
        )
    }

    private fun getChangeLocalAuthorityUrl(journeyData: JourneyData) =
        if (DataHelper.isManualAddressChosen(journeyDataService, journeyData)) {
            RegisterPropertyStepId.LocalAuthority.urlPathSegment
        } else {
            null
        }

    private fun getPropertyTypeDetails(journeyData: JourneyData): FormSummaryDataModel {
        val propertyType = DataHelper.getPropertyType(journeyDataService, journeyData)!!
        val customType = DataHelper.getCustomPropertyType(journeyDataService, journeyData)
        return FormSummaryDataModel(
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
        FormSummaryDataModel(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            DataHelper.getOwnershipType(journeyDataService, journeyData),
            RegisterPropertyStepId.OwnershipType.urlPathSegment,
        )

    private fun getLicensingTypeDetails(journeyData: JourneyData): FormSummaryDataModel {
        val licensingType = DataHelper.getLicensingType(journeyDataService, journeyData)!!
        val licenceNumber = getLicenceNumberOfType(licensingType, journeyData)
        val licensingSummaryValue = getLicensingSummaryValue(licenceNumber, licensingType)
        return FormSummaryDataModel(
            "forms.checkPropertyAnswers.propertyDetails.licensing",
            licensingSummaryValue,
            RegisterPropertyStepId.LicensingType.urlPathSegment,
        )
    }

    private fun getLicensingSummaryValue(
        licenceNumber: String?,
        licensingType: LicensingType,
    ): Any =
        if (licenceNumber != null) {
            listOf(licensingType, licenceNumber)
        } else {
            licensingType
        }

    private fun getLicenceNumberOfType(
        licensingType: LicensingType,
        journeyData: JourneyData,
    ) = when (licensingType) {
        LicensingType.SELECTIVE_LICENCE ->
            DataHelper.getLicenseNumber(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.SelectiveLicence.urlPathSegment,
            )
        LicensingType.HMO_MANDATORY_LICENCE ->
            DataHelper.getLicenseNumber(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment,
            )
        LicensingType.HMO_ADDITIONAL_LICENCE ->
            DataHelper.getLicenseNumber(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment,
            )
        LicensingType.NO_LICENSING -> null
    }

    private fun getTenancyDetails(journeyData: JourneyData): List<FormSummaryDataModel> {
        val occupied = DataHelper.getIsOccupied(journeyDataService, journeyData)!!
        if (occupied) {
            return getOccupyingTenantsDetails(journeyData)
        } else {
            return listOf(
                FormSummaryDataModel(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    RegisterPropertyStepId.Occupancy.urlPathSegment,
                ),
            )
        }
    }

    private fun getLandlordTypeDetails(journeyData: JourneyData): FormSummaryDataModel {
        val landlordType = DataHelper.getLandlordType(journeyDataService, journeyData)!!
        return FormSummaryDataModel(
            "forms.checkPropertyAnswers.propertyDetails.landlordType",
            landlordType,
            RegisterPropertyStepId.LandlordType.urlPathSegment,
        )
    }

    private fun getLocalAuthority(custodianCode: String?) = LOCAL_AUTHORITIES.single { it.custodianCode == custodianCode }

    private fun getOccupyingTenantsDetails(journeyData: JourneyData): List<FormSummaryDataModel> =
        listOf(
            FormSummaryDataModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                true,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
            ),
            FormSummaryDataModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                DataHelper.getNumberOfHouseholds(journeyDataService, journeyData)!!,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            ),
            FormSummaryDataModel(
                "forms.checkPropertyAnswers.propertyDetails.people",
                DataHelper.getNumberOfTenants(journeyDataService, journeyData)!!,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            ),
        )
}
