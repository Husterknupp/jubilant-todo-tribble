package de.husterknupp.todoapp.model

class AuthenticationResult {

    var success = false

    var user: String? = null

    val roles = mutableListOf<String>()

    var jwt: String? = null
}
