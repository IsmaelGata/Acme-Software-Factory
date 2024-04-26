
package acme.features.administrator.configuration;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.configuration.Configuration;
import acme.entities.exchange.Exchange;

@Repository
public interface AdministratorConfigurationRepository extends AbstractRepository {

	@Query("select c from Configuration c")
	Configuration findConfigurationOfSystem();

	@Query("select distinct p.cost.currency from Project p")
	Collection<String> currenciesOfProjects();

	@Query("select distinct c.budget.currency from Contract c")
	Collection<String> currenciesOfContracts();

	@Query("select distinct s.amount.currency from Sponsorship s")
	Collection<String> currenciesOfSponsorships();

	@Query("select distinct i.quantity.currency from Invoice i")
	Collection<String> currenciesOfInvoices();

	default Collection<String> findAllCurrentCurrencies() {
		Collection<String> res;

		res = this.currenciesOfProjects();

		res.addAll(this.currenciesOfContracts());
		res.addAll(this.currenciesOfSponsorships());
		res.addAll(this.currenciesOfInvoices());

		return res.stream().distinct().toList();
	}

	@Query("select e.currency from Exchange e")
	Collection<String> findCurrenciesFromAPI();

	@Query("select e from Exchange e where e.expireDate >= :now")
	Collection<Exchange> findExchangeByCurrency(Date now);
}
