package io.github.kory33.s2mctest.core.client.api

import io.github.kory33.s2mctest.core.client.api.MinecraftVector
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class MinecraftVectorSpec extends AnyFlatSpec with should.Matchers {
  val error = 0.000000001f

  "yaw" should "compute yaw values for vectors along axes" in {
    MinecraftVector(0.0, 3.0, 1.0).yaw should ===(0.0f +- error)
    MinecraftVector(-1.0, 3.0, 0.0).yaw should ===(90.0f +- error)
    MinecraftVector(0.0, 3.0, -1.0).yaw should ===(180.0f +- error)
    MinecraftVector(1.0, 3.0, 0.0).yaw should ===(270.0f +- error)
  }

  it should "compute yaw values for vectors with intermediate angles" in {
    MinecraftVector(-1.0, 0.0, 1.0).yaw should ===(45.0f +- error)
    MinecraftVector(-1.0, 0.0, -1.0).yaw should ===(135.0f +- error)
    MinecraftVector(1.0, 0.0, -1.0).yaw should ===(225.0f +- error)
    MinecraftVector(1.0, 0.0, 1.0).yaw should ===(315.0f +- error)
  }

  "pitch" should "compute pitch values for vectors along axes" in {
    MinecraftVector(0.0, 3.0, 0.0).pitch should ===(-90.0f +- error)
    MinecraftVector(1.0, 0.0, 1.0).pitch should ===(0.0f +- error)
    MinecraftVector(0.0, -3.0, 0.0).pitch should ===(90.0f +- error)
  }

  it should "compute pitch values for vectors with intermediate angles" in {
    MinecraftVector(1.0, 1.0, 0.0).pitch should ===(-45.0f +- error)
    MinecraftVector(0.0, 1.0, 1.0).pitch should ===(-45.0f +- error)

    MinecraftVector(1.0, -1.0, 0.0).pitch should ===(45.0f +- error)
    MinecraftVector(0.0, -1.0, 1.0).pitch should ===(45.0f +- error)
  }
}
