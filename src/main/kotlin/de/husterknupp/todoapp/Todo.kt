package de.husterknupp.todoapp

data class Todo(val fileUrl: String, val lineOfCode: Int, val todoLineStr: String, val context: String) {
    override fun toString(): String = "Todo(file=${fileUrl}" +
            ", lineOfCode=$lineOfCode" +
            ", todoLineStr=$todoLineStr" +
            ", context=..)"
}
