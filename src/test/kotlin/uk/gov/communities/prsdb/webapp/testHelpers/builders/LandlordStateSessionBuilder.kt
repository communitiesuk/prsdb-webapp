package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

class LandlordStateSessionBuilder(
    override val mockLocalCouncilService: LocalCouncilService = mock(),
) : JourneyStateSessionBuilder<LandlordStateSessionBuilder>(),
    IdentityStateBuilder<LandlordStateSessionBuilder>,
    AddressStateBuilder<LandlordStateSessionBuilder> {
    fun withPrivacyNotice(): LandlordStateSessionBuilder {
        val privacyNoticeFormModel =
            LandlordPrivacyNoticeFormModel().apply {
                agreesToPrivacyNotice = true
            }
        withSubmittedValue(PrivacyNoticeStep.ROUTE_SEGMENT, privacyNoticeFormModel)
        return self()
    }

    fun withEmail(email: String = "email@test.com"): LandlordStateSessionBuilder {
        val emailFormModel = EmailFormModel().apply { emailAddress = email }
        withSubmittedValue(EmailStep.ROUTE_SEGMENT, emailFormModel)
        return self()
    }

    fun withPhoneNumber(phoneNumber: String = "01234567890"): LandlordStateSessionBuilder {
        val phoneFormModel = PhoneNumberFormModel().apply { this.phoneNumber = phoneNumber }
        withSubmittedValue(PhoneNumberStep.ROUTE_SEGMENT, phoneFormModel)
        return self()
    }

    companion object {
        fun beforeName() = LandlordStateSessionBuilder().withPrivacyNotice().withIdentityNotVerified()

        fun beforeDob() = beforeName().withName()

        fun beforeEmail() = beforeDob().withDateOfBirth()

        fun beforePhoneNumber() = beforeEmail().withEmail()

        fun beforeCountryOfResidence() = beforePhoneNumber().withPhoneNumber()

        fun beforeLookupAddress() = beforeCountryOfResidence().withEnglandOrWalesResidence()

        fun beforeSelectAddress() = beforeLookupAddress().withLookupAddress()

        fun beforeManualAddress() = beforeSelectAddress().withManualAddressSelected()

        fun beforeCheckAnswers() = beforeSelectAddress().withSelectedAddress()
    }
}
