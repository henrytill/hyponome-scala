package hyponome.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{TestActors, TestKit, ImplicitSender}
import hyponome.core._
import java.net.InetAddress
import java.nio.file._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.util.{Failure, Success}

class FileActorSpec(_system: ActorSystem) extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("FileActorSpec"))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val fs: FileSystem = FileSystems.getDefault()

  val tempStorePath: Path = fs.getPath("/tmp/hyponome/store")

  val testPDF: Path = {
    val s: String = getClass.getResource("/test.pdf").getPath
    fs.getPath(s)
  }

  val testPDFHash = SHA256Hash(
    "eba205fb9114750b2ce83db62f9c2a15dd068bcba31a2de32d8df7f7c8d85441"
  )

  val ip: Option[InetAddress] = Some(InetAddress.getByName("192.168.1.253"))

  val add = Addition(
    testPDF,
    getSHA256Hash(testPDF),
    testPDF.toFile.getName,
    "application/octet-stream",
    testPDF.toFile.length,
    ip
  )

  val remove = Removal(
    add.hash,
    add.remoteAddress
  )

  val expected = File(
    add.hash,
    add.name,
    add.contentType,
    add.length
  )

  def withFileActor(testCode: ActorRef => Any): Unit = {
    val fileActor = system.actorOf(FileActor.props(tempStorePath))
    try {
      testCode(fileActor)
    }
    finally system.stop(fileActor)
  }

  "A FileActor" must {

    """respond with DeleteStoreAck when attempting to delete a store""" in withFileActor { fileActor =>
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }

    """respond with CreateStoreAck when attempting to create a store""" in withFileActor { fileActor =>
      fileActor ! FileActor.CreateStore(self)
      expectMsg(FileActor.CreateStoreAck(self))
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }

    """respond with AddFileAck when attempting to add a file to the
    store""" in withFileActor { fileActor =>
      fileActor ! FileActor.CreateStore(self)
      expectMsg(FileActor.CreateStoreAck(self))
      fileActor ! FileActor.AddFile(self, add)
      expectMsg(FileActor.AddFileAck(self, add))
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }

    """respond with AddFileFail when attempting to add a file to the
    store which has already been added""" in withFileActor { fileActor =>
      fileActor ! FileActor.CreateStore(self)
      expectMsg(FileActor.CreateStoreAck(self))
      fileActor ! FileActor.AddFile(self, add)
      expectMsg(FileActor.AddFileAck(self, add))
      fileActor ! FileActor.AddFile(self, add)
      expectMsgType[FileActor.AddFileFail]
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }

    """respond with RemoveFileAck when attempting to remove a file
    from the store""" in withFileActor { fileActor =>
      fileActor ! FileActor.CreateStore(self)
      expectMsg(FileActor.CreateStoreAck(self))
      fileActor ! FileActor.AddFile(self, add)
      expectMsg(FileActor.AddFileAck(self, add))
      fileActor ! FileActor.RemoveFile(self, remove)
      expectMsg(FileActor.RemoveFileAck(self, remove))
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }

    """respond with RemoveFileFail when attempting to remove a file
    from the store which has already been removed""" in withFileActor { fileActor =>
      fileActor ! FileActor.CreateStore(self)
      expectMsg(FileActor.CreateStoreAck(self))
      fileActor ! FileActor.AddFile(self, add)
      expectMsg(FileActor.AddFileAck(self, add))
      fileActor ! FileActor.RemoveFile(self, remove)
      expectMsg(FileActor.RemoveFileAck(self, remove))
      fileActor ! FileActor.RemoveFile(self, remove)
      expectMsgType[FileActor.RemoveFileFail]
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }

    """respond with StoreFile when sent a FindFile msg""" in withFileActor { fileActor =>
      fileActor ! FileActor.CreateStore(self)
      expectMsg(FileActor.CreateStoreAck(self))
      fileActor ! FileActor.AddFile(self, add)
      expectMsg(FileActor.AddFileAck(self, add))
      fileActor ! FileActor.FindFile(self, testPDFHash)
      expectMsgType[FileActor.StoreFile]
      fileActor ! FileActor.DeleteStore(self)
      expectMsg(FileActor.DeleteStoreAck(self))
    }
  }
}
