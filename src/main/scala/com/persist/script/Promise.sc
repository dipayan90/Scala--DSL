import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

// Simple

val p = Promise[Int]

p.future.onComplete(i => println(s"Saw:$i"))

val v1 = p.trySuccess(5)

val v2 = Thread.sleep(1000)

// Cancel Futures

case object Cancel extends Exception("cancel")

def cancel(p:Promise[Unit]): Unit = {
  if (p.isCompleted) throw Cancel
}

def run(p:Promise[Unit]): Future[Unit] = {
  Future {
    cancel(p)
    Thread.sleep(100)
    cancel(p)
    println("F1")
  }
  .map { f =>
    cancel(p)
    Thread.sleep(100)
    cancel(p)
    println("F1")
  }
  .map { f =>
    cancel(p)
    Thread.sleep(100)
    cancel(p)
    println("F2")
  }
}

val p1 = Promise[Unit]
val f1 = run(p1)
f1.onComplete(v=>println(s"R1:$v"))

Thread.sleep(2000)

val p2 = Promise[Unit]
val f2 = run(p2)
f2.onComplete(v=>println(s"R2:$v"))
Thread.sleep(200)
p2.trySuccess(())

Thread.sleep(2000)
