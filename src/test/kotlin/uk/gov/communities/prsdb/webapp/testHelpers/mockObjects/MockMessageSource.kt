package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import java.util.Locale

class MockMessageSource : MessageSource {
    override fun getMessage(
        code: String,
        args: Array<out Any>?,
        defaultMessage: String?,
        locale: Locale,
    ): String = code

    override fun getMessage(
        code: String,
        args: Array<out Any>?,
        locale: Locale,
    ): String = code

    override fun getMessage(
        resolvable: MessageSourceResolvable,
        locale: Locale,
    ): String = ""
}
