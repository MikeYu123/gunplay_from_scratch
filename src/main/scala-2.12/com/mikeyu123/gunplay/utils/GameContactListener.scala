package com.mikeyu123.gunplay.utils

import com.mikeyu123.gunplay_physics.structs.{Contact, ContactListener}

class GameContactListener extends ContactListener {
  override def preSolve(contact: Contact) = super.preSolve(contact)

  override def postSolve(contact: Contact) = super.postSolve(contact)
}