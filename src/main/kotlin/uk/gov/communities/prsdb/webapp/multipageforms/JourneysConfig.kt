package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.multipageforms.components.EmailInput
import uk.gov.communities.prsdb.webapp.multipageforms.components.PhoneNumberInput

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
                                            EmailInput(),
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
                                            PhoneNumberInput(),
                                        ),
                                ),
                            nextStep = { StepAction.Redirect("/register-as-a-landlord/check-answers") },
                        ),
                ),
        )
}
