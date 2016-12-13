package com.persist.uw.examples

import scala.reflect.ClassTag
import Graph._

import scala.collection.immutable.Iterable

object Graph {

  trait Node

  trait Rel

  case class Link(r: Rel, prev: Node, next: Node)

  def apply(nodes: Set[Node], links: Set[Link]) = new Graph(nodes, links)
}

class Graph(val nodes: Set[Node], val links: Set[Link]) {

  import Cypher._

  def rels: Set[Rel] = links.map(_.r)

  val nextRels: Map[Node, Set[Rel]] = links.groupBy(_.prev).map(e => e._1 -> e._2.map(_.r))

  val nextNode: Map[Rel, Node] = links.map({ case Link(r,n1,n2) => (r,n2) }).toMap

  def cypher[T: ClassTag, T1](pattern: Pattern[T])(act: (T) => T1): Set[T1] = {
    // could add optimization here
    pattern match {
      case ns: NS[T] => nodes.flatMap(ns.cypher(this, _)).map(act)
      case rs: RS[T] => rels.flatMap(rs.cypher(this, _)).map(act)
    }
  }
}

object Cypher {

  private def cross[T1, T2](a: Set[T1], b: Set[T2]): Set[(T1, T2)] = for {x <- a; y <- b} yield (x, y)

  trait Pattern[T]

  // pattern that starts with a Node
  abstract class NS[T: ClassTag] extends Pattern[T] {
    def cypher(g: Graph, start: Node): Set[T]

    def ->:[T1 <: Node : ClassTag](next: N[T1]) = NN[T1,T](next,this)

    def ->:[T1 <: Rel : ClassTag](next: R[T1]) = RN[T1,T](next,this)
  }

  // pattern that starts with a Rel
  abstract class RS[T: ClassTag] extends Pattern[T] {
    def cypher(g: Graph, start: Rel): Set[T]

    def ->:[T1 <: Node : ClassTag](next: N[T1]) = NR[T1,T](next,this)
  }

  // pattern for a Node
  case class N[T <: Node : ClassTag](cond: T => Boolean = (x: T) => true) extends NS[T] {
    def cypher(g: Graph, start: Node): Set[T] = {
      val result = for(
        p1 <- g.nodes.collect { case p: T if cond(p) => p}
      ) yield p1
      result
    }
  }

  // pattern for a Rel
  case class R[T <: Rel : ClassTag](cond: T => Boolean = (x: T) => true) extends RS[T] {
    def cypher(g: Graph, start: Rel): Set[T] = {
      val result = for(
        p1 <- g.links.map(_.r).collect { case p: T if cond(p) => p}
      )yield p1
      result
    }
  }

  // compound pattern, node followed by node ...
  case class NN[T <: Node : ClassTag, T1: ClassTag](n: N[T], ns: NS[T1]) extends NS[(T, T1)] {
    def cypher(g: Graph, start: Node): Set[(T, T1)] = {
        for {
        n1 <- n.cypher(g,start)
        r1 <- g.nextRels.getOrElse(n1,Set())
        n2 <- Set(g.nextNode(r1))
        n3 <- ns.cypher(g,n2)
      } yield (n1,n3)
    }
  }

  // compound pattern, node followed by rel ...
  case class NR[T <: Node : ClassTag, T1: ClassTag](n: N[T], rs: RS[T1]) extends NS[(T, T1)] {
    def cypher(g: Graph, start: Node): Set[(T, T1)] = {
      val result = for {
        p1 <- n.cypher(g, start)
        r1 <- g.nextRels.getOrElse(p1, Set())
        r2 <- rs.cypher(g, r1)
    } yield(p1,r2)
      result
    }
  }

  // compound pattern, rel followed by node ...
  case class RN[T <: Rel : ClassTag, T1: ClassTag](r: R[T], ns: NS[T1]) extends RS[(T, T1)] {
    def cypher(g: Graph, start: Rel): Set[(T, T1)] = {
      for {
        r1 <- r.cypher(g, start)
        n1 <- Set(g.nextNode(r1))
        n2 <- ns.cypher(g, n1)
      } yield(r1,n2)
    }
  }

  object --: {
    def unapply[T1, T2](v: (T1, T2)) = Some(v)
  }

}
