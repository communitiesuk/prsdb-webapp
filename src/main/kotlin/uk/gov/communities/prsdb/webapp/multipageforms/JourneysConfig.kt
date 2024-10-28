package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

@Configuration
class JourneysConfig {
    @Bean
    fun landlordRegistrationJourney(): Journey<LandlordRegistrationStepId> =
        journey {
            stepIdType = LandlordRegistrationStepId::class
            journeyType = JourneyType.LANDLORD_REGISTRATION
            initialStepId = LandlordRegistrationStepId.Email

            step(LandlordRegistrationStepId.Email) {
                page {
                    titleKey = "registerAsALandlord.title"
                    email("email") {
                        validateRegex(Regex(""".+@.+"""), "formComponents.email.error.invalidFormat")
                    }
                }
                goToStep(LandlordRegistrationStepId.PhoneNumber)
            }

            step(LandlordRegistrationStepId.PhoneNumber) {
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
