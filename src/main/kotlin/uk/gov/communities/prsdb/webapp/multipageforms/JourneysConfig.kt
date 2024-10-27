package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.multipageforms.components.Email
import uk.gov.communities.prsdb.webapp.multipageforms.components.PhoneNumber

@Configuration
class JourneysConfig {
    @Bean
    fun landlordRegistrationJourney(): Journey<LandlordRegistrationStepId> =
        Journey(
            stepIdType = LandlordRegistrationStepId::class,
            journeyType = JourneyType.LANDLORD_REGISTRATION,
            initialStepId = LandlordRegistrationStepId.Email,
            steps =
                mapOf(
                    LandlordRegistrationStepId.Email to
                        Step(
                            page =
                                Page(
                                    titleKey = "registerAsALandlord.title",
                                    formComponents =
                                        listOf(
                                            Email(
                                                fieldName = "email",
                                                validationRules =
                                                    listOf(
                                                        {
                                                            if (it?.matches(Regex(""".+@.+""")) == false) {
                                                                listOf("formComponents.email.error.invalidFormat")
                                                            } else {
                                                                listOf()
                                                            }
                                                        },
                                                    ),
                                            ),
                                        ),
                                ),
                            nextStep = { StepAction.GoToStep(LandlordRegistrationStepId.PhoneNumber) },
                        ),
                    LandlordRegistrationStepId.PhoneNumber to
                        Step(
                            page =
                                Page(
                                    titleKey = "registerAsALandlord.title",
                                    formComponents =
                                        listOf(
                                            PhoneNumber(
                                                fieldName = "phoneNumber",
                                                validationRules =
                                                    listOf(
                                                        {
                                                            if (it?.matches(Regex("""[\d ]+""")) == false) {
                                                                listOf("formComponents.phoneNumber.error.invalidFormat")
                                                            } else {
                                                                listOf()
                                                            }
                                                        },
                                                    ),
                                            ),
                                        ),
                                ),
                            nextStep = { StepAction.Redirect("/register-as-a-landlord/check-answers") },
                        ),
                ),
        )
}
