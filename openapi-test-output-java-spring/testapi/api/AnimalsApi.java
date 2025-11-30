package testapi.api;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import testapi.model.Animal;

@RestController
@RequestMapping("/animals")
public sealed interface AnimalsApi {
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  /** List all animals (polymorphic) */
  List<Animal> listAnimals();
}