package zachkingcade.dev.ledger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Context load requires full DB wiring; covered by unit and slice tests.")
@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
				"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
				"org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class LedgerApplicationTests {

}
