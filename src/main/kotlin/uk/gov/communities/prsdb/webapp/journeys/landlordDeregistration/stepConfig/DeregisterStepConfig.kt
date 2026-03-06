package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordWithPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDetailsEmailSectionList
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class DeregisterStepConfig(
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val securityContextService: SecurityContextService,
    private val confirmationWithPropertiesEmailSender: EmailNotificationService<LandlordWithPropertiesDeregistrationConfirmationEmail>,
    private val confirmationWithNoPropertiesEmailSender: EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>,
) : AbstractInternalStepConfig<Complete, LandlordDeregistrationJourneyState>() {
    override fun mode(state: LandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: LandlordDeregistrationJourneyState) {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val landlordEmailAddress = landlordService.retrieveLandlordByBaseUserId(baseUserId)!!.email

        val landlordProperties = propertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId)
        val landlordHadActiveProperties = landlordProperties.isNotEmpty()

        landlordDeregistrationService.deregisterLandlord(baseUserId)
        landlordDeregistrationService.addLandlordHadActivePropertiesToSession(landlordHadActiveProperties)

        if (landlordHadActiveProperties) {
            val propertySectionList = PropertyDetailsEmailSectionList.fromPropertyOwnerships(landlordProperties)
            confirmationWithPropertiesEmailSender.sendEmail(
                landlordEmailAddress,
                LandlordWithPropertiesDeregistrationConfirmationEmail(propertySectionList),
            )
        } else {
            confirmationWithNoPropertiesEmailSender.sendEmail(
                landlordEmailAddress,
                LandlordNoPropertiesDeregistrationConfirmationEmail(),
            )
        }

        securityContextService.refreshContext()
    }

    override fun resolveNextDestination(
        state: LandlordDeregistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class DeregisterStep(
    stepConfig: DeregisterStepConfig,
) : JourneyStep.InternalStep<Complete, LandlordDeregistrationJourneyState>(stepConfig)
