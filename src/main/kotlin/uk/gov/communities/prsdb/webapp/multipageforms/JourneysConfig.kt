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
                    titleKey = "registerAsALandlord.title"
                    email("email") {
                        validateRegex(Regex(""".+@.+"""), "formComponents.email.error.invalidFormat")
                    }
                }
                goToStep(RegisterLandlordStepId.PhoneNumber)
            }

            step(RegisterLandlordStepId.PhoneNumber) {
                page {
                    titleKey = "registerAsALandlord.title"
                    phoneNumber("phoneNumber") {
                        validateRegex(Regex("""[\d ]+"""), "formComponents.phoneNumber.error.invalidFormat")
                    }
                }
                redirect("/register-as-a-landlord/check-answers")
            }
        }
}
