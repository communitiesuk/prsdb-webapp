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
        fun getName(journeyData: JourneyData) = getVerifiedName(journeyData) ?: getManualName(journeyData)

        private fun getManualName(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Name.urlPathSegment,
                "name",
            )

        private fun getVerifiedName(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "name",
            )

        fun getDOB(journeyData: JourneyData) = getVerifiedDOB(journeyData) ?: getManualDOB(journeyData)

        private fun getVerifiedDOB(journeyData: JourneyData) =
            JourneyDataService.getFieldLocalDateValue(
                journeyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "birthDate",
            )

        private fun getManualDOB(journeyData: JourneyData): LocalDate? {
            val day =
                JourneyDataService.getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "day",
                ) ?: return null

            val month =
                JourneyDataService.getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "month",
                ) ?: return null

            val year =
                JourneyDataService.getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "year",
                ) ?: return null

            return LocalDate.of(year, month, day)
        }

        fun getEmail(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Email.urlPathSegment,
                "emailAddress",
            )

        fun getPhoneNumber(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
                "phoneNumber",
            )

        fun getLivesInUK(journeyData: JourneyData) =
            JourneyDataService.getFieldBooleanValue(
                journeyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInUK",
            )

        fun getNonUKCountryOfResidence(journeyData: JourneyData) =
            if (getLivesInUK(journeyData) == true) {
                null
            } else {
                JourneyDataService.getFieldStringValue(
                    journeyData,
                    LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                    "countryOfResidence",
                )
            }

        fun getAddress(
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            val livesInUK = getLivesInUK(journeyData) ?: return null

            return if (isManualAddressChosen(journeyData, !livesInUK)) {
                getManualAddress(journeyData, !livesInUK)
            } else {
                val selectedAddress = getSelectedAddress(journeyData, !livesInUK) ?: return null
                addressDataService.getAddressData(selectedAddress)
            }
        }

        private fun getSelectedAddress(
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
        ): String? {
            val selectAddressPathSegment =
                if (isContactAddress) {
                    LandlordRegistrationStepId.SelectContactAddress.urlPathSegment
                } else {
                    LandlordRegistrationStepId.SelectAddress.urlPathSegment
                }

            return JourneyDataService.getFieldStringValue(
                journeyData,
                selectAddressPathSegment,
                "address",
            )
        }

        private fun getManualAddress(
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
        ): AddressDataModel? {
            val manualAddressPathSegment =
                if (isContactAddress) {
                    LandlordRegistrationStepId.ManualContactAddress.urlPathSegment
                } else {
                    LandlordRegistrationStepId.ManualAddress.urlPathSegment
                }

            return JourneyDataHelper.getManualAddress(journeyData, manualAddressPathSegment)
        }

        fun getInternationalAddress(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.InternationalAddress.urlPathSegment,
                "internationalAddress",
            )

        fun isIdentityVerified(journeyData: JourneyData) =
            getVerifiedName(journeyData) != null &&
                getVerifiedDOB(journeyData) != null

        fun isManualAddressChosen(
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
        ) = getSelectedAddress(journeyData, isContactAddress) == MANUAL_ADDRESS_CHOSEN
    }
}
