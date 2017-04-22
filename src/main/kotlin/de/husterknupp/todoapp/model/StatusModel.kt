package de.husterknupp.todoapp.model

class StatusOverviewResponse {

    val services = mutableListOf<ServiceStatus>()
}

class ServiceStatus {

    var name: String? = null

    var status: Int? = null
}
