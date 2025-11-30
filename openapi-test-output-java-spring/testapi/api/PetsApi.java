package testapi.api;

import java.lang.Void;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import testapi.model.Pet;
import testapi.model.PetCreate;

@RestController
@RequestMapping("/pets")
public sealed interface PetsApi {
  @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  /** Create a pet */
  CreatePetResponse createPet(@RequestBody PetCreate body);

  @DeleteMapping(value = "/{petId}")
  /** Delete a pet */
  Void deletePet(
  
    /** The pet ID */
    @PathVariable("petId") String petId
  );

  @GetMapping(value = "/{petId}", produces = MediaType.APPLICATION_JSON_VALUE)
  /** Get a pet by ID */
  GetPetResponse getPet(
  
    /** The pet ID */
    @PathVariable("petId") String petId
  );

  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  /** List all pets */
  List<Pet> listPets(
    /** Maximum number of pets to return */
    @RequestParam(name = "limit", required = false, defaultValue = "20") Optional<Integer> limit,
    /** Filter by status */
    @RequestParam(name = "status", required = false, defaultValue = "available") Optional<String> status
  );
}