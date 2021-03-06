package com.mikeyu123.gunplay.objects

import java.util.UUID

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.{Geometry, MassType}
import Wall.{defaultHeight, defaultWidth}
import com.mikeyu123.gunplay.utils
import com.mikeyu123.gunplay.utils.Vector2

object Wall {
  val defaultWidth: Double = utils.AppConfig.getInt("wall.width")
  val defaultHeight: Double = utils.AppConfig.getInt("wall.width")
}
class Wall(width: Double = defaultWidth,
           height: Double = defaultHeight,
           position: Vector2 = Vector2(0, 0),
           velocity: Vector2 = Vector2(0, 0))
      extends Body() {
  addFixture(Geometry.createRectangle(width, height))
  translate(position)
  setLinearVelocity(velocity)
  setAngularVelocity(0.0)
  setMass(MassType.INFINITE)
}
