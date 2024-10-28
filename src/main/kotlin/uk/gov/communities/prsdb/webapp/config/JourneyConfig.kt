package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.models.journeyModels.FormModel
import uk.gov.communities.prsdb.webapp.models.journeyModels.Journey
import uk.gov.communities.prsdb.webapp.models.journeyModels.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.journeyModels.Page
import uk.gov.communities.prsdb.webapp.models.journeyModels.Step

@Configuration
class JourneyConfig {
    // TODO this does not have to be a config, it can be a class, it MUST have a bean
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
                                    messageKeys =
                                        mapOf(
                                            "title" to "start",
                                            "serviceName" to "multipage form",
                                            "postURI" to "register-as-a-landlord/end",
                                            "template" to "multiFormFrameworkDemo",
                                        ),
                                    formModel = FormModel(),
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.End },
                        ),
                    LandlordRegistrationStepId.End to
                        Step(
                            page =
                                Page(
                                    messageKeys =
                                        mapOf(
                                            "title" to "end",
                                            "serviceName" to "multipage form",
                                            "postURI" to "register-as-a-landlord/start",
                                            "template" to "multiFormFrameworkDemo",
                                        ),
                                    formModel = FormModel(),
                                ),
                            nextStep = { _ -> LandlordRegistrationStepId.Start },
                        ),
                ),
        )
}
