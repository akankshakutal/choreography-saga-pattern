@main def hello: Unit = 
  val listenedTopics = Seq(
    "OrderPlaced",
    "PaymentFailed"
  )

  val emittedTopics = Seq(
    "ProductAvailed",
    "ProductAvailFailed"
  )

  

  println("product-service started...")
  println(s"listening to topics: ${listenedTopics.mkString(",")}")
  println(s"emitting to topics: ${emittedTopics.mkString(",")}")

