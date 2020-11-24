package ru.vood.hazelcastgraph.run;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
import com.hazelcast.jet.datamodel.WindowResult;
import com.hazelcast.jet.examples.tradesource.Trade;
import com.hazelcast.jet.examples.tradesource.TradeSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;

import java.util.List;

import static com.hazelcast.function.ComparatorEx.comparing;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.topN;
import static com.hazelcast.jet.pipeline.WindowDefinition.sliding;
import static com.hazelcast.jet.pipeline.WindowDefinition.tumbling;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TradeMonitor {

    private static final int TRADES_PER_SEC = 5000;
    private static final long MONITORING_INTERVAL = SECONDS.toMillis(60);
    private static final long REPORTING_INTERVAL = SECONDS.toMillis(5);

    public static void main(String[] args) {
        Pipeline pipeline = definePipeline();
        submitForExecution(pipeline);
    }

    private static Pipeline definePipeline() {
        Pipeline pipeline = Pipeline.create();
        StreamSource<Trade> streamSource = TradeSource.tradeStream(TRADES_PER_SEC);
        StreamStage<Trade> source = pipeline.readFrom(streamSource)
                .withNativeTimestamps(0);

        StreamStage<KeyedWindowResult<String, Long>> tradeCounts = source
                .groupingKey(Trade::getTicker)
                .window(sliding(MONITORING_INTERVAL, REPORTING_INTERVAL))
                .aggregate(counting());

        StreamStage<WindowResult<List<KeyedWindowResult<String, Long>>>> topN = tradeCounts
                .window(tumbling(REPORTING_INTERVAL))
                .aggregate(topN(10, comparing(KeyedWindowResult::result)));

        StreamStage<WindowResult<List<KeyedWindowResult<String, Long>>>> aggregate = tradeCounts
                .window(tumbling(REPORTING_INTERVAL))
                .aggregate(AggregateOperations.toList());
        aggregate
                .map(wrList -> format(wrList.result()))
                .writeTo(Sinks.logger());

        topN.map(wrList -> format(wrList.result()))
                .writeTo(Sinks.logger());

        return pipeline;
    }

    private static String format(List<KeyedWindowResult<String, Long>> results) {
        StringBuilder sb = new StringBuilder("Most active stocks in past minute:");
        for (int i = 0; i < results.size(); i++) {
            KeyedWindowResult<String, Long> result = results.get(i);
            sb.append(String.format("\n\t%2d. %5s - %d trades", i + 1, result.getKey(), result.getValue()));
        }
        return sb.toString();
    }

    private static void submitForExecution(Pipeline pipeline) {
        JetInstance instance = Jet.bootstrappedInstance();
        instance.newJob(pipeline, new JobConfig().setName("trade-monitor"));
    }

}