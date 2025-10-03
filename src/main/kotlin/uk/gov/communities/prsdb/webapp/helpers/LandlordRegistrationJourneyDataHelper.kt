package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel
import java.time.LocalDate

class LandlordRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) = getVerifiedName(journeyData) ?: getManualName(journeyData)

        private fun getManualName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Name.urlPathSegment,
                NameFormModel::name.name,
            )

        fun getVerifiedName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                VerifiedIdentityModel::name.name,
            )

        fun getDOB(journeyData: JourneyData) = getVerifiedDOB(journeyData) ?: getManualDOB(journeyData)

        fun getVerifiedDOB(journeyData: JourneyData) =
            getFieldLocalDateValue(
                journeyData,
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                VerifiedIdentityModel::birthDate.name,
            )

        private fun getManualDOB(journeyData: JourneyData): LocalDate? {
            val day =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::day.name,
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::month.name,
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::year.name,
                ) ?: return null

            return LocalDate.of(year, month, day)
        }

        fun getEmail(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Email.urlPathSegment,
                EmailFormModel::emailAddress.name,
            )

        fun getPhoneNumber(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
                PhoneNumberFormModel::phoneNumber.name,
            )

        fun getLivesInEnglandOrWales(journeyData: JourneyData) =
            getFieldBooleanValue(
                journeyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                CountryOfResidenceFormModel::livesInEnglandOrWales.name,
            )

        fun getAddress(journeyData: JourneyData): AddressDataModel? =
            if (isManualAddressChosen(journeyData)) {
                getManualAddress(journeyData)
            } else {
                val selectedAddress = getSelectedAddress(journeyData)
                selectedAddress?.let { journeyData.getLookedUpAddress(it) }
            }

        private fun getSelectedAddress(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.SelectAddress.urlPathSegment,
                SelectAddressFormModel::address.name,
            )

        private fun getManualAddress(journeyData: JourneyData) =
            getManualAddress(journeyData, LandlordRegistrationStepId.ManualAddress.urlPathSegment)

        fun isIdentityVerified(journeyData: JourneyData) =
            getVerifiedName(journeyData) != null &&
                getVerifiedDOB(journeyData) != null

        fun getHasAcceptedPrivacyNotice(journeyData: JourneyData) =
            getFieldBooleanValue(
                journeyData,
                LandlordRegistrationStepId.PrivacyNotice.urlPathSegment,
                PrivacyNoticeFormModel::agreesToPrivacyNotice.name,
            )

        fun isManualAddressChosen(journeyData: JourneyData): Boolean =
            journeyData.getLookedUpAddresses().isEmpty() || getSelectedAddress(journeyData) == MANUAL_ADDRESS_CHOSEN
    }
}
