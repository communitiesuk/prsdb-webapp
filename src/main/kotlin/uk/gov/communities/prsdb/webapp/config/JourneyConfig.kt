package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.models.journeyModels.*

@Configuration
class JourneyConfig {
    @Bean
    fun landlordRegistrationJourney(validator: Validator): Journey<LandlordRegistrationStepId> =
        Journey(
            journeyType = JourneyType.LANDLORD_REGISTRATION,
            initialStepId = LandlordRegistrationStepId.Start,
            steps =
                mapOf(
                    LandlordRegistrationStepId.Start to
                        Step(
                            page =
                                Page(
                                    template = "multiFormFrameworkDemo",
                                    messageKeys =
                                        mapOf(
                                            "title" to "start",
                                            "serviceName" to "multipage form demo",
                                        ),
                                    validateSubmission = { _ -> true },
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.Second },
                            updateContext = { _, _ -> mapOf("fieldName" to "fieldValue") },
                        ),
                    LandlordRegistrationStepId.Second to
                        Step(
                            page =
                                Page(
                                    template = "multiFormFrameworkDemo",
                                    messageKeys =
                                        mapOf(
                                            "title" to "start",
                                            "serviceName" to "multipage form demo",
                                        ),
                                    validateSubmission = { _ -> true },
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.End },
                            updateContext = { _, _ -> mapOf("postCode" to "fieldValue") },
                        ),
                    LandlordRegistrationStepId.End to
                        Step(
                            page =
                                Page(
                                    template = "multiFormFrameworkDemo",
                                    messageKeys =
                                        mapOf(
                                            "title" to "end",
                                            "serviceName" to "multipage form demo",
                                        ),
                                    validateSubmission = { _ -> true },
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.Start },
                            updateContext = { _, _ -> mapOf("fieldName" to "fieldValue") },
                        ),
                ),
        )
}
