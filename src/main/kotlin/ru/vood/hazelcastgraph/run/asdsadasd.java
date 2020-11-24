package ru.vood.hazelcastgraph.run;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.pipeline.SourceBuilder;

import java.util.HashMap;

public class asdsadasd {

    public static void main(String[] args) {
        SourceBuilder
                .timestampedStream("someData", new FunctionEx<Processor.Context, Object>() {
                    @Override
                    public HashMap<String, Object> applyEx(Processor.Context context) throws Exception {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("idx", "1");
                        HashMap<String, Object> valueArray = new HashMap<>();
                        valueArray.put("1", 1);
                        valueArray.put("2", 3);
                        map.put("Array", valueArray);
                        return map;
                    }
                });

    }
}
