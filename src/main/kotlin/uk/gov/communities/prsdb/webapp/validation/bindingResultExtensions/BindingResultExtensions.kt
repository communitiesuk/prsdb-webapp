package uk.gov.communities.prsdb.webapp.validation.bindingResultExtensions

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import uk.gov.communities.prsdb.webapp.models.dataModels.ValidatableDataModel
import kotlin.reflect.full.declaredMemberProperties

fun <T : ValidatableDataModel> BindingResult.toBindingResultWithSingleFieldErrorCodes(dataModel: T): BindingResult {
    val newResult = BeanPropertyBindingResult(dataModel, dataModel::class.simpleName!!)
    for (errorMessage in dataModel.errorPrecedenceList) {
        if (errorMessage in allErrors.map { it.defaultMessage }) {
            val matchingError = allErrors.single { it.defaultMessage == errorMessage }
            newResult.rejectFromValidationError(matchingError, dataModel)
        }
    }
    return newResult
}

private fun <T : ValidatableDataModel> BindingResult.rejectFromValidationError(
    matchingError: ObjectError,
    dataModel: T,
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
