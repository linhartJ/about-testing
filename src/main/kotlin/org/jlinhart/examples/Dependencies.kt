package org.jlinhart.examples

data class Letter(
    val title: String,
    val content: String,
    val type: LetterType
)

enum class LetterType { PERSONAL, OFFICIAL }
class LetterDao {
    fun save(l: Letter): Letter {
        return l
    }

    fun getAll(): Collection<Letter> {
        return emptyList()
    }

    fun getLetterByTitle(title: String): Letter {
        return Letter(title, "Hello all", LetterType.PERSONAL)
    }

    fun canSaveMore(): Boolean {
        return true
    }
}

data class LettersConfig(
    val smtpServer: String,
    val delay: Long
)
