package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

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
                                    formType = EmailFormModel::class,
                                    messageKeys =
                                        GenericFormPageMessages(
                                            title = "registerAsALandlord.title",
                                            contentHeader = "registerAsALandlord.heading",
                                            formInstruction = "registerAsALandlord.heading",
                                        ),
                                ),
                            nextStep = { LandlordRegistrationStepId.PhoneNumber },
                        ),
                    LandlordRegistrationStepId.PhoneNumber to
                        Step(
                            page =
                                Page(
                                    formType = PhoneNumberFormModel::class,
                                    messageKeys =
                                        GenericFormPageMessages(
                                            title = "registerAsALandlord.title",
                                            contentHeader = "registerAsALandlord.heading",
                                            formInstruction = "registerAsALandlord.heading",
                                        ),
                                ),
                            nextStep = { null },
                        ),
                ),
        )
}
