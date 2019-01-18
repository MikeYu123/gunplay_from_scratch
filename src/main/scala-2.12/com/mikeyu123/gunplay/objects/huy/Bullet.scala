package com.mikeyu123.gunplay.objects.huy

import java.util.UUID

import org.dyn4j.dynamics.Body
import org.dyn4j.geometry.Geometry
import Bullet.{defaultHeight, defaultWidth}
import com.mikeyu123.gunplay.utils.Vector2
import org.dyn4j.geometry
import geometry.MassType

object Bullet {
  val defaultWidth = 5
  val defaultHeight = 1
}
class Bullet(val emitent: UUID,
             width: Double = defaultWidth,
             height: Double = defaultHeight,
             position: geometry.Vector2 = Vector2(0, 0),
             velocity: geometry.Vector2 = Vector2(0, 0)) extends Body() {
  addFixture(Geometry.createRectangle(width, height))
  setLinearVelocity(velocity)
  translate(position)
  setAngularVelocity(0.0)
  setMass(MassType.FIXED_LINEAR_VELOCITY)
}
