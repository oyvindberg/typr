package adventureworks;

import adventureworks.customtypes.Defaulted;
import adventureworks.customtypes.TypoLocalDate;
import adventureworks.humanresources.employee.EmployeeRepoImpl;
import adventureworks.person.businessentity.BusinessentityRepoImpl;
import adventureworks.person.emailaddress.EmailaddressRepoImpl;
import adventureworks.person.person.PersonRepoImpl;
import adventureworks.public_.Name;
import adventureworks.sales.salesperson.SalespersonRepoImpl;
import adventureworks.userdefined.FirstName;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Tests for DSL join chains - equivalent to Scala DSLTest.
 */
public class DSLTest extends SnapshotTest {
    private final BusinessentityRepoImpl businessentityRepoImpl = new BusinessentityRepoImpl();
    private final PersonRepoImpl personRepoImpl = new PersonRepoImpl();
    private final EmployeeRepoImpl employeeRepoImpl = new EmployeeRepoImpl();
    private final SalespersonRepoImpl salespersonRepoImpl = new SalespersonRepoImpl();
    private final EmailaddressRepoImpl emailaddressRepoImpl = new EmailaddressRepoImpl();

    @Test
    public void works() {
        WithConnection.run(c -> {
            var testInsert = new TestInsert(new java.util.Random(0), new DomainInsertImpl());

            // Create businessentity
            var businessentityRow = testInsert.personBusinessentity(
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    c
            );

            // Create person
            var personRow = testInsert.personPerson(
                    businessentityRow.businessentityid(),
                    "EM",  // persontype
                    new FirstName("a"),
                    Optional.empty(),  // title
                    Optional.empty(),  // middlename
                    new Name("lastname"),
                    Optional.empty(),  // suffix
                    Optional.empty(),  // additionalcontactinfo
                    Optional.empty(),  // demographics
                    new Defaulted.UseDefault<>(),  // namestyle
                    new Defaulted.UseDefault<>(),  // emailpromotion
                    new Defaulted.UseDefault<>(),  // rowguid
                    new Defaulted.UseDefault<>(),  // modifieddate
                    c
            );

            // Create emailaddress
            testInsert.personEmailaddress(
                    personRow.businessentityid(),
                    Optional.of("a@b.c"),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    c
            );

            // Create employee
            var employeeRow = testInsert.humanresourcesEmployee(
                    personRow.businessentityid(),
                    new TypoLocalDate(java.time.LocalDate.of(1998, 1, 1)),
                    "M",  // maritalstatus
                    "M",  // gender
                    new TypoLocalDate(java.time.LocalDate.of(1997, 1, 1)),
                    "12345",  // nationalidnumber
                    "adventure-works\\a",  // loginid
                    "Test Job",  // jobtitle
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    c
            );

            // Create salesperson
            var salespersonRow = testInsert.salesSalesperson(
                    employeeRow.businessentityid(),
                    Optional.empty(),  // territoryid
                    Optional.empty(),  // salesquota
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    new Defaulted.UseDefault<>(),
                    c
            );

            // Test join chain: salesperson -> employee -> person -> businessentity, then join with email
            var query = salespersonRepoImpl.select()
                    .where(sp -> sp.rowguid().isEqual(salespersonRow.rowguid()))
                    .joinFk(sp -> sp.fkHumanresourcesEmployee(), employeeRepoImpl.select())
                    .joinFk(sp_e -> sp_e._2().fkPersonPerson(), personRepoImpl.select())
                    .joinFk(sp_e_p -> sp_e_p._2().fkBusinessentity(), businessentityRepoImpl.select())
                    .join(emailaddressRepoImpl.select().orderBy(e -> e.rowguid().asc()))
                    .on(sp_e_p_b_email -> sp_e_p_b_email._2().businessentityid().isEqual(sp_e_p_b_email._1()._2().businessentityid()));

            // Self-join the query
            var doubled = query
                    .join(query)
                    .on(left_right -> left_right._1()._1()._1()._2().businessentityid().isEqual(left_right._2()._1()._1()._2().businessentityid()));

            // Add snapshot test - same as Scala version
            compareFragment("doubled", doubled.sql());

            var results = doubled.toList(c);
            results.forEach(System.out::println);
            assertEquals(1, doubled.count(c));
        });
    }
}
