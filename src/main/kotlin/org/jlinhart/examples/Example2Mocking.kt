package org.jlinhart.examples

import kotlin.concurrent.thread

class Example2Mocking(
    private val config: LettersConfig,
    private val letterDao: LetterDao,
) {
    fun archiveImportantLetter(l: Letter) {
        if (letterDao.canSaveMore()) {
            when (l.type) {
                LetterType.PERSONAL -> return
                LetterType.OFFICIAL -> letterDao.save(l)
            }
        }
    }

    fun saveAsync(letter: Letter) {
        thread {
            Thread.sleep(config.delay)
            letterDao.save(letter)
        }
    }
}
