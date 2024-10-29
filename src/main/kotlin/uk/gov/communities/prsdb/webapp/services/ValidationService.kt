package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel
import kotlin.reflect.full.declaredMemberProperties

@Service
class ValidationService(
    val validator: Validator,
) {
    fun validateDataModel(dataModel: ConfirmedEmailDataModel): BindingResult {
        val binder = WebDataBinder(dataModel, ConfirmedEmailDataModel::class.simpleName!!)
        binder.validator = validator
        binder.validate()
        return binder.bindingResult.toBindingResultWithSingleFieldErrorCodes(dataModel)
    }
}

fun BindingResult.toBindingResultWithSingleFieldErrorCodes(dataModel: ConfirmedEmailDataModel): BindingResult {
    val newResult = BeanPropertyBindingResult(dataModel, dataModel::class.simpleName!!)
    for (errorMessage in dataModel.errorPrecedenceList) {
        if (errorMessage in allErrors.map { it.defaultMessage }) {
            val matchingError = allErrors.single { it.defaultMessage == errorMessage }
            newResult.rejectFromValidationError(matchingError, dataModel)
        }
    }
    return newResult
}

private fun BindingResult.rejectFromValidationError(
    matchingError: ObjectError,
    dataModel: ConfirmedEmailDataModel,
) {
    if (matchingError is FieldError && matchingError.field in dataModel::class.declaredMemberProperties.map { it.name }) {
        rejectValueIfNotRejected(
            matchingError.field,
            matchingError.defaultMessage ?: "",
        )
    } else {
        rejectValueIfNotRejected(
            dataModel.errorFieldMap[matchingError.defaultMessage] ?: "",
            matchingError.defaultMessage ?: "",
        )
    }
}

private fun BindingResult.rejectValueIfNotRejected(
    fieldName: String,
    errorCode: String,
) {
    if (!this.hasFieldErrors(fieldName)) {
        rejectValue(fieldName, errorCode)
    }
}
