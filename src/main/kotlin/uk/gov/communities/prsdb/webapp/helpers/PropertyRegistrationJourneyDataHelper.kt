package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class PropertyRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getAddress(
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            return if (isManualAddressChosen(journeyData)) {
                getManualAddress(
                    journeyData,
                    RegisterPropertyStepId.ManualAddress.urlPathSegment,
                    RegisterPropertyStepId.LocalAuthority.urlPathSegment,
                )
            } else {
                val selectedAddress = getSelectedAddress(journeyData) ?: return null
                addressDataService.getAddressData(selectedAddress)
            }
        }

        fun getPropertyType(journeyData: JourneyData): PropertyType? =
            getFieldEnumValue<PropertyType>(
                journeyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                "propertyType",
            )

        fun getCustomPropertyType(journeyData: JourneyData): String? =
            getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                "customPropertyType",
            )

        fun getOwnershipType(journeyData: JourneyData): OwnershipType? =
            getFieldEnumValue<OwnershipType>(
                journeyData,
                RegisterPropertyStepId.OwnershipType.urlPathSegment,
                "ownershipType",
            )

        fun getIsOccupied(journeyData: JourneyData): Boolean? =
            getFieldBooleanValue(
                journeyData,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
                "occupied",
            )

        fun getNumberOfHouseholds(journeyData: JourneyData): Int =
            getFieldIntegerValue(
                journeyData,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
                "numberOfHouseholds",
            ) ?: 0

        fun getNumberOfTenants(journeyData: JourneyData): Int =
            getFieldIntegerValue(
                journeyData,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
                "numberOfPeople",
            ) ?: 0

        fun getLicensingType(journeyData: JourneyData): LicensingType? =
            getFieldEnumValue<LicensingType>(
                journeyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            )

        fun getLicenseNumber(journeyData: JourneyData): String? {
            val licenseNumberPathSegment =
                when (getLicensingType(journeyData)!!) {
                    LicensingType.SELECTIVE_LICENCE -> RegisterPropertyStepId.SelectiveLicence.urlPathSegment
                    LicensingType.HMO_MANDATORY_LICENCE -> RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment
                    LicensingType.HMO_ADDITIONAL_LICENCE -> RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment
                    LicensingType.NO_LICENSING -> return ""
                }

            return getFieldStringValue(journeyData, licenseNumberPathSegment, "licenceNumber")
        }

        private fun getSelectedAddress(journeyData: JourneyData): String? =
            getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                "address",
            )

        fun isManualAddressChosen(journeyData: JourneyData) = getSelectedAddress(journeyData) == MANUAL_ADDRESS_CHOSEN
    }
}
