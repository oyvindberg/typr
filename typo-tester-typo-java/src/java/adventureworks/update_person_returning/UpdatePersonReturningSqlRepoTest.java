package adventureworks.update_person_returning;

import adventureworks.WithConnection;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;

public class UpdatePersonReturningSqlRepoTest {
  private final UpdatePersonReturningSqlRepoImpl updatePersonReturningSqlRepo =
      new UpdatePersonReturningSqlRepoImpl();

  @Test
  public void timestampWorks() {
    WithConnection.run(
        c -> {
          updatePersonReturningSqlRepo.apply(Optional.of("1"), Optional.of(LocalDateTime.now()), c);
        });
  }
}
