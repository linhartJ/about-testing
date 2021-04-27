package org.jlinhart.examples

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.verification.VerificationMode
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mockito Extension allows mocks setup with annotations @Mock, @Spy, @Captor
 */
@ExtendWith(MockitoExtension::class)
internal class Example2MockingTest {

    /**
     * Classes instantiated by mockito cannot be final
     * - lateinit var is a bit ugly but at least the object is not nullable
     */
    @Mock
    private lateinit var letterDao: LetterDao

    /**
     * If you are not annotations fan, these are standard ways to create mocks programmatically
     * These would not be picked up by
     */
//    private val letterDaoAlt1 = Mockito.mock(LetterDao::class.java) // mockito core
//    private val letterDaoAlt2 = mock<LetterDao>() // mockito-kotlin syntax

    /**
     * In come cases we don't want to mock the dependency, we can create actual instance
     * Notice no lateinit var is needed.
     * @Spy - not really sure what its supposed to be for, but @InjectMocks can use this, I never used it for anything else (at least successfully)
     */
    @Spy
    private val config = LettersConfig("smtp.quadient.test")

    /**
     * @InjectMocks will attempt class instantiation with available declared @Mock and @Spy classes
     * advantages:
     * - no constructor call with explicitly declared dependencies
     * - order of dependencies does not matter
     * disadvantages:
     * - when removing dependencies from class declared mock can be forgotten in test
     */
    @InjectMocks
    private lateinit var classUnderTest: Example2Mocking

    /**
     * Mocking is forcing test doubles (mocks) to behave in predictable way in order to control
     * test flow.
     *
     * Mocking is practically speaking always heavily tied to the implementation. That is unfortuned for
     * test clarity & readability.
     *
     * Mockito will fail your test by default if you mock something that the production code does not invoke.
     * You can work around this by setting the mock or the mocking itself 'lenient'. Don't over-do it.
     */
    private fun `how to mock things with mockito`() {
        // use methods from mockito-kotlin library when possible
        // 1. given-willReturn
        given(letterDao.getAll()).willReturn(emptyList())

        // 2. given-willReturn when invoked multiple times. 
        // Last given response would be returned on any subsequent invocation
        val firstResponse = aLetter()
        val secondResponse = aLetter()
        given(letterDao.getLetterByTitle(any())).willReturn(firstResponse, secondResponse)

        // 3. given-willAnswer - allow us to make the return value dynamic base on some logic
        given(letterDao.getLetterByTitle(any())).willAnswer {
            val requestedLetterTitle = it.getArgument<String>(0)
            when (requestedLetterTitle) {
                "My love" -> aLetter(title = requestedLetterTitle, type = LetterType.PERSONAL)
                "Greetings" -> aLetter(title = requestedLetterTitle, type = LetterType.OFFICIAL)
                else -> aLetter(title = requestedLetterTitle)
            }
        }

        // 4. restricting scope of the mock
        // 4.a by explicit argument value - mocked value will be returned only when provided argument is of that value
        given(letterDao.getLetterByTitle("Invitation")).willReturn(aLetter())
        // 4.b by matcher like any() or eq(..)
        given(letterDao.getLetterByTitle(eq("Invitation"))).willReturn(aLetter())
        // You cannot mix 4.a & 4.b
        // Beginner mistake - be mindful that nullable arguments must be matched by anyOrNull() matcher and not any()

        // 5. fancy mocking during mock initialization
        mock<LetterDao> {
            on { getAll() } doReturn emptyList()
        }
        // instead of
        val mock = mock<LetterDao>()
        given(mock.getAll()).willReturn(emptyList())
    }

    /**
     * The best assertion will always be direct verification of returned value.
     * Verifying interactions on mocks is sometimes needed, but we should try to avoid it and not over-use it.
     */
    private fun `how to verify mocks interactions with mockito`() {
        // 1. simple verification - mockito by default fails the test if the mock does not register exactly 1 specified interaction.
        verify(letterDao).getAll()

        // 2. verification matching function arguments
        // - its the same as specifying arguments in given - values or matchers, no mix
        val l = aLetter()
        verify(letterDao).save(eq(l))

        // 3. verification mode
        // - second argument to verify function - times(1) by default
        verify(letterDao, atLeastOnce()).getAll()
        verify(letterDao, times(3)).getAll()
        verify(letterDao, never()).getAll()
    }

    /**
     * Lets test archiveImportantLetter method.
     * Observe, how mocking & verifications can make a test cluttered with technical details.
     *
     * Note: it gets worse and worse with every mock interaction
     * Note: every change in mocked/verified method will break test compilation
     */
    @Test
    fun `archiveImportantLetter saves OFFICIAL letters given there is capacity in storage`() {
        // given
        val letter = aLetter(type = LetterType.OFFICIAL)
        given(letterDao.canSaveMore()).willReturn(true)

        // when
        classUnderTest.archiveImportantLetter(letter)

        // then
        verify(letterDao).save(letter)
    }

    @Test
    fun `improved archiveImportantLetter saves OFFICIAL letters given there is capacity in storage`() {
        // given
        val officialLetter = aLetter(type = LetterType.OFFICIAL)
        givenStorageCapacityAvailable()

        // when
        archiveImportantLetter(officialLetter)

        // then
        assertLetterArchived()
    }

    @Test
    fun `improved archiveImportantLetter does not save PERSONAL letters despite there is capacity in storage`() {
        // given
        val personalLetter = aLetter(type = LetterType.PERSONAL)
        givenStorageCapacityAvailable()

        // when
        archiveImportantLetter(personalLetter)

        // then
        assertLetterArchived(never())
    }

    /**
     * wrapper methods
     * - provides name semantically closer to use-case
     * - decouples the test-case from implementation details
     * - decouples the test from mockito
     * - can wrap multiple given statements (when it makes sense)
     */
    private fun givenStorageCapacityAvailable(available: Boolean = true) {
        given(letterDao.canSaveMore()).willReturn(available)
    }

    private fun assertLetterArchived(mode: VerificationMode = times(1)) {
        verify(letterDao, mode).save(any())
    }

    private fun archiveImportantLetter(letter: Letter) {
        classUnderTest.archiveImportantLetter(letter)
    }

    private val counter = AtomicInteger(0)
    private fun aLetter(
        title: String = "title-${counter.incrementAndGet()}",
        content: String = "some content",
        type: LetterType = LetterType.PERSONAL
    ): Letter {
        return Letter(title, content, type)
    }
}
