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
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.BetaFeedbackModel
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService

@PrsdbController
@RequestMapping
class BetaFeedbackController(
    private val emailService: NotifyEmailNotificationService<BetaFeedbackEmail>,
) {
    @GetMapping(LANDLORD_FEEDBACK_URL, FEEDBACK_URL)
    fun landlordFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedback(model, request)

    @GetMapping(LANDLORD_FEEDBACK_SUCCESS_URL)
    fun landlordFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedbackSuccess(model, request)

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping(LOCAL_AUTHORITY_FEEDBACK_URL)
    fun localAuthorityFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedback(model, request)

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping(LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL)
    fun localAuthorityFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String = renderFeedbackSuccess(model, request)

    @PostMapping(LANDLORD_FEEDBACK_URL, FEEDBACK_URL)
    fun submitLandlordFeedback(
        @Valid @ModelAttribute("formModel") betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String =
        handleFeedbackSubmission(
            betaFeedbackModel,
            bindingResult,
            model,
            request,
            LANDLORD_FEEDBACK_SUCCESS_URL,
        )

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @PostMapping(LOCAL_AUTHORITY_FEEDBACK_URL)
    fun submitLocalAuthorityFeedback(
        @Valid @ModelAttribute("formModel") betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String =
        handleFeedbackSubmission(
            betaFeedbackModel,
            bindingResult,
            model,
            request,
            LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL,
        )

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

        val escapeRegex = Regex("""([\[\]\(\)])""")
        val escapedFeedback = betaFeedbackModel.feedback.replace(escapeRegex, """\\$1""")

        val feedbackEmail =
            BetaFeedbackEmail(
                feedback = escapedFeedback,
                email = betaFeedbackModel.email,
                referrer = betaFeedbackModel.referrerHeader,
            )
        // TODO: PRSD-1441 - email needs updating with env variable
        emailService.sendEmail("Team-PRSDB@Softwire.com", feedbackEmail)
        return "redirect:$redirectPath"
    }

    companion object {
        const val LANDLORD_FEEDBACK_URL = "/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}"
        const val LANDLORD_FEEDBACK_SUCCESS_URL = "/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}"
        const val LOCAL_AUTHORITY_FEEDBACK_URL = "/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}"
        const val LOCAL_AUTHORITY_FEEDBACK_SUCCESS_URL = "/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}"
        const val FEEDBACK_URL = "/${FEEDBACK_PATH_SEGMENT}"
    }
}
