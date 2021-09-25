package com.example

import zio.*
import zio.console.*

object Application extends App {
  def program(args:List[String]) = 
    ZIO.accessM[Has[Settings]](s => ZIO.succeed(s.get))
    .flatMap(s => putStrLn(s.toString))

  override def run(args: List[String]) = program(args)
    .provideCustomLayer(Settings.live(args))
    .exitCode
}
