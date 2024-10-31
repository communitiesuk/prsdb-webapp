package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord.BestFriendEmailForm
import uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord.EmailForm
import uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord.PhoneNumberForm
import uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord.RegisterLandlordStepId

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
                nextStep(RegisterLandlordStepId.QuickBreak)
            }

            interstitial(RegisterLandlordStepId.QuickBreak) {
                nextStep(RegisterLandlordStepId.BestFriendEmail)
            }

            step(RegisterLandlordStepId.BestFriendEmail) {
                page(BestFriendEmailForm::class) {
                    messageKeys("registerAsALandlord", "bestfriendemail")
                }
                nextStep {
                    ifSavedForms(RegisterLandlordStepId.PhoneNumber) { it.isEmpty() }
                    default(RegisterLandlordStepId.ReviewPhoneNumbers)
                }
            }

            interstitial(RegisterLandlordStepId.ReviewPhoneNumbers) {
                nextStep {
                    ifUserAction(RegisterLandlordStepId.PhoneNumber) { it == "add-new" }
                    default("/register-as-a-landlord/check-answers")
                }
            }

            step(RegisterLandlordStepId.PhoneNumber) {
                allowRepeats = true
                page(PhoneNumberForm::class) {
                    messageKeys("registerAsALandlord", "phoneNumber")
                    userActions {
                        saveAndContinue("next")
                        custom("repeat", "registerAsALandlord.phoneNumber.addAnother")
                    }
                }
                nextStep {
                    ifUserAction(RegisterLandlordStepId.PhoneNumber) { it == "repeat" }
                    default("/register-as-a-landlord/check-answers")
                }
            }
        }
}
