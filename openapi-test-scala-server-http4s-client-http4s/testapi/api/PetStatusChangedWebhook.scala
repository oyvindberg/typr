package testapi.api

import cats.effect.IO
import io.circe.Json

trait PetStatusChangedWebhook {

  /** Called when a pet's status changes */
  def onPetStatusChanged(body: Json): IO[Unit]
}
