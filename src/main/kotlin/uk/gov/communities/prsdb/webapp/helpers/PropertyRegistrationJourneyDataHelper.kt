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

class PropertyRegistrationJourneyDataHelper {
    companion object {
        fun getAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            return if (getSelectedAddress(journeyDataService, journeyData) == MANUAL_ADDRESS_CHOSEN) {
                addressDataService.getManualAddress(journeyDataService, journeyData, RegisterPropertyStepId.ManualAddress.urlPathSegment)
            } else {
                val selectedAddress = getSelectedAddress(journeyDataService, journeyData) ?: return null
                addressDataService.getAddressData(selectedAddress)
            }
        }

        fun getPropertyType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): PropertyType? {
            val propertyTypeString =
                journeyDataService.getFieldStringValue(journeyData, RegisterPropertyStepId.PropertyType.urlPathSegment, "propertyType")
                    ?: return null
            return PropertyType.valueOf(propertyTypeString)
        }

        fun getCustomPropertyType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): String? =
            journeyDataService.getFieldStringValue(journeyData, RegisterPropertyStepId.PropertyType.urlPathSegment, "customPropertyType")

        fun getOwnershipType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): OwnershipType? {
            val ownershipTypeString =
                journeyDataService.getFieldStringValue(journeyData, RegisterPropertyStepId.OwnershipType.urlPathSegment, "ownershipType")
                    ?: return null
            return OwnershipType.valueOf(ownershipTypeString)
        }

        fun getLandlordType(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): LandlordType? {
            val landlordTypeString =
                journeyDataService.getFieldStringValue(journeyData, RegisterPropertyStepId.LandlordType.urlPathSegment, "landlordType")
                    ?: return null

            return LandlordType.valueOf(landlordTypeString)
        }

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
        ): LicensingType? {
            val licensingType =
                journeyDataService
                    .getFieldStringValue(journeyData, RegisterPropertyStepId.LicensingType.urlPathSegment, "licensingType")
                    ?: return null
            return LicensingType.valueOf(licensingType)
        }

        fun getLicenseNumber(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            licenseNumberPathSegment: String,
        ): String? = journeyDataService.getFieldStringValue(journeyData, licenseNumberPathSegment, "licenceNumber")

        private fun getSelectedAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): String? =
            journeyDataService.getFieldStringValue(
                journeyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                "address",
            )
    }
}
