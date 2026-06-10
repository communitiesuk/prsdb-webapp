package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.controllers.CancelJointLandlordInvitationController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PendingInvitationViewModel(
    val email: String,
    val expiresInDays: Long,
    val expiryDate: String,
    val sentDate: String,
    val cancelUrl: String,
)

data class ExpiredInvitationViewModel(
    val invitationId: Long,
    val email: String,
    val expiredDate: String,
    val removeFromListUrl: String,
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
                cancelUrl = CancelJointLandlordInvitationController.getCancelJointLandlordInvitationPath(invitation.id),
            )

        fun buildExpiredViewModel(invitation: JointLandlordInvitation): ExpiredInvitationViewModel =
            ExpiredInvitationViewModel(
                invitationId = invitation.id,
                email = invitation.invitedEmail,
                expiredDate = formatDate(invitation.expiresOnDate),
                removeFromListUrl =
                    PropertyDetailsController.getRemoveExpiredInvitePath(
                        invitation.registeredOwnership.id,
                        invitation.id,
                    ),
            )

        private fun formatDate(date: LocalDate): String = date.toJavaLocalDate().format(DATE_FORMATTER)
    }
}
