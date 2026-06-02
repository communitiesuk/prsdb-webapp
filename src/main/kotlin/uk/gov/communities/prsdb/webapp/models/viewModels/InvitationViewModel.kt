package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PendingInvitationViewModel(
    val email: String,
    val expiresInDays: Long,
    val expiryDate: String,
    val sentDate: String,
)

data class ExpiredInvitationViewModel(
    val email: String,
    val expiredDate: String,
)

class InvitationViewModelBuilder {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

        fun buildPendingViewModel(invitation: JointLandlordInvitation): PendingInvitationViewModel =
            PendingInvitationViewModel(
                email = invitation.invitedEmail,
                expiresInDays = invitation.daysUntilExpiry,
                expiryDate = formatInstant(invitation.expiryDate),
                sentDate = formatInstant(invitation.createdDate),
            )

        fun buildExpiredViewModel(invitation: JointLandlordInvitation): ExpiredInvitationViewModel =
            ExpiredInvitationViewModel(
                email = invitation.invitedEmail,
                expiredDate = formatInstant(invitation.expiryDate),
            )

        private fun formatInstant(instant: Instant): String =
            DateTimeHelper.getDateInUK(instant.toKotlinInstant()).toJavaLocalDate().format(DATE_FORMATTER)
    }
}
