package hyponome.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import java.net.{InetAddress, URI}
import java.nio.file.{Files, FileSystem, FileSystems, Path}
import java.sql.Timestamp
import spray.json._

object JsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object sha256HashFormat extends RootJsonFormat[SHA256Hash] {
    def write(h: SHA256Hash) = JsObject("SHA256Hash" -> JsString(h.value))
    def read(value: JsValue) = value.asJsObject.getFields("SHA256Hash") match {
      case Seq(JsString(h)) => new SHA256Hash(h)
    }
  }

  implicit object pathJsonFormat extends RootJsonFormat[Path] {
    def write(p: Path) = JsObject("Path" -> JsString(p.toFile.toString))
    def read(value: JsValue) = value.asJsObject.getFields("Path") match {
      case Seq(JsString(p)) => FileSystems.getDefault().getPath(p)
    }
  }

  implicit object InetAddressFormat extends RootJsonFormat[InetAddress] {
    def write(i: InetAddress) = JsObject("InetAddress" -> JsString(i.getHostAddress))
    def read(value: JsValue) = value.asJsObject.getFields("InetAddress") match {
      case Seq(JsString(i)) => InetAddress.getByName(i)
    }
  }

  implicit object URIFormat extends RootJsonFormat[URI] {
    def write(uri: URI) = JsObject("URI" -> JsString(uri.toString))
    def read(value: JsValue) = value.asJsObject.getFields("URI") match {
      case Seq(JsString(uri)) => new URI(uri)
    }
  }

  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {
    def write(t: Timestamp) = JsObject("Timestamp" -> JsString(t.toString))
    def read(value: JsValue) = value.asJsObject.getFields("Timestamp") match {
      case Seq(JsString(t)) => Timestamp.valueOf(t)
    }
  }

  implicit object OperationFormat extends RootJsonFormat[Operation] {
    def write(op: Operation) = op match {
      case Add    => JsObject("Operation" -> JsString("Add"))
      case Remove => JsObject("Operation" -> JsString("Remove"))
    }
    def read(value: JsValue) = value.asJsObject.getFields("Operation") match {
      case Seq(JsString("Add"))    => Add
      case Seq(JsString("Remove")) => Remove
    }
  }

  implicit val additionFormat: RootJsonFormat[Addition] = jsonFormat6(Addition)
  implicit val removalFormat:  RootJsonFormat[Removal]  = jsonFormat2(Removal)
  implicit val fileFormat:     RootJsonFormat[File]     = jsonFormat4(File)
  implicit val eventFormat:    RootJsonFormat[Event]    = jsonFormat5(Event)
  implicit val responseFormat: RootJsonFormat[Response] = jsonFormat6(Response)
}
