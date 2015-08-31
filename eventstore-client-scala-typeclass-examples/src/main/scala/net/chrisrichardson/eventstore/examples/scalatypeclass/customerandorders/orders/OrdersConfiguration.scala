package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.client.scalatypeclass.EventStoreScalaTypeClassClientConfiguration
import org.springframework.context.annotation.{Import, ComponentScan, Configuration}

@Configuration
@ComponentScan
@Import(Array(classOf[EventStoreScalaTypeClassClientConfiguration]))
class OrdersConfiguration {

}
