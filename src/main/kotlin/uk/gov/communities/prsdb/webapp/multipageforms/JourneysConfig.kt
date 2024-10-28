package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

@Configuration
class JourneysConfig {
    @Bean
    fun landlordRegistrationJourney(): Journey<RegisterLandlordStepId> =
        journey {
            journeyType = JourneyType.LANDLORD_REGISTRATION
            initialStepId = RegisterLandlordStepId.Email

            step(RegisterLandlordStepId.Email) {
                page {
                    messageKeys {
                        title = "registerAsALandlord.title"
                        fieldsetHeading = "registerAsALandlord.email.fieldsetHeading"
                        fieldsetHint = "registerAsALandlord.email.fieldsetHint"
                    }
                    email("email") {
                        labelKey = "registerAsALandlord.email.label"
                        validateRegex(Regex(""".+@.+"""), "registerAsALandlord.email.error.invalidFormat")
                    }
                }
                goToStep(RegisterLandlordStepId.PhoneNumber)
            }

            step(RegisterLandlordStepId.PhoneNumber) {
                page {
                    messageKeys {
                        title = "registerAsALandlord.title"
                        fieldsetHeading = "registerAsALandlord.phoneNumber.fieldsetHeading"
                        fieldsetHint = "registerAsALandlord.phoneNumber.fieldsetHint"
                    }
                    phoneNumber("phoneNumber") {
                        labelKey = "registerAsALandlord.phoneNumber.label"
                        hintKey = "registerAsALandlord.phoneNumber.hint"
                        validateRegex(Regex("""[\d ]+"""), "registerAsALandlord.phoneNumber.error.invalidFormat")
                    }
                }
                redirect("/register-as-a-landlord/check-answers")
            }
        }
}
