package com.library.catalog.bdd

import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MvcResult

@Component
class ScenarioState {
    var lastMvcResult: MvcResult? = null

    fun clear() {
        lastMvcResult = null
    }
}
