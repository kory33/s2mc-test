package io.github.kory33.s2mctest.core.client

import java.util.UUID

/**
 * Identity of a Minecraft client which is normally notified by the server upon login.
 */
case class ClientIdentity(name: String, uuid: UUID)
