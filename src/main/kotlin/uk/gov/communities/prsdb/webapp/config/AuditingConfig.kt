package uk.gov.communities.prsdb.webapp.config

import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebConfiguration

@PrsdbWebConfiguration
@EnableJpaAuditing
class AuditingConfig
