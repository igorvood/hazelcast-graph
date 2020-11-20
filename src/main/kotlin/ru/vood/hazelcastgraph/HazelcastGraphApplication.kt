package ru.vood.hazelcastgraph

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HazelcastGraphApplication

fun main(args: Array<String>) {
    runApplication<HazelcastGraphApplication>(*args)
}
