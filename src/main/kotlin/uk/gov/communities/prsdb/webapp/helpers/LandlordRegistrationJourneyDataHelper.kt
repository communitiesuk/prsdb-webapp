package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import java.time.LocalDate

class LandlordRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) = getVerifiedName(journeyData) ?: getManualName(journeyData)

        private fun getManualName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Name.urlPathSegment,
                "name",
            )

        fun getVerifiedName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "name",
            )

        fun getDOB(journeyData: JourneyData) = getVerifiedDOB(journeyData) ?: getManualDOB(journeyData)

        fun getVerifiedDOB(journeyData: JourneyData) =
            getFieldLocalDateValue(
                journeyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                "birthDate",
            )

        private fun getManualDOB(journeyData: JourneyData): LocalDate? {
            val day =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "day",
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "month",
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    "year",
                ) ?: return null

            return LocalDate.of(year, month, day)
        }

        fun getEmail(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Email.urlPathSegment,
                "emailAddress",
            )

        fun getPhoneNumber(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
                "phoneNumber",
            )

        fun getLivesInEnglandOrWales(journeyData: JourneyData) =
            getFieldBooleanValue(
                journeyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                "livesInEnglandOrWales",
            )

        fun getNonEnglandOrWalesCountryOfResidence(journeyData: JourneyData) =
            if (getLivesInEnglandOrWales(journeyData) == true) {
                null
            } else {
                getFieldStringValue(
                    journeyData,
                    LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                    "countryOfResidence",
                )
            }

        fun getAddress(
            journeyData: JourneyData,
            addressDataService: AddressDataService,
        ): AddressDataModel? {
            val livesInEnglandOrWales = getLivesInEnglandOrWales(journeyData) ?: return null

            return if (isManualAddressChosen(journeyData, !livesInEnglandOrWales)) {
                getManualAddress(journeyData, !livesInEnglandOrWales)
            } else {
                val selectedAddress = getSelectedAddress(journeyData, !livesInEnglandOrWales) ?: return null
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

            return getFieldStringValue(
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

            return getManualAddress(journeyData, manualAddressPathSegment)
        }

        fun getNonEnglandOrWalesAddress(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment,
                "nonEnglandOrWalesAddress",
            )

        fun isIdentityVerified(journeyData: JourneyData) =
            getVerifiedName(journeyData) != null &&
                getVerifiedDOB(journeyData) != null

        fun isManualAddressChosen(
            journeyData: JourneyData,
            isContactAddress: Boolean = false,
        ) = getSelectedAddress(journeyData, isContactAddress) == MANUAL_ADDRESS_CHOSEN

        fun getCountryOfResidence(journeyData: JourneyData): String =
            getNonEnglandOrWalesCountryOfResidence(journeyData) ?: ENGLAND_OR_WALES
    }
}
