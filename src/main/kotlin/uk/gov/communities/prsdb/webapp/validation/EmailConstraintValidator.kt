package uk.gov.communities.prsdb.webapp.validation

import java.net.IDN

open class EmailConstraintValidator : PropertyConstraintValidator {
    companion object {
        private const val VALID_LOCAL_CHARS = """a-zA-Z0-9.!#$%&'*+/=?^_`{|}~\-"""
        private val EMAIL_REGEX = Regex("^[$VALID_LOCAL_CHARS]+@([^.@][^@\\s]+)$")
        private val HOSTNAME_PART_REGEX = Regex("^(xn|[a-z0-9]+)(-?-[a-z0-9]+)*$", RegexOption.IGNORE_CASE)
        private val TLD_PART_REGEX = Regex("^([a-z]{2,63}|xn--([a-z0-9]+-)*[a-z0-9]+)$", RegexOption.IGNORE_CASE)
        private const val MAX_EMAIL_LENGTH = 320
        private const val MAX_HOSTNAME_LENGTH = 253
        private const val MAX_HOSTNAME_PART_LENGTH = 63
    }

    // More or less a copy of
    // https://github.com/alphagov/notifications-utils/blob/995faf0d925f7e95ecac6ecec383b1d162b2eceb/notifications_utils/recipient_validation/email_address.py
    // From Notify
    override fun isValid(value: Any?): Boolean {
        val email = (value as? CharSequence)?.toString() ?: return false
        if (email.isBlank()) return false

        val match = EMAIL_REGEX.matchEntire(email) ?: return false

        if (email.length > MAX_EMAIL_LENGTH) return false

        if (".." in email) return false

        val hostname =
            try {
                IDN.toASCII(match.groupValues[1])
            } catch (_: IllegalArgumentException) {
                return false
            }

        if (hostname.length > MAX_HOSTNAME_LENGTH) return false

        val parts = hostname.split(".")
        if (parts.size < 2) return false

        for (part in parts) {
            if (part.isEmpty() || part.length > MAX_HOSTNAME_PART_LENGTH || !HOSTNAME_PART_REGEX.matches(part)) {
                return false
            }
        }

        return TLD_PART_REGEX.matches(parts.last())
    }
}

class OptionalEmailConstraintValidator : EmailConstraintValidator() {
    override fun isValid(value: Any?): Boolean {
        if (value == null) return true
        if (value is String && value.isBlank()) return true
        return super.isValid(value)
    }
}
