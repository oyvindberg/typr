package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import io.smallrye.mutiny.Uni

interface PetStatusChangedWebhook {
  /** Called when a pet's status changes */
  abstract fun onPetStatusChanged(body: JsonNode): Uni<Unit>
}