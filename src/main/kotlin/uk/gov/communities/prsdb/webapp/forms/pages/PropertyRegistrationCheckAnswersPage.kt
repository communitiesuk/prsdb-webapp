package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FormSummaryViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class PropertyRegistrationCheckAnswersPage(
    addressDataService: AddressDataService,
) : Page(
        NoInputFormModel::class,
        "forms/propertyRegistrationCheckAnswersForm",
        mapOf(
            "title" to "registerProperty.title",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        ),
    ) {
    private val addressHelpers = AddressHelpers(addressDataService)

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
        if (addressHelpers.isManualAddress(journeyData)) {
            addressHelpers.getManualAddressValue(journeyData[RegisterPropertyStepId.ManualAddress.urlPathSegment])
        } else {
            val addressData = objectToStringKeyedMap(journeyData[RegisterPropertyStepId.SelectAddress.urlPathSegment])
            addressData?.get("address") as String
        }

    private fun getPropertyDetailsSummary(journeyData: JourneyData): List<FormSummaryViewModel> =
        getAddressDetails(journeyData) +
            getPropertyTypeDetails(journeyData) +
            getOwnershipTypeDetails(journeyData) +
            getLicensingTypeDetails(journeyData) +
            getTenancyDetails(journeyData) +
            getLandlordTypeDetails(journeyData)

    private fun getAddressDetails(journeyData: JourneyData): List<FormSummaryViewModel> =
        if (addressHelpers.isManualAddress(journeyData)) {
            addressHelpers.manualAddressDetails(journeyData)
        } else {
            addressHelpers.selectedAddressDetails(journeyData)
        }

    private fun getPropertyTypeDetails(journeyData: JourneyData): FormSummaryViewModel {
        val propertyType =
            PropertyType.valueOf(
                objectToStringKeyedMap(
                    journeyData[RegisterPropertyStepId.PropertyType.urlPathSegment],
                )?.get("propertyType") as String,
            )
        return if (propertyType != PropertyType.OTHER) {
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.type",
                propertyType,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
            )
        } else {
            val otherType =
                objectToStringKeyedMap(
                    journeyData[RegisterPropertyStepId.PropertyType.urlPathSegment],
                )?.get("customPropertyType") as String
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.type",
                listOf(propertyType, otherType),
                RegisterPropertyStepId.PropertyType.urlPathSegment,
            )
        }
    }

    private fun getOwnershipTypeDetails(journeyData: JourneyData) =
        FormSummaryViewModel(
            "forms.checkPropertyAnswers.propertyDetails.ownership",
            OwnershipType.valueOf(
                objectToStringKeyedMap(journeyData[RegisterPropertyStepId.OwnershipType.urlPathSegment])?.get("ownershipType") as String,
            ),
            RegisterPropertyStepId.OwnershipType.urlPathSegment,
        )

    private fun getLicensingTypeDetails(journeyData: JourneyData): FormSummaryViewModel {
        val licensingType =
            LicensingType.valueOf(
                objectToStringKeyedMap(journeyData[RegisterPropertyStepId.LicensingType.urlPathSegment])?.get("licensingType") as String,
            )
        val licenceNumber =
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE ->
                    objectToStringKeyedMap(
                        journeyData[RegisterPropertyStepId.SelectiveLicence.urlPathSegment],
                    )?.get("licenceNumber") as String
                LicensingType.HMO_MANDATORY_LICENCE ->
                    objectToStringKeyedMap(
                        journeyData[RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment],
                    )?.get("licenceNumber") as String
                LicensingType.HMO_ADDITIONAL_LICENCE ->
                    objectToStringKeyedMap(
                        journeyData[RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment],
                    )?.get("licenceNumber") as String
                LicensingType.NO_LICENSING -> null
            }
        return FormSummaryViewModel(
            "forms.checkPropertyAnswers.propertyDetails.licensing",
            listOfNotNull(
                licensingType,
                licenceNumber,
            ),
            RegisterPropertyStepId.LicensingType.urlPathSegment,
        )
    }

    private fun getTenancyDetails(journeyData: JourneyData): List<FormSummaryViewModel> {
        val occupied = objectToStringKeyedMap(journeyData[RegisterPropertyStepId.Occupancy.urlPathSegment])?.get("occupied") as String
        if (occupied == "true") {
            return getOccupyingTenantsDetails(journeyData)
        } else {
            return listOf(
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.occupied",
                    false,
                    RegisterPropertyStepId.Occupancy.urlPathSegment,
                ),
            )
        }
    }

    private fun getLandlordTypeDetails(journeyData: JourneyData): FormSummaryViewModel {
        val landlordType =
            LandlordType.valueOf(
                objectToStringKeyedMap(journeyData[RegisterPropertyStepId.LandlordType.urlPathSegment])?.get("landlordType") as String,
            )
        return FormSummaryViewModel(
            "forms.checkPropertyAnswers.propertyDetails.landlordType",
            landlordType,
            RegisterPropertyStepId.LandlordType.urlPathSegment,
        )
    }

    private class AddressHelpers(
        val addressDataService: AddressDataService,
    ) {
        fun selectedAddressDetails(journeyData: JourneyData): List<FormSummaryViewModel> {
            val addressData = objectToStringKeyedMap(journeyData[RegisterPropertyStepId.SelectAddress.urlPathSegment])
            val singleLineAddress = addressData?.get("address") as String
            val address = addressDataService.getAddressData(singleLineAddress)
            return listOf(
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.address",
                    singleLineAddress,
                    RegisterPropertyStepId.LookupAddress.urlPathSegment,
                ),
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.uprn",
                    address?.uprn,
                    null,
                ),
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                    getLocalAuthority(address?.custodianCode).displayName,
                    null,
                ),
            )
        }

        fun manualAddressDetails(journeyData: JourneyData) =
            listOf(
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.address",
                    getManualAddressValue(journeyData[RegisterPropertyStepId.ManualAddress.urlPathSegment]),
                    RegisterPropertyStepId.LookupAddress.urlPathSegment,
                ),
                FormSummaryViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                    getLocalAuthority(
                        objectToStringKeyedMap(
                            journeyData[RegisterPropertyStepId.LocalAuthority.urlPathSegment],
                        )?.get("localAuthorityCustodianCode") as String,
                    ).displayName,
                    RegisterPropertyStepId.LocalAuthority.urlPathSegment,
                ),
            )

        fun getLocalAuthority(custodianCode: String?) = LOCAL_AUTHORITIES.single { it.custodianCode == custodianCode }

        fun isManualAddress(journeyData: JourneyData): Boolean =
            objectToStringKeyedMap(journeyData[RegisterPropertyStepId.SelectAddress.urlPathSegment])?.get("address") ==
                MANUAL_ADDRESS_CHOSEN

        fun getManualAddressValue(manualAddressData: Any?): String {
            val addressLineOne = objectToStringKeyedMap(manualAddressData)?.get("addressLineOne")
            val addressLineTwo = objectToStringKeyedMap(manualAddressData)?.get("addressLineTwo")
            val townOrCity = objectToStringKeyedMap(manualAddressData)?.get("townOrCity")
            val county = objectToStringKeyedMap(manualAddressData)?.get("county")
            val postcode = objectToStringKeyedMap(manualAddressData)?.get("postcode")
            return listOfNotNull(addressLineOne, addressLineTwo, townOrCity, county, postcode)
                .joinToString(", ")
        }
    }

    private fun getOccupyingTenantsDetails(journeyData: JourneyData): List<FormSummaryViewModel> =
        listOf(
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                true,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                objectToStringKeyedMap(
                    journeyData[RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment],
                )?.get("numberOfHouseholds") as String,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            ),
            FormSummaryViewModel(
                "forms.checkPropertyAnswers.propertyDetails.people",
                objectToStringKeyedMap(journeyData[RegisterPropertyStepId.NumberOfPeople.urlPathSegment])?.get("numberOfPeople") as String,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            ),
        )
}
