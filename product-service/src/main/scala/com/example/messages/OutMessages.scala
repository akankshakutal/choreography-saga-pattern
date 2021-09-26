package com.example
package messages

import zio.*
import zio.json.*
import zio.kafka.serde.Serde

case class ProductAvailed(orderId: String)
object ProductAvailed {
  given JsonCodec[ProductAvailed] = DeriveJsonCodec.gen
  val serde = Serde.string.inmapM(str =>
    IO
      .fromEither(str.fromJson[ProductAvailed])
      .mapError(RuntimeException(_))
  )(x => UIO.succeed(x.toJson))
}

case class ProductAvailFailed(orderId: String)
object ProductAvailFailed {
  given JsonCodec[ProductAvailFailed] = DeriveJsonCodec.gen
  val serde = Serde.string.inmapM(str =>
    IO
      .fromEither(str.fromJson[ProductAvailFailed])
      .mapError(RuntimeException(_))
  )(x => UIO.succeed(x.toJson))
}
