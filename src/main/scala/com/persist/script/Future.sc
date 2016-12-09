import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

// Simple

val f1 = Future {
  2 + 3
}

val v1 = Await.result(f1, 1 second)

// Function composition

def double(i: Int): Future[Int] = {
  Future {
    2 * i
  }
}

def add(i: Int): Future[Int] = {
  Future {
    i + 1
  }
}

val f2 = double(5).flatMap(add)

val v2 = Await.result(f2, 1 second)

// Using for

val f3 = for {
  v1 <- double(5)
  v2 <- add(v1)
} yield v2

val v3 = Await.result(f3, 1 second)

// To a side effect

val v4 = add(3).flatMap(double).map(println(_))


