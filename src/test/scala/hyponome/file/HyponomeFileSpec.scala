package hyponome.file

import com.typesafe.config.{Config, ConfigFactory}
import hyponome.core._
import java.net.InetAddress
import java.nio.file._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class TestStore(p: Path) extends HyponomeFile {

  val storePath: Path = p
}

class HyponomeFileSpec extends WordSpecLike with Matchers with ScalaFutures {

  val fs: FileSystem = FileSystems.getDefault()

  val tempStorePath: Path = fs.getPath("/tmp/hyponome/store")

  val testPDF: Path = {
    val s: String = getClass.getResource("/test.pdf").getPath
    fs.getPath(s)
  }

  val testPDFHash = SHA256Hash(
    "eba205fb9114750b2ce83db62f9c2a15dd068bcba31a2de32d8df7f7c8d85441"
  )

  def withTestStoreInstance(testCode: TestStore => Any): Unit = {
    val t: TestStore = new TestStore(tempStorePath)
    val storeFuture: Future[Path] = t.createStore()
    val store: Path = Await.result(storeFuture, 5.seconds)
    try     { testCode(t);                 () }
    finally { deleteFolder(tempStorePath); () }
  }

  "An instance of a class that extends HyponomeFile" must {

    "have a getFilePath method" which {
      "returns the correct file store Path" in withTestStoreInstance { t =>
        val hash = testPDFHash
        val (dir: String, file: String) = hash.value.splitAt(2)
        val expected: Path = tempStorePath.resolve(dir).resolve(file).toAbsolutePath
        t.getFilePath(hash) should equal(expected)
      }
    }

    "have a copyToStore method" which {
      "copies a file to the correct file store Path" in withTestStoreInstance { t =>
        t.copyToStore(testPDFHash, testPDF).flatMap { p =>
          getSHA256Hash(p)
        }.futureValue should equal(testPDFHash)
      }
      """returns a Future of a Failure(FileAlreadyExistsException)
      when trying to copy a file to a path that already exists""" in withTestStoreInstance { t =>
        t.copyToStore(testPDFHash, testPDF).flatMap { _ =>
          t.copyToStore(testPDFHash, testPDF)
        }.failed.futureValue shouldBe a [java.nio.file.FileAlreadyExistsException]
      }
    }

    "have a existsInStore method" which {
      """returns a Future with the value of true if the specified path
      exists in the file store""" in withTestStoreInstance { t =>
        t.copyToStore(testPDFHash, testPDF).flatMap { p =>
          t.existsInStore(p)
        }.futureValue should equal(true)
      }
      """returns a Future with the value of false if the specified path
      doesn't exist in the file store""" in withTestStoreInstance { t =>
        val testHash: SHA256Hash = SHA256Hash(
          "482bfece08d11246f41ce3dc43480e1b61659fbe0083b754bee09b44b940ae6c"
        )
        t.existsInStore(t.getFilePath(testHash)).futureValue should equal(false)
      }
    }

    "have a deleteFromStore method" which {
      "deletes a file with the specified hash from the file store" in withTestStoreInstance { t =>
        t.copyToStore(testPDFHash, testPDF).flatMap { _ =>
          t.deleteFromStore(testPDFHash)
        }.flatMap { _ =>
          t.existsInStore(t.getFilePath(testPDFHash))
        }.futureValue should equal(false)
      }
      """returns a Future of value Failed(NoSuchFileException) when
      attempting to delete a file which doesn't exist""" in withTestStoreInstance { t =>
        t.deleteFromStore(testPDFHash).failed.futureValue shouldBe a [java.nio.file.NoSuchFileException]
      }
    }
  }
}