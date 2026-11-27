package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome

class NotificationHelper {
    companion object {
        fun sendNotificationAndLogFailures(
            notificationDescription: String,
            sendNotification: () -> Unit,
        ) {
            try {
                sendNotification()
            } catch (exception: Exception) {
                println("Failed to send $notificationDescription: ${exception.stackTraceToString()}")
            }
        }

        fun <T> sendAddedToPropertyNotifications(
            partyDescription: String,
            parties: List<T>,
            propertyOwnershipId: Long,
            sendNotification: (T) -> AssociatedPartyNotificationOutcome,
        ) {
            parties.forEachIndexed { index, party ->
                val partyLabel = "$partyDescription ${index + 1}"
                sendNotificationAndLogFailures(
                    "the $partyLabel added-to-property notification for property ownership $propertyOwnershipId",
                ) {
                    val notSentReason =
                        when (sendNotification(party)) {
                            AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED -> "details are fully redacted"
                            AssociatedPartyNotificationOutcome.NOT_SENT_IS_LANDLORD -> "they are a landlord on the property"
                            else -> null
                        }
                    if (notSentReason != null) {
                        println(
                            "Not sending $partyLabel added-to-property notification " +
                                "for property ownership $propertyOwnershipId: $notSentReason",
                        )
                    }
                }
            }
        }
    }
}
