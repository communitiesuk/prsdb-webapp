package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SUCCESS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.BetaFeedbackModel

@PrsdbController
@RequestMapping
class BetaFeedbackController {
    private fun renderFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String {
        val formModel = BetaFeedbackModel()
        model.addAttribute("formModel", formModel)
        val referrer = request.getHeader("referer")
        model.addAttribute("referrerHeader", referrer)
        model.addAttribute("backUrl", referrer)
        return "betaBannerFeedback"
    }

    private fun renderFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String {
        model.addAttribute("backUrl", request.getHeader("referer"))
        return "betaBannerFeedbackSuccess"
    }

    private fun handleFeedbackSubmission(
        betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
        redirectPath: String,
    ): String {
        if (bindingResult.hasErrors()) {
            val referrer = request.getHeader("referer")
            model.addAttribute("referrerHeader", referrer)
            model.addAttribute("backUrl", referrer)
            model.addAttribute("formModel", betaFeedbackModel)
            return "betaBannerFeedback"
        }
        return "redirect:$redirectPath"
    }

    @GetMapping("/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun landlordFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedback(model, request)

    @GetMapping("/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}")
    fun landlordFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedbackSuccess(model, request)

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun localAuthorityFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedback(model, request)

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}")
    fun localAuthorityFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedbackSuccess(model, request)

    // TODO: These are so that the feedback/success pages can be viewed locally, will need to be removed before production
    // worth noting that you need to manually go to /feedback/success
    @GetMapping("/${FEEDBACK_PATH_SEGMENT}")
    fun feedback(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedback(model, request)

    @GetMapping("/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}")
    fun feedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedbackSuccess(model, request)

    @PostMapping("/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun submitLandlordFeedback(
        @Valid @ModelAttribute betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String =
        handleFeedbackSubmission(
            betaFeedbackModel,
            bindingResult,
            model,
            request,
            "/$LANDLORD_PATH_SEGMENT/$FEEDBACK_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT",
        )

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @PostMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun submitLocalAuthorityFeedback(
        @Valid @ModelAttribute betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String =
        handleFeedbackSubmission(
            betaFeedbackModel,
            bindingResult,
            model,
            request,
            "/$LOCAL_AUTHORITY_PATH_SEGMENT/$FEEDBACK_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT",
        )

    @PostMapping("/${FEEDBACK_PATH_SEGMENT}")
    fun submitFeedback(
        @Valid @ModelAttribute betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String =
        handleFeedbackSubmission(
            betaFeedbackModel,
            bindingResult,
            model,
            request,
            "/$FEEDBACK_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT",
        )
}
