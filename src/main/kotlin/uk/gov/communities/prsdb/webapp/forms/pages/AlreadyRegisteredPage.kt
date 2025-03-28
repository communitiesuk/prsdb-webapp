package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class AlreadyRegisteredPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val selectedAddressPathSegment: String,
) : AbstractPage(formModel, templateName, content) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        modelAndView.addObject(
            "singleLineAddress",
            JourneyDataHelper.getFieldStringValue(
                filteredJourneyData!!,
                selectedAddressPathSegment,
                SelectAddressFormModel::class.memberProperties.first().name,
            ),
        )
    }
}
