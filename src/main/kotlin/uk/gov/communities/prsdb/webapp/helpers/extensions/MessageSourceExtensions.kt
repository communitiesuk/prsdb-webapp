package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.springframework.context.MessageSource
import java.util.Locale

class MessageSourceExtensions {
    companion object {
        fun MessageSource.getMessageForKey(
            key: String,
            args: Array<Any>? = null,
        ) = getMessage(key, args, Locale.getDefault())
    }
}
