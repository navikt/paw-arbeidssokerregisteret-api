package no.nav.paw.arbeidssoekerregisteret.app

import org.slf4j.LoggerFactory
import kotlin.system.exitProcess


fun main() {
    val logger = LoggerFactory.getLogger("app")
    logger.info("Starter")
    logger.info("Avsluttet")
    exitProcess(1)
}