package com.example.demo

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.relational.core.mapping.Table
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.concurrent.TimeUnit


@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Autowired
private lateinit var metricLogger: MetricLogger

@RestController
class MessageResource(val service: MessageService) {
    @GetMapping
    fun index(): List<Message> = service.findMessages()


    @PostMapping
    fun post(@RequestBody message: Message) {
        service.post(message)
        metricLogger.requestReceived()
    }
}

@Table("MESSAGES")
data class Message(val id: String?, val text: String)

interface MessageRepository : CrudRepository<Message, String>{

    @Query("select * from messages")
    fun findMessages(): List<Message>
}

@Service
class MessageService(val db: MessageRepository) {

    fun findMessages(): List<Message> = db.findMessages()

    fun post(message: Message){
        db.save(message)
    }
}



@Component
class MetricLogger(registry: MeterRegistry) {
    private var requestReceivedCounter: Counter = Counter
        .builder("request_received")
        .tag("app","my-app")
        .register(registry)

    fun requestReceived() = requestReceivedCounter.increment()
}
