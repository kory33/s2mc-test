package io.github.kory33.s2mctest.core.client.worldview

/**
 * The datatype describing the cumulative age of the world in which the client resides, along
 * with the current time of day.
 */
case class WorldTime(worldAge: Long, timeOfDay: Long)
