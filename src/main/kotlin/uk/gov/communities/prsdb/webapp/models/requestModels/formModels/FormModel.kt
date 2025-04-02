package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.forms.ExcludeFromPageData
import uk.gov.communities.prsdb.webapp.forms.PageData
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

interface FormModel {
    open fun toPageData(): PageData =
        this.javaClass.kotlin.memberProperties
            .filter { !it.hasAnnotation<ExcludeFromPageData>() }
            .associate { it.name to it.get(this) }
}
