package testapi.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;

public interface PetStatusChangedWebhook {
  /** Called when a pet's status changes */
  Uni<Void> onPetStatusChanged(JsonNode body);
}
