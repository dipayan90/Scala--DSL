package com.persist.uw.examples

import Graph._
import java.util.UUID

object GodsGraphSchema {

  private def u = UUID.randomUUID()

  trait Person extends Node {
    val name: String
    val age: Int
  }

  case class Titan(name: String, age: Int) extends Person

  case class God(name: String, age: Int) extends Person

  case class DemiGod(name: String, age: Int) extends Person

  case class Human(name: String, age: Int) extends Person

  case class Monster(name: String) extends Node

  case class Location(name: String) extends Node

  trait Family extends Rel

  case class Father(uuid: UUID = u) extends Family

  case class Mother(uuid: UUID = u) extends Family

  case class Brother(uuid: UUID = u) extends Family

  case class Pet(uuid: UUID = u) extends Rel

  case class Lives(reason: String) extends Rel

  case class Battled(time: Int, place: (Double, Double)) extends Rel

}

case class GodsGraph() {

  import GodsGraphSchema._

  val saturn = Titan("saturn", 10000)
  val jupiter = God("jupiter", 5000)
  val neptune = God("neptune", 4500)
  val hercules = DemiGod("hercules", 30)
  val alcamene = Human("alcamene", 45)
  val pluto = God("pluto", 4000)
  val nemean = Monster("nemean")
  val hydra = Monster("hydra")
  val cerberus = Monster("cerberus")
  val sky = Location("sky")
  val sea = Location("sea")
  val tartarus = Location("tartarus")
  val ns: Set[Node] = Set(saturn, jupiter, neptune, hercules, alcamene, pluto, nemean, hydra, cerberus, sky, sea, tartarus)

  val rs = Set(
    Link(Father(), jupiter, saturn),
    Link(Lives("loves fresh breezes"), jupiter, sky),
    Link(Lives("loves waves"), neptune, sea),
    Link(Brother(), jupiter, neptune),
    Link(Brother(), jupiter, pluto),
    Link(Brother(), neptune, jupiter),
    Link(Brother(), neptune, pluto),
    Link(Brother(), pluto, jupiter),
    Link(Brother(), pluto, neptune),
    Link(Father(), hercules, jupiter),
    Link(Mother(), hercules, alcamene),
    Link(Battled(1, (38.1, 23.7)), hercules, nemean),
    Link(Battled(2, (37.7, 23.9)), hercules, hydra),
    Link(Battled(12, (39, 22)), hercules, cerberus),
    Link(Pet(), pluto, cerberus),
    Link(Lives(""), cerberus, tartarus),
    Link(Lives("no fear of death"), pluto, tartarus)
  )

  def godsGraph() = Graph(ns, rs)

}

