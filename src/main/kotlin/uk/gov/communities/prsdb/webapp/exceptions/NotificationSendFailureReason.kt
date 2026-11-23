package uk.gov.communities.prsdb.webapp.exceptions

enum class NotificationSendFailureReason {
    INVALID_REQUEST,
    TECHNICAL,
    DAILY_LIMIT,
    MINUTE_LIMIT,
    UNKNOWN,
}
