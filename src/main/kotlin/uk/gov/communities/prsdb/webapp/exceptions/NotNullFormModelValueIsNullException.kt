package uk.gov.communities.prsdb.webapp.exceptions

import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KProperty1

class NotNullFormModelValueIsNullException : PrsdbWebException {
    constructor(message: String) : super(message)
    constructor(property: KProperty1<*, *>) : super(property.name + " in journey state is null when it was expected to have a value")

    companion object {
        fun <TFormModel : FormModel, T> TFormModel.notNullValue(prop: KProperty1<TFormModel, T?>): T =
            prop.get(this) ?: throw NotNullFormModelValueIsNullException(prop)
    }
}
