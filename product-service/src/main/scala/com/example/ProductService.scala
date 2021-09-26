package com.example

import zio.*
import com.example.models.OrderItem

object ProductService {
  def live = ZLayer.succeed(new ProductService())
  
  def avail(items:List[OrderItem]) = ZIO.accessM[Has[ProductService]](_.get.avail(items))
}

class ProductService(){
  def avail(items:List[OrderItem]):ZIO[Any, String, Unit] = ???
}
