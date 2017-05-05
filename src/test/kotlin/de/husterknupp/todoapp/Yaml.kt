package de.husterknupp.todoapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

class Yaml(fileName: String, yamlProperty: String) {

    val yamlPart: Map<String, Any>

    init {
        val yamlFile = File(fileName)
        if (!yamlFile.isFile) {
            throw RuntimeException("yaml file $fileName not available")
        }

        val objectMapper = ObjectMapper(YAMLFactory())
        objectMapper.registerModule(KotlinModule())
        yamlPart = objectMapper.readValue<Map<String, Map<String, Any>>>(yamlFile.readText())[yamlProperty]!!
    }

    fun get(propertyName: String): String {
        return yamlPart[propertyName].toString()
    }

}
