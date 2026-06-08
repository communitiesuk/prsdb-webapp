package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
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
                expiryDate = formatDate(invitation.expiresOnDate),
                sentDate = formatDate(DateTimeHelper.getDateInUK(invitation.createdDate.toKotlinInstant())),
            )

        fun buildExpiredViewModel(invitation: JointLandlordInvitation): ExpiredInvitationViewModel =
            ExpiredInvitationViewModel(
                email = invitation.invitedEmail,
                expiredDate = formatDate(invitation.expiresOnDate),
            )

        private fun formatDate(date: LocalDate): String = date.toJavaLocalDate().format(DATE_FORMATTER)
    }
}
