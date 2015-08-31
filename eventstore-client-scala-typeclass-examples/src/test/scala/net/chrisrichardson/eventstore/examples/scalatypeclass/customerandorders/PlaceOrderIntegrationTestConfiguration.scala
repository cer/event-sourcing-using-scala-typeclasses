package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders

import net.chrisrichardson.eventstore.client.scalatypeclass.EventStoreScalaTypeClassClientConfiguration
import net.chrisrichardson.eventstore.jdbc.config.JdbcEventStoreConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{ComponentScan, Import, Configuration}

@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[JdbcEventStoreConfiguration], classOf[EventStoreScalaTypeClassClientConfiguration]))
@ComponentScan
class PlaceOrderIntegrationTestConfiguration {

}
