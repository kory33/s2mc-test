package com.github.kory33.s2mctest.core.connection.transport

import cats.data.Writer
import com.github.kory33.s2mctest.core.connection.codec.ByteCodec
import com.github.kory33.s2mctest.core.connection.codec.dsl.DecodeFiniteBytes
import com.github.kory33.s2mctest.core.connection.protocol.{
  PacketId,
  CodecBinding,
  PacketIdBindings,
  Protocol
}
import fs2.Chunk
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ProtocolBasedTransportSpec extends AnyFlatSpec with should.Matchers {
  val mockedProtocol: Protocol[(CodecBinding[Unit], CodecBinding[Int]), EmptyTuple] =
    new Protocol(
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
        Writer.tell(data)
    }

  "ProtocolBasedTransportSpec.writePacket" should "allow writing stuff in binding tuple" in {
    val protocolBasedTransport =
      ProtocolBasedTransport(writeOnlyTransport, mockedProtocol.asViewedFromClient)

    protocolBasedTransport.writePacket(0).run._1 should equal(Chunk(0: Byte))
    protocolBasedTransport.writePacket(10).run._1 should equal(Chunk(10: Byte))
    protocolBasedTransport.writePacket(()).run._1 should equal(Chunk.empty[Byte])
  }

  "ProtocolBasedTransportSpec.writePacket" should "not allow writing stuff not in binding tuple" in {
    val protocolBasedTransport =
      ProtocolBasedTransport(writeOnlyTransport, mockedProtocol.asViewedFromClient)

    "protocolBasedTransport.writePacket('').run._1" shouldNot compile
    "protocolBasedTransport.writePacket(0.0).run._1" shouldNot compile
    "protocolBasedTransport.writePacket(Some(1)).run._1" shouldNot compile
  }
}
