package com.example
package messages

import zio.*
import zio.json.*
import zio.kafka.serde.Serde

case class Item(productId: String, quantity: Int)
object Item {
  given JsonCodec[Item] = DeriveJsonCodec.gen
}

case class OrderPlaced(orderId: String, items: List[Item])
object OrderPlaced {
  given JsonCodec[OrderPlaced] = DeriveJsonCodec.gen
  given serde: Serde[Any, OrderPlaced] = Serde.string.inmapM(str =>
    IO
      .fromEither(str.fromJson[OrderPlaced])
      .mapError(RuntimeException(_))
  )(x => UIO.succeed(x.toJson))
}

case class PaymentFailed(orderId: String)
