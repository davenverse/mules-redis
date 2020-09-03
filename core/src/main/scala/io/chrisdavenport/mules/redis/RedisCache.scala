package io.chrisdavenport.mules.redis

import cats._
import cats.syntax.all._
import dev.profunktor.redis4cats.algebra.KeyCommands
import dev.profunktor.redis4cats.algebra.StringCommands
import dev.profunktor.redis4cats.effects._

import io.chrisdavenport.mules.Cache
import io.chrisdavenport.mules.TimeSpec
import scala.concurrent.duration._

object RedisCache {

  def fromCommands[F[_]: Functor, K, V](
    commands: KeyCommands[F, K] with StringCommands[F, K, V],
    defaultTimeout: Option[TimeSpec]
  ): Cache[F, K, V] = 
    new RedisCacheImpl[F, K, V](commands, commands, defaultTimeout)

  private class RedisCacheImpl[F[_]: Functor, K, V](
    s: StringCommands[F, K, V],
    key: KeyCommands[F, K],
    defaultTimeout: Option[TimeSpec]
  ) extends Cache[F, K, V]{
    def insert(k: K, v: V): F[Unit] = 
      s.set(k, v, SetArgs(None, defaultTimeout.map(n => SetArg.Ttl.Px(n.nanos.nanos))))
        .void
    def lookup(k: K): F[Option[V]] = 
      s.get(k)
    def delete(k: K): F[Unit] = 
      key.del(k)
  }
  
}