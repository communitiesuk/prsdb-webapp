package uk.gov.communities.prsdb.webapp.constants

import org.springframework.web.servlet.support.ServletUriComponentsBuilder

val PRSD_BASE_URI = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
