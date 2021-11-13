package io.github.kory33.s2mctest.core.client.worldview

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class Vector3DSpec extends AnyFlatSpec with should.Matchers {
  val error = 0.00000000001

  "normalized" should "make the length of a vector sufficiently close to 1" in {
    val inputs = List(
      // format: off
      Vector3D(0.0, -0.1, 0.0),
      Vector3D(10.0, 0.1, -3.0),
      Vector3D(1.0, 2.0, -10.0),
      Vector3D(10000.0, 1.0, 5.0),
      Vector3D(0.0, -1.0, 1.3)
      // format: on
    )

    inputs.foreach { input => input.normalized.length should ===(1.0 +- error) }
  }

  "yaw" should "compute yaw values for vectors along axes" in {
    Vector3D(0.0, 3.0, 1.0).yaw should ===(0.0 +- error)
    Vector3D(-1.0, 3.0, 0.0).yaw should ===(90.0 +- error)
    Vector3D(0.0, 3.0, -1.0).yaw should ===(180.0 +- error)
    Vector3D(1.0, 3.0, 0.0).yaw should ===(270.0 +- error)
  }

  it should "compute yaw values for vectors with intermediate angles" in {
    Vector3D(-1.0, 0.0, 1.0).yaw should ===(45.0 +- error)
    Vector3D(-1.0, 0.0, -1.0).yaw should ===(135.0 +- error)
    Vector3D(1.0, 0.0, -1.0).yaw should ===(225.0 +- error)
    Vector3D(1.0, 0.0, 1.0).yaw should ===(315.0 +- error)
  }

  "pitch" should "compute pitch values for vectors along axes" in {
    Vector3D(0.0, 3.0, 0.0).pitch should ===(-90.0 +- error)
    Vector3D(1.0, 0.0, 1.0).pitch should ===(0.0 +- error)
    Vector3D(0.0, -3.0, 0.0).pitch should ===(90.0 +- error)
  }

  it should "compute pitch values for vectors with intermediate angles" in {
    Vector3D(1.0, 1.0, 0.0).pitch should ===(-45.0 +- error)
    Vector3D(0.0, 1.0, 1.0).pitch should ===(-45.0 +- error)

    Vector3D(1.0, -1.0, 0.0).pitch should ===(45.0 +- error)
    Vector3D(0.0, -1.0, 1.0).pitch should ===(45.0 +- error)
  }
}
