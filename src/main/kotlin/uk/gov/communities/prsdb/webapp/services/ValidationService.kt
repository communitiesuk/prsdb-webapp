package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.models.dataModels.ValidatableDataModel
import uk.gov.communities.prsdb.webapp.validation.bindingResultExtensions.toBindingResultWithSingleFieldErrorCodes

@Service
class ValidationService(
    val validator: Validator,
) {
    fun <T : ValidatableDataModel> validateDataModel(dataModel: T): BindingResult {
        val binder = WebDataBinder(dataModel, dataModel::class.simpleName!!)
        binder.validator = validator
        binder.validate()
        return binder.bindingResult.toBindingResultWithSingleFieldErrorCodes(dataModel)
    }
}
