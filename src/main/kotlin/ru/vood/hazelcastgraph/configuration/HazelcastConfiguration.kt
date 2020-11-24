package ru.vood.hazelcastgraph.configuration

import com.hazelcast.config.Config
import com.hazelcast.config.EvictionPolicy
import com.hazelcast.config.MapConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HazelcastConfiguration {
    @Bean
    fun hazelCastConfig(): Config {
        val config = Config()
        config.setInstanceName("hazelcast-instance") // hazel case instance name
                .addMapConfig(
                        MapConfig() // create map
                                .setName("configuration")
//                                .setMaxSizeConfig(MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
//                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setTimeToLiveSeconds(-1)) // cache will be available until it will remove manually. less then 0 means never expired.
        return config
    }
}