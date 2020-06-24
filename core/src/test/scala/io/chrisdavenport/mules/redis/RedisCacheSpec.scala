package io.chrisdavenport.mules.redis

import cats.implicits._
import cats.effect._

import dev.profunktor.redis4cats.connection.{ RedisClient, RedisURI }
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.Redis
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
import io.chrisdavenport.mules.TimeSpec
import scala.concurrent.duration._

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

  def makeCache(defaultTimeout: Option[TimeSpec]) : Resource[IO, Cache[IO, String, String]] = for {
    uri <- Resource.liftF(RedisURI.make[IO](s"redis://localhost:$mappedPort"))
    client <- RedisClient[IO](uri)
    redis <- Redis[IO].fromClient[String, String](client,  RedisCodec.Utf8)
    cache = RedisCache.fromCommands(redis, defaultTimeout)
  } yield cache

  "RedisCache" should {
    "successfully run an expected operation" in {
      makeCache(None).use{ cache => 
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


    "see an expired value with defaultTimeout" in {
      makeCache(TimeSpec.unsafeFromDuration(2.seconds).some).use{ cache => 
        val key = "red"
        for  {
          _ <- cache.delete(key)
          _ <- cache.insert(key, "value1")
          out1 <- cache.lookup(key)
          _ <- Timer[IO].sleep(2.5.seconds)
          out2 <- cache.lookup(key)
        } yield (out1, out2)
      }.map(_ must_===(("value1".some, None)))
    }

    "see the set value rather than the default" in {
      makeCache(TimeSpec.unsafeFromDuration(2.seconds).some).use{ cache => 
        val key = "blue"
        for  {
          _ <- cache.delete(key)
          _ <- cache.insert(key, "value1")
          out1 <- cache.lookup(key)
          _ <- Timer[IO].sleep(1.5.seconds)
          out2 <- cache.lookup(key)
          _ <- Timer[IO].sleep(1.seconds)
          out3 <- cache.lookup(key)
        } yield (out1, out2, out3)
      }.map(_ must_===(("value1".some, "value1".some, None)))
    }
  }

}