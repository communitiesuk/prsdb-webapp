package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NonEnglandOrWalesAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

class LandlordRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) = getVerifiedName(journeyData) ?: getManualName(journeyData)

        private fun getManualName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Name.urlPathSegment,
                NameFormModel::class.memberProperties.first().name,
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
                    DateOfBirthFormModel::class.memberProperties.elementAt(1).name,
                ) ?: return null

            val month =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::class.memberProperties.elementAt(2).name,
                ) ?: return null

            val year =
                getFieldIntegerValue(
                    journeyData,
                    LandlordRegistrationStepId.DateOfBirth.urlPathSegment,
                    DateOfBirthFormModel::class.memberProperties.last().name,
                ) ?: return null

            return LocalDate.of(year, month, day)
        }

        fun getEmail(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.Email.urlPathSegment,
                EmailFormModel::class.memberProperties.first().name,
            )

        fun getPhoneNumber(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment,
                PhoneNumberFormModel::class.memberProperties.first().name,
            )

        fun getLivesInEnglandOrWales(journeyData: JourneyData) =
            getFieldBooleanValue(
                journeyData,
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                CountryOfResidenceFormModel::class.memberProperties.last().name,
            )

        fun getNonEnglandOrWalesCountryOfResidence(journeyData: JourneyData) =
            if (getLivesInEnglandOrWales(journeyData) == true) {
                null
            } else {
                getFieldStringValue(
                    journeyData,
                    LandlordRegistrationStepId.CountryOfResidence.urlPathSegment,
                    CountryOfResidenceFormModel::class.memberProperties.first().name,
                )
            }

        fun getAddress(
            journeyData: JourneyData,
            lookedUpAddresses: List<AddressDataModel>,
        ): AddressDataModel? {
            val livesInEnglandOrWales = getLivesInEnglandOrWales(journeyData) ?: return null

            return if (isManualAddressChosen(journeyData, !livesInEnglandOrWales)) {
                getManualAddress(journeyData, !livesInEnglandOrWales)
            } else {
                val selectedAddress = getSelectedAddress(journeyData, !livesInEnglandOrWales) ?: return null
                lookedUpAddresses.singleOrNull { it.singleLineAddress == selectedAddress }
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
                SelectAddressFormModel::class.memberProperties.first().name,
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
                NonEnglandOrWalesAddressFormModel::class.memberProperties.first().name,
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
