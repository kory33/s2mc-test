package io.github.kory33.s2mctest.core.connection.codec.dsl.tracing

sealed trait DecodeDSLTrace

object DecodeDSLTrace {

  /**
   * A [[DecodeDSLTrace]] which is also a [[Throwable]].
   *
   * These objects encapsulate the stack trace information (as filled in by the [[Throwable]]
   * constructor), and they
   *   - should be constructed when a DSL instruction object is constructed
   *   - should be retrieved back when an error occurs in the interpreter
   */
  class StackTrace extends Throwable with DecodeDSLTrace

  /**
   * Compute the trace object from the current call-stack. This is necessarily a side-effectful
   * function.
   */
  def computeCurrentTrace(): DecodeDSLTrace = {
    // When test harness' performance becomes real problem,
    // maybe add an application argument to collect no trace

    // Right now, we collect the current stack trace and return
    new StackTrace()
  }

}
