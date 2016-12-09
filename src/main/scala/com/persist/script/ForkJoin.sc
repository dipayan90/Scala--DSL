import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

// Simple

val f1 = Future {
  2 + 3
}

val f2 = f1.map(_ * 2)

val f3 = f1.map(_ + 1)

val f4 = (f2 zip f3).map(p => p._1 * p._2)

val v1 = Await.result(f4, 1 second)

// List

val s = List(1, 3, 5, 7)


val fs = s.map(i=>Future(i+1))

val fss = Future.sequence(fs)

val v2 = Await.result(fss,Duration.Inf)

// First of

val r = scala.util.Random

val s1 = List(6, 3, 7, 2, 5)

val fs2 = s1.map(i => {
  Future{
    Thread.sleep(i*500)
    i * 2
  }
})

val first = Future.firstCompletedOf(fs2)

val firstv = Await.result(first, Duration.Inf)

val all = Future.sequence(fs2)

val allv = Await.result(all, Duration.Inf)

