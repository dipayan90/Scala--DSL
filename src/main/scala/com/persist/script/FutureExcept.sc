import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

// Simple

val s = List(1, 2, 0, 7)

val fs = s.map(i => Future(10 / i))

for (f <- fs) {
  try {
    val v = Await.result(f, Duration.Inf)
    println(s"Success:$v")
  } catch {
    case ex: Throwable => println(s"Fail:$ex")
  }
}

// OnComplete

for (f <- fs) {
  f.onComplete {
    t: Try[Int] => println(t)
  }
}

// Sequence

try {
  val v = Await.result(Future.sequence(fs),Duration.Inf)
  println(s"Seq:$v")
} catch {
  case ex:Throwable => println(s"Seq Fail:$ex")
}


// Recover

val fs1 = for (f <- fs) yield f.recover {
  case ex: Throwable => -1
}

val v = Await.result(Future.sequence(fs1), Duration.Inf)
