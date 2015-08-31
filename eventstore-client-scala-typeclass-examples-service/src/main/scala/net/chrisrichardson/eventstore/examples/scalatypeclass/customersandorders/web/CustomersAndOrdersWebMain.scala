package net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web

import org.springframework.boot.SpringApplication

object CustomersAndOrdersWebMain extends App {

  SpringApplication.run(classOf[CustomersAndOrdersWebConfiguration], args :_ *)

}
