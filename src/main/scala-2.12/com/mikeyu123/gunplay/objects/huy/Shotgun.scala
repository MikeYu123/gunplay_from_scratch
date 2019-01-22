package com.mikeyu123.gunplay.objects.huy

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.mikeyu123.gunplay.utils
import com.mikeyu123.gunplay.utils.Vector2



//TODO bullets velocity override
object Shotgun {
  val bulletOffset = utils.AppConfig.getInt("bullet.offset")
  val span: Long = utils.AppConfig.getLong("shotgun.span")
  val ammo = utils.AppConfig.getDouble("shotgun.ammo")
  def apply(span: Long = span, ammo: Double = ammo) = new Shotgun(span, ammo)
}
class Shotgun(span: Long = Shotgun.span, var ammo: Double = Shotgun.ammo) extends Weapon {
  var lastFired = Instant.now

  def emit(player: Player): Set[Bullet] = {
    if(lastFired.plus(span, ChronoUnit.MILLIS).isBefore(Instant.now) && ammo > 0) {
      val bullet1 = new Bullet(player.getId, position =
        player.getWorldCenter.add(
          Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation)),
        velocity = Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation)
      )
      val bullet2 = new Bullet(player.getId, position =
        player.getWorldCenter.add(
          Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation)),
        velocity = Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation - Math.PI / 6)
      )
      val bullet3 = new Bullet(player.getId, position =
        player.getWorldCenter.add(
          Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation)),
        velocity = Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation + Math.PI / 6)
      )
      val bullet4 = new Bullet(player.getId, position =
        player.getWorldCenter.add(
          Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation)),
        velocity = Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation - Math.PI / 12)
      )
      val bullet5 = new Bullet(player.getId, position =
        player.getWorldCenter.add(
          Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation)),
        velocity = Vector2(Pistol.bulletOffset, 0).rotate(player.getTransform.getRotation + Math.PI / 12)
      )
      bullet1.getTransform.setRotation(player.getTransform.getRotation)
      bullet1.setAsleep(false)
      bullet2.getTransform.setRotation(player.getTransform.getRotation - Math.PI / 6)
      bullet2.setAsleep(false)
      bullet3.getTransform.setRotation(player.getTransform.getRotation + Math.PI / 6)
      bullet3.setAsleep(false)
      bullet4.getTransform.setRotation(player.getTransform.getRotation - Math.PI / 12)
      bullet4.setAsleep(false)
      bullet5.getTransform.setRotation(player.getTransform.getRotation + Math.PI / 12)
      bullet5.setAsleep(false)
      lastFired = Instant.now
      ammo -= 1
      Set(bullet1, bullet2, bullet3, bullet4, bullet5)
    }
    else {
      Set()
    }
  }
}