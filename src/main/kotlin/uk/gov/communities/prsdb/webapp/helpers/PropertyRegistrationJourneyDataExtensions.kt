package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

object PropertyRegistrationJourneyDataExtensions {
    fun JourneyData.getAddress(addressDataService: AddressDataService): AddressDataModel? {
        return if (isManualAddressChosen()) {
            getManualAddress(
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
            )
        } else {
            val selectedAddress = getSelectedAddress() ?: return null
            addressDataService.getAddressData(selectedAddress)
        }
    }

    fun JourneyData.getPropertyType(): PropertyType? =
        getFieldEnumValue<PropertyType>(
            RegisterPropertyStepId.PropertyType.urlPathSegment,
            "propertyType",
        )

    fun JourneyData.getCustomPropertyType(): String? =
        getFieldStringValue(
            RegisterPropertyStepId.PropertyType.urlPathSegment,
            "customPropertyType",
        )

    fun JourneyData.getOwnershipType(): OwnershipType? =
        getFieldEnumValue<OwnershipType>(
            RegisterPropertyStepId.OwnershipType.urlPathSegment,
            "ownershipType",
        )

    fun JourneyData.getLandlordType(): LandlordType? =
        getFieldEnumValue<LandlordType>(
            RegisterPropertyStepId.LandlordType.urlPathSegment,
            "landlordType",
        )

    fun JourneyData.getIsOccupied(): Boolean? =
        getFieldBooleanValue(
            RegisterPropertyStepId.Occupancy.urlPathSegment,
            "occupied",
        )

    fun JourneyData.getNumberOfHouseholds(): Int =
        getFieldIntegerValue(
            RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            "numberOfHouseholds",
        ) ?: 0

    fun JourneyData.getNumberOfTenants(): Int =
        getFieldIntegerValue(
            RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            "numberOfPeople",
        ) ?: 0

    fun JourneyData.getLicensingType(): LicensingType? =
        getFieldEnumValue<LicensingType>(
            RegisterPropertyStepId.LicensingType.urlPathSegment,
            "licensingType",
        )

    fun JourneyData.getLicenseNumber(): String? {
        val licenseNumberPathSegment =
            when (this.getLicensingType()!!) {
                LicensingType.SELECTIVE_LICENCE -> RegisterPropertyStepId.SelectiveLicence.urlPathSegment
                LicensingType.HMO_MANDATORY_LICENCE -> RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment
                LicensingType.HMO_ADDITIONAL_LICENCE -> RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment
                LicensingType.NO_LICENSING -> return ""
            }

        return getFieldStringValue(licenseNumberPathSegment, "licenceNumber")
    }

    private fun JourneyData.getSelectedAddress(): String? =
        getFieldStringValue(
            RegisterPropertyStepId.SelectAddress.urlPathSegment,
            "address",
        )

    fun JourneyData.isManualAddressChosen() = this.getSelectedAddress() == MANUAL_ADDRESS_CHOSEN
}
