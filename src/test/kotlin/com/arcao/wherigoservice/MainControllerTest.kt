package com.arcao.wherigoservice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MainControllerTest {
    @Autowired
    private lateinit var controller: MainController

    @Test
    fun contextLoads() {
        assertNotNull(controller)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "553e7b68-60fe-470a-baa4-a85e7907a1ef, GC3T33X",
            "569ba1cb-7f33-40f2-baa7-80c97faebfc7, GC7K3JF",
            "adfc33ba-56ce-43c5-a288-97f53407cf5c, GC9K1JC",
            "08f812cb-beea-4683-8bb4-b816dda6ad54, GC8WM6R",
            "a8c83def-cd7e-469e-9ada-8b1a766c8710, GC4805Z"
        ]
    )
    fun guidToReferenceCode(source: String) {
        val (input, expected) = source.split(", ")

        assertEquals(controller.guidToReferenceCode(input).referenceCode, expected)
    }
}
