package com.example

import zio.*
import zio.console.*
import com.example.models.OrderItem
import java.io.IOException

object ProductService {
  def live = ZLayer.succeed(new ProductService)

  def avail(items: List[OrderItem]) =
    ZIO.accessM[Has[ProductService] & Has[Console.Service]](_.get.avail(items))
}

class ProductService {
  def avail(items: List[OrderItem]): ZIO[Console, IOException, Unit] =
    putStrLn(s"items availed: $items")
}
