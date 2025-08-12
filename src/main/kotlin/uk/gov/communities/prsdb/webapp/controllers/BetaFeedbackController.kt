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
import uk.gov.communities.prsdb.webapp.constants.BETA_BANNER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SUCCESS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.BetaFeedbackModel

@PrsdbController
@RequestMapping
class BetaFeedbackController {
    @GetMapping("/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun landlordFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String {
        val formModel = BetaFeedbackModel()
        model.addAttribute("betaFeedbackModel", formModel)
        model.addAttribute("referrerHeader", request.getHeader("referer"))
        model.addAttribute("backUrl", request.getHeader("referer"))
        return "betaBannerFeedback"
    }

    @GetMapping("/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}")
    fun landlordFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String {
        model.addAttribute("backUrl", request.getHeader("referer"))
        return "betaBannerFeedbackSuccess"
    }

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun localAuthorityFeedback(
        model: Model,
        request: HttpServletRequest,
    ): String {
        val formModel = BetaFeedbackModel()
        model.addAttribute("betaFeedbackModel", formModel)
        model.addAttribute("referrerHeader", request.getHeader("referer"))
        model.addAttribute("backUrl", request.getHeader("referer"))
        return "betaBannerFeedback"
    }

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @GetMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}")
    fun localAuthorityFeedbackSuccess(
        model: Model,
        request: HttpServletRequest,
    ): String {
        model.addAttribute("backUrl", request.getHeader("referer"))
        return "betaBannerFeedbackSuccess"
    }

    @PostMapping("/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun submitLandlordFeedback(
        @Valid @ModelAttribute betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("referrerHeader", request.getHeader("referer"))
            return "betaBannerFeedback"
        }
        return "redirect:/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}"
    }

    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    @PostMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}")
    fun submitLocalAuthorityFeedback(
        @Valid @ModelAttribute betaFeedbackModel: BetaFeedbackModel,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("referrerHeader", request.getHeader("referer"))
            return "betaBannerFeedback"
        }
        return "redirect:/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}/${SUCCESS_PATH_SEGMENT}"
    }

    @RequestMapping("/${LANDLORD_PATH_SEGMENT}/$BETA_BANNER_PATH_SEGMENT")
    fun routeLandlordFeedback(): String = "redirect:/${LANDLORD_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}"

    @RequestMapping("/${LOCAL_AUTHORITY_PATH_SEGMENT}/$BETA_BANNER_PATH_SEGMENT")
    @PreAuthorize("hasAnyRole('LA_USER', 'LA_ADMIN')")
    fun routeLocalAuthorityFeedback(): String = "redirect:/${LOCAL_AUTHORITY_PATH_SEGMENT}/${FEEDBACK_PATH_SEGMENT}"
}
