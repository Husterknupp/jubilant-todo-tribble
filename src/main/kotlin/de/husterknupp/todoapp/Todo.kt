package de.husterknupp.todoapp

data class Todo(val fileUrl: String
                , val lineOfCode: Int
                , val todoLineStr: String
                , val context: String
                , val noticedByJira: Boolean
                , val jiraIssueId: String) {

    override fun toString(): String = "Todo(file=${fileUrl}" +
            ", lineOfCode=$lineOfCode" +
            ", todoLineStr=$todoLineStr" +
            ", context=.." +
            ", noticedByJira=$noticedByJira" +
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
