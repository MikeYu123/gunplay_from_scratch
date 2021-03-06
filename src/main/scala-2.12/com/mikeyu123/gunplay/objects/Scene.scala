package com.mikeyu123.gunplay.objects

import java.util.UUID

import com.mikeyu123.gunplay.objects._
import Door.Pin
import Scene.{Murder, WorldUpdates}
import com.mikeyu123.gunplay.utils
import com.mikeyu123.gunplay.utils.LevelParser.LevelData
import com.mikeyu123.gunplay.utils.{SpawnPool, Vector2}
import com.mikeyu123.gunplay.weapons.{Pistol, Riffle, Shotgun}
import org.dyn4j.collision.manifold.Manifold
import org.dyn4j.collision.narrowphase.Penetration

import scala.collection.JavaConverters._
import org.dyn4j.dynamics._
import org.dyn4j.dynamics.contact._

import scala.collection.mutable
import scala.util.Random

object Scene {
  val stepNumber = utils.AppConfig.getInt("world.stepNumber")
  case class Murder(killer: UUID, victim: UUID)
  case class WorldUpdates(bodies: Set[Player] = Set(), bullets: Set[Body] = Set(), doors: Set[Body] = Set(), drops: Set[Drop] = Set())
  def fromLevel(level: LevelData): Scene = {
    val walls = level.walls.map { wallData =>
      new Wall(wallData.width, wallData.height, Vector2(wallData.x, wallData.y), Vector2(0,0))
    }
    val doors = level.doors.map { doorData =>
      new Door(doorData.width, doorData.height, Vector2(doorData.x, doorData.y), Vector2(0,0), Door.pin(doorData.pin))
    }
    val scene = new Scene(SpawnPool(level.spawns), SpawnPool(level.dropSpawns), stepNumber)
    walls.foreach(wall => scene.world.addBody(wall))
    doors.foreach(door => {
      scene.world.addBody(door)
      scene.world.addBody(door.pin)
      scene.world.addJoint(door.joint)
    })
    scene
  }
}

class Scene(val spawnPool: SpawnPool = SpawnPool.defaultPool, val dropSpawns: SpawnPool = SpawnPool.defaultPool, stepNumber: Int = Scene.stepNumber) {
  val world = new World()
  world.setGravity(Vector2(0,0))
  var bodiesToRemove = collection.mutable.Set[Body]()
  val murders = collection.mutable.Set[Murder]()
//  FIXME this is kostyl
  val drops = collection.mutable.Map[Vector2, Drop]()


  def handlePlayerDeath(player: Body, bullet: Body) = {
//    Check if body emitted by player
    val emitentId = bullet.asInstanceOf[Bullet].emitent
    val playerId = player.getId
    if(!emitentId.equals(playerId)) {
      bodiesToRemove add player
      murders.add(Murder(emitentId, playerId))
    }
  }

  def handleBulletDisposal(bullet: Body) = {
    bodiesToRemove add bullet
  }

  def handleDropInteraction(player: Player, drop: Drop) = {
    player.weapon match {
      case None =>
        player.weapon = Some(drop.weapon)
        bodiesToRemove add drop
        val translation = drop.getTransform.getTranslation()
        drops.remove(Vector2(translation.x, translation.y))
      case _ =>
    }
  }

