package org.risto.playmovie.backend

/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 17.8.2013
 * Time: 22.41
 */

import akka.actor._
import akka.routing._


/**
 * An actor pool with a bunch of reasonable defaults for actors
 * that do some kind of blocking IO-bound work. General loose
 * assumptions are that the requests to the actors are round-trips with
 * replies (active futures), the actors won't use much CPU, that we only want
 * to process each message one time, and that the actors in the pool could
 * be expensive to create (e.g. establishing a network connection).
 *
 * A lot of the values set here are pretty arbitrary and could only
 * be set scientifically with a lot of application-specific tuning.
 */
trait IOBoundActorPool
  extends DefaultActorPool
  with SmallestMailboxSelector
  with ActiveFuturesPressureCapacitor
  with BoundedCapacityStrategy
  with Filter
  with BasicRampup
  with BasicBackoff {
  self: Actor =>

  // Selector: selectionCount is how many pool members to send each message to
  override def selectionCount = 1

  // Selector: partialFill controls whether to pick less than selectionCount or
  // send the same message to duplicate delegates, when the pool is smaller
  // than selectionCount. Does not matter if lowerBound >= selectionCount.
  override def partialFill = true

  // BoundedCapacitor: create between lowerBound and upperBound delegates in the pool
  override val lowerBound = 1
  override lazy val upperBound = 50

  // BasicRampup: rampupRate is percentage increase in capacity when all delegates are busy
  // make this very small to aim for just 1 more actor per needed connection
  override def rampupRate = 0.05

  // BasicBackoff: backoffThreshold is the percentage-busy to drop below before
  // we reduce actor count
  override def backoffThreshold = 0.7

  // BasicBackoff: backoffRate is the amount to back off when we are below backoffThreshold.
  // this one is intended to be less than 1.0-backoffThreshold so we keep some slack.
  override def backoffRate = 0.20
}
