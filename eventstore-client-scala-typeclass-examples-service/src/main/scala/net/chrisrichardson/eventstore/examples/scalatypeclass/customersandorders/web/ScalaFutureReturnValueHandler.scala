package net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web

import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.async.{DeferredResult, WebAsyncUtils}
import org.springframework.web.method.support.{ModelAndViewContainer, HandlerMethodReturnValueHandler}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class ScalaFutureReturnValueHandler extends HandlerMethodReturnValueHandler {

  override def supportsReturnType(returnType: MethodParameter): Boolean =
    classOf[Future[_]].isAssignableFrom(returnType.getParameterType)

  override def handleReturnValue(returnValue: Any, 
                                 returnType: MethodParameter, 
                                 mavContainer: ModelAndViewContainer, 
                                 webRequest: NativeWebRequest): Unit = {
    if (returnValue != null) {
      val d = new DeferredResult[Any]()
      returnValue.asInstanceOf[Future[_]] onComplete {
        case Success(x) => d.setResult(x)
        case Failure(t) => d.setErrorResult(t)
      }
      WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(d, mavContainer)
    }

  }
}