  val listener = new CollisionListener {
//    TODO player-drop interaction
    def internalCollisionHandler(body1: Body, body2: Body) = {
      (body1, body2) match {
        case (player: Player, drop: Drop) =>
          handleDropInteraction(player, drop)
          false
        case (drop: Drop, player: Player) =>
          handleDropInteraction(player, drop)
          false
        case (_: Drop, _) | (_, _: Drop) =>
          false
        case (_: Player, _: Bullet) =>
          handlePlayerDeath(body1, body2)
          false
        case (_: Bullet, _:Player) =>
          handlePlayerDeath(body2, body1)
          false
        case (_: Bullet, _: Wall) =>
          handleBulletDisposal(body1)
          false
        case (_: Bullet, _: Door) =>
          handleBulletDisposal(body1)
          false
        case (_: Wall, _: Bullet) =>
          handleBulletDisposal(body2)
        case (_: Pin, _: Bullet) =>
          handleBulletDisposal(body2)
          true
        case (_: Bullet, _: Pin) =>
          handleBulletDisposal(body1)
          true
        case (_: Door, _: Bullet) =>
          handleBulletDisposal(body2)
          false
        case (_: Player, _: Wall) =>
          true
        case (_: Wall, _: Player) =>
          true
        case (_: Player, _: Door) =>
          true
        case (_: Door, _: Player) =>
          true
        case (_: Door, _: Wall) =>
//          true
          false
        case (_: Wall, _: Door) =>
          false
//          true
        case x => false
      }
    }
    override def collision(body1: Body, fixture1: BodyFixture, body2: Body, fixture2: BodyFixture): Boolean =
      internalCollisionHandler(body1, body2)

    override def collision(body1: Body, fixture1: BodyFixture, body2: Body, fixture2: BodyFixture, penetration: Penetration): Boolean =
      internalCollisionHandler(body1, body2)

    override def collision(body1: Body, fixture1: BodyFixture, body2: Body, fixture2: BodyFixture, manifold: Manifold): Boolean =
      internalCollisionHandler(body1, body2)

    override def collision(contactConstraint: ContactConstraint): Boolean =
      internalCollisionHandler(contactConstraint.getBody1, contactConstraint.getBody2)
  }
  world.addListener(listener)

  def addPlayer: Player = {
    val player = Player(position = spawnPool.randomSpawn)
    //    player.weapon = Random.shuffle(Shotgun() :: Pistol() :: Riffle() :: Nil).headOption
    player.weapon = None
    world.addBody(player)
    player
  }

  def placeDrop: Drop = {
    val weapon = Random.shuffle(Shotgun() :: Pistol() :: Riffle() :: Nil).head
    val spawn = dropSpawns.randomSpawn
    drops.get(spawn) match {
      case Some(drop) => drop
      case None =>
        val drop = Drop(weapon, position = spawn)
        drops.put(spawn, drop)
        world.addBody(drop)
        drop
    }
  }

  def dropWeapon(uuid: UUID): Unit = {
    world.getBodies.asScala.find {
      body =>
        body.getId.equals(uuid)
    }.foreach(player =>
      player.asInstanceOf[Player].weapon = None
    )
  }

  def emitBullet(uuid: UUID): Unit = {
    world.getBodies.asScala.find {
      body =>
        body.getId.equals(uuid)
    }.foreach(player => {
//      TODO: recalculate velocity via angle
      for {
        bullet <- player.asInstanceOf[Player].emitBullets
      } world.addBody(bullet)
    })
  }

  def updateControls(uuid: UUID, velocity: Vector2, angular: Double): Unit = {
    world.getBodies.asScala.find {
      body =>
        body.getId.equals(uuid)
    }.foreach(player => {
      player.setLinearVelocity(velocity)
//      player.setAngularVelocity(angular)
//      player.applyTorque(angular)
      player.getTransform.setRotation(angular)
      player.setAsleep(false)
    })
  }

  def step: Set[Murder] = {
    world.step(stepNumber)
    bodiesToRemove.foreach(world.removeBody)
    bodiesToRemove.clear
    val returnSet: Set[Scene.Murder] = murders.toSet
    murders.clear()
    returnSet
  }

  def removePlayerById(uuid: UUID): Unit = {
    world.getBodies.asScala.find {
      body =>
        body.getId.equals(uuid)
    }.foreach(player => {
      world.removeBody(player)
    })
  }

  def updates: WorldUpdates = {
    world.getBodies.asScala.foldLeft(WorldUpdates())((acc: WorldUpdates, obj: Body) => {
      obj match {
        case x: Player => WorldUpdates(acc.bodies + x, acc.bullets, acc.doors, acc.drops)
        case x: Bullet => WorldUpdates(acc.bodies, acc.bullets + obj, acc.doors, acc.drops)
        case x: Door => WorldUpdates(acc.bodies, acc.bullets, acc.doors + obj, acc.drops)
        case x: Drop => WorldUpdates(acc.bodies, acc.bullets, acc.doors, acc.drops + x)
        case _ => acc
      }
    })
  }
}
