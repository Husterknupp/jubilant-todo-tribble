package de.husterknupp.todoapp

enum class TodoState{
    NEW_NOT_NOTIFIED, NEW_NOTIFIED,
    REMOVED_NOT_NOTIFIED, REMOVED_NOTIFIED
}

data class Todo(val fileUrl: String
                , val lineOfCode: Int
                , val todoLineStr: String
                , val context: String
                , val state: TodoState = TodoState.NEW_NOT_NOTIFIED
                , val jiraIssueId: String = "") {

    fun isNew(): Boolean {
        return this.state == TodoState.NEW_NOT_NOTIFIED
    }

    override fun toString(): String = "Todo(file=${fileUrl}" +
            ", lineOfCode=$lineOfCode" +
            ", todoLineStr=$todoLineStr" +
            ", context=.." +
            ", state=$state" +
            ", jiraIssueId: $jiraIssueId)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Todo

        if (fileUrl != other.fileUrl) return false
        if (todoLineStr != other.todoLineStr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileUrl.hashCode()
        result = 31 * result + todoLineStr.hashCode()
        return result
    }
}
