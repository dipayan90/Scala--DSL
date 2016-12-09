package com.persist.uw.examples

import org.specs2._

import Cypher._

class CypherTest extends mutable.Specification {

  import GodsGraphSchema._

  "for" >> {
    // MATCH (p1:Person)-[r1:Battled]->(m1:Monster) WHERE r1.time > 1 RETURN p1.name, r1.time, m1.name
    val g = new GodsGraph().godsGraph()
    val result = for (
      p1 <- g.nodes.collect { case p: Person => p };
      r1 <- g.nextRels.getOrElse(p1, Set()).collect { case b: Battled => b };
      if r1.time > 1;
      m1 <- Seq(g.nextNode(r1)).collect { case m: Monster => m }
    ) yield (p1.name, r1.time, m1.name)
    result mustEqual Set(("hercules", 2, "hydra"), ("hercules", 12, "cerberus"))
  }
  "n" >> {
    // Match (n:God) RETURN n.name
    val g = new GodsGraph().godsGraph()
    val p = N[God]()
    val result = g.cypher(p)(_.name)
    result mustEqual Set("jupiter", "neptune", "pluto")
  }
  "n()" >> {
    // MATCH (p:Person) WHERE p.age < 100 RETURNS p.name
    val g = new GodsGraph().godsGraph()
    val p = N[Person](_.age < 100)
    val result = g.cypher(p)(_.name)
    result mustEqual Set("hercules", "alcamene")
  }
  "r" >> {
    // MATCH [b:Battled] RETURNS b.time
    val g = new GodsGraph().godsGraph()
    val p = R[Battled]()
    val result = g.cypher(p)(_.time)
    result mustEqual Set(1, 2, 12)
  }
  "n->r->n" >> {
    // MATCH (p1:Person)-[r:Father]->(p2:person) RETURNS p1.name, p2.name
    val g = new GodsGraph().godsGraph()
    val p = N[Person]() ->: R[Father]() ->: N[Person]()
    val result = g.cypher(p) { case p1 --: r --: p2 => (p1.name, p2.name) }
    result mustEqual Set(("hercules", "jupiter"), ("jupiter", "saturn"))
  }
  "p->r()->m" >> {
    // MATCH (p1:Person)-[r1:Battled]->(m1:Monster) WHERE r1.time > 1 RETURN p1.name, r1.time, m1.name
    val g = new GodsGraph().godsGraph()
    val p = N[Person]() ->: R[Battled](_.time > 1) ->: N[Monster]()
    val result = g.cypher(p) { case p1 --: r1 --: m1 => (p1.name, r1.time, m1.name) }
    result mustEqual Set(("hercules", 2, "hydra"), ("hercules", 12, "cerberus"))
  }
  "n()->r" >> {
    // MATCH (p1:Person)-[r1:Lives] WHERE p1.age > 4500 RETURNS p1.name, r1.reason
    val g = new GodsGraph().godsGraph()
    val p = N[Person](_.age > 4500) ->: R[Lives]()
    val result = g.cypher(p) { case p1 --: r1 => (p1.name, r1.reason) }
    result mustEqual Set(("jupiter", "loves fresh breezes"))
  }
  "r()->n" >> {
    // MATCH [r1:Lives]->(loc1:Location) WHERE r1.reason.contains("loves")
    // RETURNS loc1.name
    val g = new GodsGraph().godsGraph()
    val p = R[Lives](_.reason.contains("loves")) ->: N[Location]()
    val result = g.cypher(p) { case r1 --: loc1 => loc1.name }
    result mustEqual Set("sea", "sky")
  }
  "n->n" >> {
    // MATCH (p1:Demigod) -> (m1:Monster) RETURNS m1.name
    val g = new GodsGraph().godsGraph()
    val p = N[DemiGod]() ->: N[Monster]()
    val result = g.cypher(p) { case p1 --: m1 => m1.name }
    result mustEqual Set("nemean", "hydra", "cerberus")

  }
  "n->r->n->r->n" >> {
    // MATCH (p1:Person) ->[r1:Father]->(p2:Person)->[r2:Brother]->(p3:Person)
    // RETURNS p1.name, p3.name
    val g = new GodsGraph().godsGraph()
    val p = N[Person]() ->: R[Father]() ->: N[Person]() ->: R[Brother]() ->: N[Person]()
    val result = g.cypher(p) { case p1 --: r1 --: p2 --: r2 --: p3 => (p1.name, p3.name) }
    result mustEqual Set(("hercules", "neptune"), ("hercules", "pluto"))
  }

}