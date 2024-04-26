
package acme.features.client.dashboard;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.data.datatypes.Money;
import acme.client.repositories.AbstractRepository;

@Repository
public interface ClientDashboardRepository extends AbstractRepository {

	@Query("select count(p) from ProgressLogs p where p.draftMode = false and p.completeness < :higher and p.completeness >= :lower and p.contract.client.id = :id")
	int logsBetweenCompletenessValuesForClient(int id, double lower, double higher);

	@Query("select count(p) from ProgressLogs p where p.draftMode = false and  p.completeness < :higher and p.contract.client.id =:id")
	int logsBelowCompletenessValue(int id, double higher);

	@Query("select count(p) from ProgressLogs p where p.draftMode = false and p.completeness >= :lower and p.contract.client.id = :id")
	int logsAboveCompletenessValue(int id, double lower);

	@Query("select c.budget from Contract c where c.draftMode = false and  c.client.id = :id")
	Collection<Money> findAllBudgetsFromClient(int id);

	default double currencyTransformerUsd(final Money initial) {
		double res = initial.getAmount();

		if (initial.getCurrency().equals("USD"))
			res = initial.getAmount();

		else if (initial.getCurrency().equals("EUR"))
			res = initial.getAmount() * 1.07;

		else
			res = initial.getAmount() * 1.25;

		return res;
	}
}
