package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel

data class PropertyManagerDataModel(
    val name: String,
    val email: String,
) {
    companion object {
        fun fromFormData(formModel: PropertyManagerDetailsFormModel): PropertyManagerDataModel =
            PropertyManagerDataModel(
                name = formModel.notNullValue(PropertyManagerDetailsFormModel::name),
                email = formModel.notNullValue(PropertyManagerDetailsFormModel::emailAddress),
            )
    }
}
