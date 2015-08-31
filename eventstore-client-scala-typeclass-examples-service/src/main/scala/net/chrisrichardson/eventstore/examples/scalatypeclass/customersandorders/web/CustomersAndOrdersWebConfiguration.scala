package net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web

import java.util
import javax.annotation.PostConstruct

import net.chrisrichardson.eventstore.client.config.EventStoreHttpClientConfiguration
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomersConfiguration
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.{OrdersConfiguration, CustomerAndOrderWorkflowConfiguration}
import net.chrisrichardson.eventstore.json.EventStoreCommonObjectMapping
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.HttpMessageConverters
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Bean, Import, Configuration}
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import scala.collection.JavaConversions._

@SpringBootApplication
@Import(Array(classOf[CustomersConfiguration],
  classOf[OrdersConfiguration], classOf[EventStoreHttpClientConfiguration]))
@EnableAutoConfiguration(exclude=Array(classOf[DataSourceAutoConfiguration]))
class CustomersAndOrdersWebConfiguration {


  @Bean
  def scalaJSonConverter: HttpMessageConverters = {
    val additional  = new MappingJackson2HttpMessageConverter
    additional.setObjectMapper(EventStoreCommonObjectMapping.getObjectMapper)
    new HttpMessageConverters(additional)
  }

  // @PostConstruct had issues
  //  @Autowired
  //  var adapter : RequestMappingHandlerAdapter = _

  @Bean
  def dummyClass(adapter : RequestMappingHandlerAdapter) : InitializingBean = {
    // https://jira.spring.io/browse/SPR-13083
    val handlers = new util.ArrayList[HandlerMethodReturnValueHandler](adapter.getReturnValueHandlers)
    handlers.add(0, new ScalaFutureReturnValueHandler())
    adapter.setReturnValueHandlers(handlers)
    new InitializingBean {
      def afterPropertiesSet() = {}
    }
  }


}
