package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore.EntityEventStore
import net.chrisrichardson.eventstore.eventserde.{DefaultEventSerde, EventSerde}
import net.chrisrichardson.eventstore.idgeneration.IdGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class EventStoreScalaTypeClassClientConfiguration {

  @Autowired(required=false)
  var eventSerde : EventSerde = DefaultEventSerde

  @Bean
  def eventStoreToEntityStoreAdapter(entityEventStore : EntityEventStore, idGenerator : IdGenerator) : EventStore =
    new EventStoreToEntityStoreAdapter(entityEventStore, idGenerator, eventSerde)
}
