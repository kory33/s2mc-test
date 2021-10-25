package io.github.kory33.s2mctest.core.connection.transport

import cats.data.Writer
import io.github.kory33.s2mctest.core.connection.protocol.{
  CodecBinding,
  PacketId,
  PacketIdBindings,
  Protocol
}
import fs2.Chunk
import io.github.kory33.s2mctest.core.connection.codec.ByteCodec
import io.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import io.github.kory33.s2mctest.core.connection.protocol.Protocol
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.compiletime.ops.int.S

class ProtocolBasedTransportSpec extends AnyFlatSpec with should.Matchers {
  import cats.implicits.given

  val mockedProtocol: Protocol[(Unit, Int), EmptyTuple] =
    Protocol(
      PacketIdBindings(
        3 -> ByteCodec(DecodeFiniteBytes.pure(()), _ => fs2.Chunk.empty),
        42 -> ByteCodec(DecodeFiniteBytes.pure(3), (a: Int) => fs2.Chunk(a.toByte))
      ),
      PacketIdBindings(EmptyTuple)
    )

  val writeOnlyTransport: PacketTransport[Writer[Chunk[Byte], _]] =
    new PacketTransport[Writer[Chunk[Byte], _]] {
      override def readOnePacket: Writer[Chunk[Byte], (PacketId, Chunk[Byte])] =
        Writer.value((1, Chunk.empty)) // some meaningless value

      override def write(id: PacketId, data: Chunk[Byte]): Writer[Chunk[Byte], Unit] =
        // write id then data
        Writer.tell(Chunk[Byte](id.toByte)) >> Writer.tell(data)
    }

  "ProtocolBasedTransportSpec.writePacket" should "allow writing stuff in binding tuple" in {
    val protocolBasedTransport =
      ProtocolBasedTransport(writeOnlyTransport, mockedProtocol.asViewedFromClient)

    protocolBasedTransport.writePacket(0).run._1 should equal(Chunk(42: Byte, 0: Byte))
    protocolBasedTransport.writePacket(10).run._1 should equal(Chunk(42: Byte, 10: Byte))
    protocolBasedTransport.writePacket(()).run._1 should equal(Chunk(3: Byte))
  }

  "ProtocolBasedTransportSpec.writePacket" should "not allow writing stuff not in binding tuple" in {
    val protocolBasedTransport =
      ProtocolBasedTransport(writeOnlyTransport, mockedProtocol.asViewedFromClient)

    "protocolBasedTransport.writePacket('').run._1" shouldNot compile
    "protocolBasedTransport.writePacket(0.0).run._1" shouldNot compile
    "protocolBasedTransport.writePacket(Some(1)).run._1" shouldNot compile
  }
}
