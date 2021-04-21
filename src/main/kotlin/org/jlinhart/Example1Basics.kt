package org.jlinhart

class Example1Basics {

    val expenses = mutableListOf<Int>()

    fun multiply(a: Int, b: Int): Int {
        return a * b
    }

    fun addExpense(value: Int) {
        expenses.add(value)
    }

    fun totalExpense(): Int = expenses.sum()

    fun calculateFullNameLength(p: Person): Int {
        return "${p.name} ${p.surname}".length
    }
}

data class Person(
    val name: String,
    val surname: String,
    val age: Int
)
