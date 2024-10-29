package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.models.journeyModels.Journey
import uk.gov.communities.prsdb.webapp.models.journeyModels.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.journeyModels.Page
import uk.gov.communities.prsdb.webapp.models.journeyModels.Step

@Configuration
class JourneyConfig {
    @Bean
    fun landlordRegistrationJourney(): Journey<LandlordRegistrationStepId> =
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
                                            "serviceName" to "multipage form",
                                            "postURI" to "register-as-a-landlord/end",
                                        ),
                                    validateSubmission = { _ -> true },
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.End },
                            getSubmissionFromFormContext = { _ -> mapOf("fieldName" to "value") },
                        ),
                    LandlordRegistrationStepId.End to
                        Step(
                            page =
                                Page(
                                    template = "multiFormFrameworkDemo",
                                    messageKeys =
                                        mapOf(
                                            "title" to "end",
                                            "serviceName" to "multipage form",
                                            "postURI" to "register-as-a-landlord/start",
                                        ),
                                    validateSubmission = { _ -> true },
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.Start },
                            getSubmissionFromFormContext = { _ -> mapOf("fieldName" to "value") },
                        ),
                ),
        )
}
