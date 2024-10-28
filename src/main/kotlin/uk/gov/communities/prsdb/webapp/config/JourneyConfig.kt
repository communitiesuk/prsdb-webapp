package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.models.journeyModels.Journey
import uk.gov.communities.prsdb.webapp.models.journeyModels.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.journeyModels.Page
import uk.gov.communities.prsdb.webapp.models.journeyModels.Step
import uk.gov.communities.prsdb.webapp.models.viewModels.StartPageFormModel

@Configuration
class JourneyConfig {
    // TODO this does not have to be a config, it can be a class, it MUST have a bean
    @Bean
    fun landlordRegistrationJourney(): Journey<LandlordRegistrationStepId> =
        Journey(
            id = "landlord-registration",
            initialStepId = LandlordRegistrationStepId.Start,
            steps =
                mapOf(
                    LandlordRegistrationStepId.Start to
                        Step(
                            page =
                                Page(
                                    formType = StartPageFormModel::class,
                                    messageKeys = mapOf("title" to "title.key"),
                                ),
                            nextStep = { _, _ -> LandlordRegistrationStepId.Start },
                        ),
                ),
        )
}
