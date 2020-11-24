package ru.vood.hazelcastgraph.run

import com.hazelcast.function.FunctionEx
import com.hazelcast.jet.aggregate.AggregateOperations
import com.hazelcast.jet.core.Processor
import com.hazelcast.jet.datamodel.KeyedWindowResult
import com.hazelcast.jet.examples.tradesource.Trade
import com.hazelcast.jet.examples.tradesource.TradeSource
import com.hazelcast.jet.pipeline.Pipeline
import com.hazelcast.jet.pipeline.SourceBuilder
import com.hazelcast.jet.pipeline.SourceBuilder.TimestampedSourceBuffer
import com.hazelcast.jet.pipeline.StreamStage
import com.hazelcast.jet.pipeline.WindowDefinition
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service


@Service
class Runner : CommandLineRunner {

    override fun run(vararg args: String?) {

        val pipeline = Pipeline.create()
        val build = SourceBuilder
                .timestampedStream("trade-source"
                ) { x: Processor.Context? -> SomeData("f1", "f2", "f3") }
                .fillBufferFn { obj: SomeData, buf: TimestampedSourceBuffer<SomeData> -> buf.add(obj, System.currentTimeMillis()) }
                .build()
        val readFrom = pipeline.readFrom(build).withIngestionTimestamps()

        val withIngestionTimestamps = readFrom
//        пример
        val streamSource = TradeSource.tradeStream(5000)
        val source = pipeline.readFrom(streamSource)
                .withNativeTimestamps(0)

        val tradeCounts: StreamStage<KeyedWindowResult<String, Long>> = source
                .groupingKey<String>(FunctionEx { obj: Trade -> obj.ticker })
                .window(WindowDefinition.sliding(4234324, 657))
                .aggregate<Long>(AggregateOperations.counting<Trade>())
//        пример





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