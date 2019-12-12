package io.chrisdavenport.mules.redis

// import cats.implicits._
import cats.effect._

import dev.profunktor.redis4cats.connection.{ RedisClient, RedisURI }
import dev.profunktor.redis4cats.domain.RedisCodec
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.effect.Log
import io.chrisdavenport.mules.Cache

import org.specs2.mutable.Specification
import cats.effect.specs2.CatsIO

import com.dimafeng.testcontainers.GenericContainer
import io.chrisdavenport.testcontainersspecs2.ForAllTestContainer

import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy


class RedisCacheSpec extends Specification with CatsIO 
  with  ForAllTestContainer {

    lazy val container: GenericContainer = GenericContainer(
      "redis:5.0.0",
      exposedPorts = Seq(6379),
      env = Map(),
      waitStrategy = new LogMessageWaitStrategy()
        .withRegEx(".*Ready to accept connections*\\s")
        .withTimes(1)
        .withStartupTimeout(Duration.of(60, SECONDS))
    )

    lazy val ipAddress = container.containerIpAddress
    lazy val mappedPort = container.mappedPort(6379)

  implicit val T = IO.timer(scala.concurrent.ExecutionContext.global)
  implicit val CS = IO.contextShift(scala.concurrent.ExecutionContext.global)

  implicit val log = new Log[IO]{
    def error(msg: => String): cats.effect.IO[Unit] = IO.unit
    def info(msg: => String): cats.effect.IO[Unit] = IO.unit
  }

  private val stringCodec = RedisCodec.Utf8

  def makeCache : Resource[IO, Cache[IO, String, String]] = for {
    uri <- Resource.liftF(RedisURI.make[IO](s"redis://localhost:$mappedPort"))
    client <- RedisClient[IO](uri)
    redis <- Redis[IO, String, String](client, stringCodec, uri)
    cache = RedisCache.fromCommands(redis, None)
  } yield cache

  "RedisCache" should {
    "successfully run an expected operation" in {
      makeCache.use{ cache => 
        for {
          _ <- cache.delete("foo") // Incase of existing key
          out1 <- cache.lookup("foo")
          _ <- cache.insert("foo", "bar")
          out2 <- cache.lookup("foo")
          _ <- cache.insert("foo", "baz")
          out3 <- cache.lookup("foo")
        } yield (out1, out2, out3)
      }.map(_ must_===((None, Some("bar"), Some("baz"))))
    }
  }

}