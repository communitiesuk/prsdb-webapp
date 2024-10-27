package uk.gov.communities.prsdb.webapp.multipageforms

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

data class Step<TForm : Any, TStepId : StepId>(
    val page: Page<TForm>,
    val persistAfterSubmit: Boolean = false,
    val nextStep: (Map<String, Any>) -> TStepId?,
    val isSatisfied: (Map<String, Any>) -> Boolean = { sessionData ->
        val formProperties = page.formType.memberProperties

        formProperties.all { property ->
            property.isAccessible = true
            val fieldValue = sessionData[property.name]
            fieldValue != null && (fieldValue is String && fieldValue.isNotBlank() || fieldValue !is String)
        }
    },
)
