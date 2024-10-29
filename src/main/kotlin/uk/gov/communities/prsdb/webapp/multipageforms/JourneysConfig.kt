package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord.EmailForm
import uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord.PhoneNumberForm

@Configuration
class JourneysConfig {
    @Bean
    fun landlordRegistrationJourney(validator: Validator): Journey<RegisterLandlordStepId> =
        journey(validator) {
            journeyType = JourneyType.LANDLORD_REGISTRATION
            initialStepId = RegisterLandlordStepId.Email

            step(RegisterLandlordStepId.Email) {
                page(EmailForm::class) {
                    messageKeys("registerAsALandlord", "email")
                }
                goToStep(RegisterLandlordStepId.QuickBreak)
            }

            interstitial(RegisterLandlordStepId.QuickBreak, RegisterLandlordStepId.PhoneNumber)

            step(RegisterLandlordStepId.PhoneNumber) {
                page(PhoneNumberForm::class) {
                    messageKeys("registerAsALandlord", "phoneNumber")
                }
                redirect("/register-as-a-landlord/check-answers")
            }
        }
}
