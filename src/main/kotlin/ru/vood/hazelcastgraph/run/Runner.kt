package ru.vood.hazelcastgraph.run

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.jet.datamodel.KeyedWindowResult
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit.SECONDS


@Service
class Runner(
        val applicationContext: ApplicationContext,
        val instance: HazelcastInstance) : CommandLineRunner {

    private val TRADES_PER_SEC = 5000
    private val MONITORING_INTERVAL: Long = SECONDS.toMillis(60)
    private val REPORTING_INTERVAL: Long = SECONDS.toMillis(5)

    override fun run(vararg args: String?) {
        val filter = applicationContext.beanDefinitionNames
                .filter { it.contains("hazel") }

        val stringStringMap = instance.getMap<String, String>("configuration")
        stringStringMap["data"] = "values" // write value, This value will be accessible from another jvm also
        val get = stringStringMap.get("data")



        println(filter)

    }

    private fun format(results: List<KeyedWindowResult<String, Long>>): String? {
        val sb = StringBuilder("Most active stocks in past minute:")
        for (i in results.indices) {
            val result = results[i]
            sb.append(String.format("\n\t%2d. %5s - %d trades", i + 1, result.key, result.value))
        }
        return sb.toString()
    }
}