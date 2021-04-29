package org.jlinhart.examples

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

    fun calculateInsurance(p: Person): Int {
        return when {
            p.age < 20 -> 100
            p.age < 50 -> 125
            else -> 200
        }
    }
}

data class Person(
    val name: String,
    val surname: String,
    val age: Int
)
