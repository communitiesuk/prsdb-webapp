package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.springframework.web.bind.annotation.ExceptionHandler
import uk.gov.communities.prsdb.webapp.annotations.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.CYA_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException

@PrsdbControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CyaDataHasChangedException::class)
    fun handleCyaDataHasChangedException(): String = "redirect:$CYA_ERROR_ROUTE"
}
