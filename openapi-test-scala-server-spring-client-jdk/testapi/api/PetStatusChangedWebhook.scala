package testapi.api

import io.circe.Json

trait PetStatusChangedWebhook {
  /** Called when a pet's status changes */
  def onPetStatusChanged(body: Json): Unit
}