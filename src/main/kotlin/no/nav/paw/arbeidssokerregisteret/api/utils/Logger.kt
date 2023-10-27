package no.nav.paw.arbeidssokerregisteret.api.utils

import org.slf4j.LoggerFactory

inline val <reified T : Any> T.logger get() = LoggerFactory.getLogger(T::class.java.name)
