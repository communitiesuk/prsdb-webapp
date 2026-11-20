package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordDeactivationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail
import java.time.LocalDate

@PrsdbTaskService
class LandlordDeactivationService(
    private val landlordRepository: LandlordRepository,
    private val landlordDeactivationRepository: LandlordDeactivationRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val landlordDeactivatedEmailSender: EmailNotificationService<LandlordDeactivatedEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    fun getIdsOfLandlordsWhoNeverRegisteredAProperty(): List<Long> {
        val cutoff =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusMonths(LANDLORD_WITHOUT_PROPERTY_DEACTIVATION_PERIOD_IN_MONTHS.toLong())
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()
        return landlordDeactivationRepository.findIdsOfActiveLandlordsWithNoRegisteredPropertyCreatedBefore(cutoff)
    }

    fun getIdsOfLandlordsWithNoActiveProperties(): List<Long> =
        landlordDeactivationRepository.findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties()

    // Re-checks the landlord inside its own transaction in case they changed after the candidate query ran
    @Transactional
    fun deactivateLandlordIfNoActiveProperties(landlordId: Long): Boolean {
        val landlord =
            landlordRepository.findById(landlordId).orElseThrow {
                IllegalStateException("Landlord $landlordId not found")
            }
        if (!landlord.isActive || propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId)) {
            return false
        }

        landlord.deactivate(LocalDate.now(DateTimeHelper.UK_ZONE))
        landlordRepository.save(landlord)

        // The same reason applies whether the landlord never registered a property or has no active properties left
        sendLandlordDeactivatedEmail(landlord)
        return true
    }

    private fun sendLandlordDeactivatedEmail(landlord: Landlord) {
        NotificationHelper.sendNotificationAndLogFailures("the landlord deactivated email for landlord ${landlord.id}") {
            landlordDeactivatedEmailSender.sendEmailToLandlord(
                landlord,
                LandlordDeactivatedEmail(
                    recipientName = landlord.name,
                    registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                    reason = LandlordDeactivationReason.NO_PROPERTIES_REGISTERED,
                    dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                ),
            )
        }
    }
}
