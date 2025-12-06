package testapi.api

import com.fasterxml.jackson.databind.JsonNode

interface PetStatusChangedWebhook {
  /** Called when a pet's status changes */
  fun onPetStatusChanged(body: JsonNode): Unit
}