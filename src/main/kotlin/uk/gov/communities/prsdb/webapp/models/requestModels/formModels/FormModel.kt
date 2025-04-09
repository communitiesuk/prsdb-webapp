package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.forms.PageData
import kotlin.reflect.full.memberProperties

interface FormModel {
    open fun toPageData(): PageData = this::class.memberProperties.associate { it.name to it.getter.call(this) }
}
