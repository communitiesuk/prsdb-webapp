package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import java.util.Optional

class StepUrlBuilder {
    companion object {
        fun <T : StepId> getStepUrl(
            stepId: T,
            subPageNumber: Int?,
        ): String =
            UriComponentsBuilder
                .newInstance()
                .path(stepId.urlPathSegment)
                .queryParamIfPresent("subpage", Optional.ofNullable(subPageNumber))
                .build(true)
                .toUriString()
    }
}
