package adventureworks.update_person_returning;

import adventureworks.WithConnection;
import adventureworks.customtypes.TypoLocalDateTime;
import org.junit.Test;

import java.util.Optional;

public class UpdatePersonReturningSqlRepoTest {
    private final UpdatePersonReturningSqlRepoImpl updatePersonReturningSqlRepo = new UpdatePersonReturningSqlRepoImpl();

    @Test
    public void timestampWorks() {
        WithConnection.run(c -> {
            updatePersonReturningSqlRepo.apply(
                    Optional.of("1"),
                    Optional.of(TypoLocalDateTime.now()),
                    c
            );
        });
    }
}
