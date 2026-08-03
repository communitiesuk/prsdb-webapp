package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
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
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class DeregisterStepConfig(
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val userToLandlordService: UserToLandlordService,
    private val securityContextService: SecurityContextService,
    private val confirmationWithPropertiesEmailSender: EmailNotificationService<LandlordWithPropertiesDeregistrationConfirmationEmail>,
    private val confirmationWithNoPropertiesEmailSender: EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>,
    private val swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService,
) : AbstractInternalStepConfig<Complete, LandlordDeregistrationJourneyState>() {
    override fun mode(state: LandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: LandlordDeregistrationJourneyState) {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val landlord = userToLandlordService.getCurrentLandlordForUser()
        // TODO: PDJB-1435: Update landlord deregistration for org landlords
        check(landlord is IndividualLandlord)

        val soleLandlordProperties = landlord.landlordships.toList()
        val landlordHadActiveSoloProperties = soleLandlordProperties.isNotEmpty()
        val jointlyOwnedProperties = landlord.landlordships.filterNot { it.isSolelyOwnedBy(landlord) }

        landlordDeregistrationService.deregisterLandlord(baseUserId)
        landlordDeregistrationService.addLandlordHadActivePropertiesToSession(landlordHadActiveSoloProperties)

        // TODO: PDJB-1274: Update emails to account for org landlord
        val landlordEmailAddress = landlord.email

        if (landlordHadActiveSoloProperties) {
            // TODO PDJB-311: This email does not address properties that are not deleted
            val propertySectionList = PropertyDetailsEmailSectionList.fromPropertyOwnerships(soleLandlordProperties)
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

        jointlyOwnedProperties.forEach {
            swapToIndividualNudgeEmailService.sendNudgeEmailIfApplicable(it)
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
