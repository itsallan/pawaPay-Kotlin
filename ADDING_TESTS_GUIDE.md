# Guide: Adding New Tests to PawaPay Kotlin SDK

## Quick Reference

```kotlin
// 1. Create test file in appropriate directory
shared/src/commonTest/kotlin/io/dala/pawapaykotlin/[component]/YourTest.kt

// 2. Use this template
package io.dala.pawapaykotlin.[component]

import kotlin.test.*
import kotlinx.coroutines.test.runTest

class YourFeatureTest {
    
    @BeforeTest
    fun setup() {
        // Initialize test fixtures
    }
    
    @Test
    fun testFeatureHappyPath() {
        // Arrange
        val input = createTestInput()
        
        // Act
        val result = performOperation(input)
        
        // Assert
        assertEquals(expected, result)
    }
    
    @Test
    fun testFeatureErrorCase() = runTest {
        // Test error handling
        assertFailsWith<Exception> {
            performOperation(invalidInput)
        }
    }
    
    @AfterTest
    fun teardown() {
        // Clean up if needed
    }
}
```

## Directory Structure

Place tests in the corresponding directory: