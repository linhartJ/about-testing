package org.jlinhart.examples

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

/**
 * Test class has the same name + 'Test' suffix as class under test
 * - this makes navigate to test possible
 */
internal class Example1BasicsTest {

    /**
     * Inline instantiation - no dependencies, no mocking
     * val is possible - JUnit will create new instance before each test by default
     */
    private val classUnderTest = Example1Basics()

    /**
     * Most straightforward example of unit test - pure function
     * - deterministic behavior
     * - function returns value based on inputs
     * - no internal state
     *
     * Pure functions are usually very easy to test. Tests are simple and readable.
     */
    @Test
    fun `pure function test`() {
        // given
        val a = 4
        val b = 5

        // when
        val result = classUnderTest.multiply(a, b)

        // then
        assertEquals(20, result)
    }

    /**
     * Sometimes methods do not provide return value but mutate internal state of the tested object.
     * Testing these is relatively easy, but often not desirable, because the test looses connection to
     * use-case.
     *
     * It is much better to identify the use case for which the state even exists for and test it instead of
     * the method in isolation.
     */
    @Test
    fun `testing class state`() {
        // Instead of:
        // fun `addExpense adds value to list of expenses`()
        // given
        val newExpense = 10

        // when
        classUnderTest.addExpense(newExpense)

        // then
        val expenses = classUnderTest.expenses
        assertEquals(newExpense, expenses.last())
        expenses.clear()

        // test like this:
        // fun `totalExpense provides total value accumulated by all added expenses`()
        // given
        val expenseValue = 10
        val repetitions = 5

        // when
        repeat(repetitions) { classUnderTest.addExpense(expenseValue) }

        // then
        assertEquals(expenseValue * repetitions, classUnderTest.totalExpense())
    }

    /**
     * Parametrized tests require 'org.junit.jupiter:junit-jupiter-params' dependency.
     * Test arguments must be provided by static method that is referenced by name. - bit uglier in kotlin than in java
     * Suitable for pure functions...
     */
    @ParameterizedTest(name = "{index}: {0} * {1} = {2}")
    @MethodSource("multiplyInputs") // there are other options, but this is most common & flexible
    fun `parametrized test example`(a: Int, b: Int, expectedResult: Int) {
        // when
        val result = classUnderTest.multiply(a, b)

        // then
        assertEquals(expectedResult, result)
    }

    companion object {
        @JvmStatic
        fun multiplyInputs(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0, 0, 0),
                Arguments.of(0, 1, 0),
                Arguments.of(1, 0, 0),
                Arguments.of(1, 1, 1),
                Arguments.of(1, 5, 5),
                Arguments.of(5, 5, 25),
            )
        }
    }

    /**
     * It is very common to use data classes in kotlin to pass data around.
     * It is very common for data classes to change.
     * We should protect out code from these changes to avoid tedious changes to our test.
     * Inspect given example and try to add email property to Person class
     */
    @Test
    fun `builder classes example`() {
        // Instead of:
        // given
        val julia = Person("Julia", "Roberts", 20 /*age value not needed in test - confusing*/)

        // when
        val juliaResult = classUnderTest.calculateFullNameLength(julia)

        // then
        assertEquals(13, juliaResult)

        // use builder class
        // given
        val bale = aPerson(name = "Christian", surname = "Bale")

        // when
        val baleResult = classUnderTest.calculateFullNameLength(bale)

        // then
        assertEquals(14, baleResult)
    }

    /**
     * Builder functions should like this one should not be part of production code - ideally use test-fixtures.
     * Calling this function without arguments should provide valid object that can appear in the production.
     * Adding new value to underlying data class required you to update only this function instead of potentially many tests.
     * Tests also have the liberty to ignore irrelevant values.
     */
    private fun aPerson(
        name: String = "Rowan",
        surname: String = "Atkinson",
        age: Int = 55
    ): Person {
        return Person(name, surname, age)
    }

    /**
     * Builder classes protect our tests from changes in data classes. There is another technique that accomplishes
     * similar thing and that is a wrapper function for tested method.
     * Wrapper function looks seemingly same as tested method, so it may feel to you like redundant code or DRY violation.
     *
     * Wrapper function protects your tests from changes in tested method signature - like adding a parameter,
     * because you can add newly added parameter to wrapper function with default value. It is VERY common,
     * and generally worth the drawbacks in my opinion...
     *
     * Explore example below, then try to add mode to the method enum class Mode { STANDARD, DISCOUNT } as method param.
     */
    @Test
    fun `decoupling test from implementation by wrapping tested method`() {
        val youngPerson = aPerson(age = 15)

        // when
        val baleResult = calculateInsurance(youngPerson)

        // then
        assertEquals(100, baleResult)
    }

    private fun calculateInsurance(p: Person) = classUnderTest.calculateInsurance(p)
}
