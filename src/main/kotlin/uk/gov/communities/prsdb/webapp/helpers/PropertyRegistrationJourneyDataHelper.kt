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
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService.Companion.getFieldEnumValue

class PropertyRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            return if (isManualAddressChosen(journeyDataService, journeyData)) {
                getManualAddress(journeyDataService, journeyData, RegisterPropertyStepId.ManualAddress.urlPathSegment)
            } else {
                val selectedAddress = getSelectedAddress(journeyDataService, journeyData) ?: return null
                addressDataService.getAddressData(selectedAddress)
            }
        }

        fun getCustodianCode(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): String? =
            journeyDataService.getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
                "localAuthorityCustodianCode",
            )

        fun getPropertyType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): PropertyType? =
            getFieldEnumValue<PropertyType>(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                "propertyType",
            )

        fun getCustomPropertyType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): String? =
            journeyDataService.getFieldStringValue(journeyData, RegisterPropertyStepId.PropertyType.urlPathSegment, "customPropertyType")

        fun getOwnershipType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): OwnershipType? =
            getFieldEnumValue<OwnershipType>(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.OwnershipType.urlPathSegment,
                "ownershipType",
            )

        fun getLandlordType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): LandlordType? =
            getFieldEnumValue<LandlordType>(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.LandlordType.urlPathSegment,
                "landlordType",
            )

        fun getIsOccupied(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): Boolean? = journeyDataService.getFieldBooleanValue(journeyData, RegisterPropertyStepId.Occupancy.urlPathSegment, "occupied")

        fun getNumberOfHouseholds(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): Int? =
            journeyDataService
                .getFieldIntegerValue(journeyData, RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment, "numberOfHouseholds")

        fun getNumberOfTenants(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): Int? =
            journeyDataService
                .getFieldIntegerValue(journeyData, RegisterPropertyStepId.NumberOfPeople.urlPathSegment, "numberOfPeople")

        fun getLicensingType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): LicensingType? =
            getFieldEnumValue<LicensingType>(
                journeyDataService,
                journeyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            )

        fun getLicenseNumber(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): String? {
            val licenseNumberPathSegment =
                when (getLicensingType(journeyDataService, journeyData)!!) {
                    LicensingType.SELECTIVE_LICENCE -> RegisterPropertyStepId.SelectiveLicence.urlPathSegment
                    LicensingType.HMO_MANDATORY_LICENCE -> RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment
                    LicensingType.HMO_ADDITIONAL_LICENCE -> RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment
                    LicensingType.NO_LICENSING -> return ""
                }

            return journeyDataService.getFieldStringValue(journeyData, licenseNumberPathSegment, "licenceNumber")
        }

        private fun getSelectedAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): String? =
            journeyDataService.getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                "address",
            )

        fun isManualAddressChosen(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = getSelectedAddress(journeyDataService, journeyData) == MANUAL_ADDRESS_CHOSEN
    }
}
