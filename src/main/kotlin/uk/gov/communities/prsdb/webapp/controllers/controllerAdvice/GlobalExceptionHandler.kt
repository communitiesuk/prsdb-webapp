package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.springframework.web.bind.annotation.ExceptionHandler
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.CYA_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.UPDATE_CONFLICT_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException

@PrsdbControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CyaDataHasChangedException::class)
    fun handleCyaDataHasChangedException(): String = "redirect:$CYA_ERROR_ROUTE"

    @ExceptionHandler(UpdateConflictException::class)
    fun handleUpdateConflictException(): String = "redirect:$UPDATE_CONFLICT_ERROR_ROUTE"
}
