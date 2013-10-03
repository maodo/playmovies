package org.risto.playmovie.backend

import QueryProtocol.Query
import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.risto.playmovie.backend.QueryMasterProtocol.{RemoveSupervisor, AddSupervisor}


/**
 * User: Risto Yrjänä
 * Date: 8.8.2013
 * Time: 23.17
 */
class QueryMasterSpec extends TestKit(ActorSystem
  .create("QueryMasterSpec")) with PlayMovieSpec {

  class EchoActor extends Actor {
    def receive = {
      case msg => sender ! QueryProtocol.NotFound
    }
  }

  behavior of "A QueryMaster"

  it should "forward queries to a configured QuerySupervisor" in {
    val queryMasterParams = List(("testSupervisor", Props(new ActorForwarder(testActor))))
    val queryMaster = system.actorOf(Props(new QueryMaster(queryMasterParams)))

    queryMaster ! Query("test1")
    expectMsg(Query("test1"))

    queryMaster ! Query("test2")
    expectMsg(Query("test2"))
  }

  it should "return the results from a QuerySupervisor" in {
    val queryMasterParams = List(("testSupervisor", Props(new EchoActor)))

    val queryMaster = system.actorOf(Props(new QueryMaster(queryMasterParams)))

    queryMaster ! Query("test")
    expectMsg(List(QueryProtocol.NotFound))
  }

  it should "allow adding supervisors with messages" in {
    val queryMaster = TestActorRef[QueryMaster]

    queryMaster.underlyingActor.supervisors.isEmpty

    queryMaster ! AddSupervisor("test", Props(new ActorForwarder(testActor)))

    queryMaster.underlyingActor.supervisors.size == 1
  }

  it should "allow removing supervisors with messages" in {
    val queryMaster = TestActorRef[QueryMaster]

    queryMaster ! AddSupervisor("test", Props(new ActorForwarder(testActor)))
    queryMaster ! RemoveSupervisor("test")

    queryMaster.underlyingActor.supervisors.isEmpty
  }
}
