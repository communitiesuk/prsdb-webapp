package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.time.LocalDate

class LandlordJourneyDataHelper {
    companion object {
        fun getName(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = getVerifiedName(journeyDataService, journeyData) ?: getManualName(journeyDataService, journeyData)

        private fun getManualName(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldStringValue(
            journeyData,
            LandlordRegistrationStepId.Name.urlPathSegment,
            "name",
        )

        private fun getVerifiedName(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldStringValue(
            journeyData,
            LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
            "name",
        )

        fun getDOB(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = getVerifiedDOB(journeyDataService, journeyData) ?: getManualDOB(journeyDataService, journeyData)

        private fun getVerifiedDOB(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldLocalDateValue(
            journeyData,
            LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
            "birthDate",
        )

        private fun getManualDOB(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ): LocalDate? {
            val day =
                journeyDataService.getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "day",
                ) ?: return null

            val month =
                journeyDataService.getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "month",
                ) ?: return null

            val year =
                journeyDataService.getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "year",
                ) ?: return null

            return LocalDate.of(year, month, day)
        }

        fun getEmail(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldStringValue(
            journeyData,
            LandlordRegistrationStepId.Email.urlPathSegment,
            "emailAddress",
        )

        fun getPhoneNumber(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldStringValue(
            journeyData,
            LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
            "phoneNumber",
        )

        fun getLivesInUK(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldBooleanValue(
            journeyData,
            LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            "livesInUK",
        )

        fun getCountryOfResidence(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldStringValue(
            journeyData,
            LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
            "countryOfResidence",
        )

        fun getAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            val livesInUK = getLivesInUK(journeyDataService, journeyData) ?: return null

            return if (isManualAddressChosen(journeyDataService, journeyData, !livesInUK)) {
                getManualAddress(journeyDataService, journeyData, !livesInUK, addressDataService)
            } else {
                val selectedAddress = getSelectedAddress(journeyDataService, journeyData, !livesInUK) ?: return null
                addressDataService.getAddressData(selectedAddress)
            }
        }

        private fun getSelectedAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
        ): String? {
            val selectAddressPathSegment =
                if (isContactAddress) {
                    LandlordRegistrationStepId.SelectContactAddress.urlPathSegment
                } else {
                    LandlordRegistrationStepId.SelectAddress.urlPathSegment
                }

            return journeyDataService.getFieldStringValue(
                journeyData,
                selectAddressPathSegment,
                "address",
            )
        }

        private fun getManualAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            val manualAddressPathSegment =
                if (isContactAddress) {
                    LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
                } else {
                    LandlordRegistrationStepId.ManualAddress.urlPathSegment
                }

            return addressDataService.getManualAddress(journeyDataService, journeyData, manualAddressPathSegment)
        }

        fun getInternationalAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = journeyDataService.getFieldStringValue(
            journeyData,
            LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
            "internationalAddress",
        )

        fun isIdentityVerified(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
        ) = getVerifiedName(journeyDataService, journeyData) != null &&
            getVerifiedDOB(journeyDataService, journeyData) != null

        fun isManualAddressChosen(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
        ) = getSelectedAddress(journeyDataService, journeyData, isContactAddress) == MANUAL_ADDRESS_CHOSEN
    }
}
