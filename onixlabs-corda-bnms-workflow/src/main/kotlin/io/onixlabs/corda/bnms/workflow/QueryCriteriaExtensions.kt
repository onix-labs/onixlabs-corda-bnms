package io.onixlabs.corda.bnms.workflow

import net.corda.core.node.services.vault.*
import net.corda.core.schemas.StatePersistable

internal val MAX_PAGE_SPECIFICATION: PageSpecification
    get() = PageSpecification(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE)