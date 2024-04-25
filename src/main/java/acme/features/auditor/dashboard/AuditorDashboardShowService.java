
package acme.features.auditor.dashboard;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;
import acme.forms.auditor.Dashboard;
import acme.roles.Auditor;

@Service
public class AuditorDashboardShowService extends AbstractService<Auditor, Dashboard> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AuditorDashboardRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		int totalStaticCodeAudits = 0;
		int totalDynamicCodeAudits = 0;
		Double averageAuditRecords;
		Double deviationAuditRecords;
		int minimunAuditRecords;
		int maximunAuditRecords;
		Double averageDurationAuditRecords;
		Double deviationDurationAuditRecords;
		double minimunDurationAuditRecords;
		double maximunDurationAuditRecords;
		List<Integer> auditRecordsCounts = null;
		List<Object[]> listPeriods = null;

		Auditor auditor = this.repository.findOneAuditorById(super.getRequest().getPrincipal().getActiveRoleId());

		// Llamadas al repositorio para obtener los valores
		if (auditor != null) {
			totalStaticCodeAudits = this.repository.totalStaticCodeAudits(auditor.getId());
			totalDynamicCodeAudits = this.repository.totalDynamicCodeAudits(auditor.getId());
			auditRecordsCounts = this.repository.findRecordCountsByAudit(auditor.getId());
			listPeriods = this.repository.findPeriods(auditor.getId());
		}

		if (auditRecordsCounts != null && listPeriods != null) {
			// Listado de duraciones de registros
			List<Long> durations = new ArrayList<>();
			for (Object[] period : listPeriods) {
				// Extraer los periodos de inicio y fin
				java.sql.Timestamp startPeriodTimestamp = (java.sql.Timestamp) period[0];
				java.sql.Timestamp endPeriodTimestamp = (java.sql.Timestamp) period[1];

				LocalDateTime startPeriod = startPeriodTimestamp.toLocalDateTime();
				LocalDateTime endPeriod = endPeriodTimestamp.toLocalDateTime();

				// Calcular la duración en segundos usando Duration
				long duration = Duration.between(startPeriod, endPeriod).get(ChronoUnit.SECONDS);
				durations.add(duration);
			}

			// Calculos cantidades de registros
			averageAuditRecords = auditRecordsCounts.stream().mapToInt(Integer::intValue).average().orElse(0);
			double countVariance = auditRecordsCounts.stream().mapToDouble(count -> Math.pow(count - averageAuditRecords, 2)).average().orElse(0);
			deviationAuditRecords = Math.sqrt(countVariance);
			minimunAuditRecords = auditRecordsCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
			maximunAuditRecords = auditRecordsCounts.stream().mapToInt(Integer::intValue).max().orElse(0);

			// Calculos duraciones de registros
			double averageDurationAuditRecordsInSeconds = durations.stream().mapToLong(Long::longValue).average().orElse(0);
			averageDurationAuditRecords = (double) Duration.of((long) averageDurationAuditRecordsInSeconds, ChronoUnit.SECONDS).toMinutes();
			averageDurationAuditRecords = Math.round(averageDurationAuditRecords / 60.0 * 10) / 10.0;

			double durationVariance = durations.stream().mapToDouble(duration -> Math.pow(duration - averageDurationAuditRecordsInSeconds, 2)).average().orElse(0);
			long deviationDurationAuditRecordsInSeconds = (long) Math.sqrt(durationVariance);
			deviationDurationAuditRecords = (double) Duration.of(deviationDurationAuditRecordsInSeconds, ChronoUnit.SECONDS).toMinutes();
			deviationDurationAuditRecords = Math.round(deviationDurationAuditRecords / 60.0 * 10) / 10.0;

			minimunDurationAuditRecords = durations.stream().mapToLong(Long::longValue).min().orElse(0);
			minimunDurationAuditRecords = Duration.of((long) minimunDurationAuditRecords, ChronoUnit.SECONDS).toMinutes();
			minimunDurationAuditRecords = Math.round(minimunDurationAuditRecords / 60.0 * 10) / 10.0;

			maximunDurationAuditRecords = durations.stream().mapToLong(Long::longValue).max().orElse(0);
			maximunDurationAuditRecords = Duration.of((long) maximunDurationAuditRecords, ChronoUnit.SECONDS).toMinutes();
			maximunDurationAuditRecords = Math.round(maximunDurationAuditRecords / 60.0 * 10) / 10.0;
		} else {
			totalStaticCodeAudits = 0;
			totalDynamicCodeAudits = 0;
			averageAuditRecords = 0.;
			deviationAuditRecords = 0.;
			minimunAuditRecords = 0;
			maximunAuditRecords = 0;
			averageDurationAuditRecords = 0.;
			deviationDurationAuditRecords = 0.;
			minimunDurationAuditRecords = 0;
			maximunDurationAuditRecords = 0;
		}
		// Asignación de valores calculados
		Dashboard dashboard = new Dashboard();

		dashboard.setTotalStaticCodeAudits(totalStaticCodeAudits);
		dashboard.setTotalDynamicCodeAudits(totalDynamicCodeAudits);
		dashboard.setAverageAuditRecords(averageAuditRecords);
		dashboard.setDeviationAuditRecords(deviationAuditRecords);
		dashboard.setMinimunAuditRecords(minimunAuditRecords);
		dashboard.setMaximunAuditRecords(maximunAuditRecords);
		dashboard.setAverageDurationAuditRecords(averageDurationAuditRecords);
		dashboard.setDeviationDurationAuditRecords(deviationDurationAuditRecords);
		dashboard.setMinimunDurationAuditRecords(minimunDurationAuditRecords);
		dashboard.setMaximunDurationAuditRecords(maximunDurationAuditRecords);

		super.getBuffer().addData(dashboard);
	}

	@Override
	public void unbind(final Dashboard object) {
		Dataset dataset;

		dataset = super.unbind(object, //
			"totalStaticCodeAudits", "totalDynamicCodeAudits", // 
			"averageAuditRecords", "deviationAuditRecords", //
			"minimunAuditRecords", "maximunAuditRecords", //
			"averageDurationAuditRecords", "deviationDurationAuditRecords", //
			"minimunDurationAuditRecords", "maximunDurationAuditRecords");

		super.getResponse().addData(dataset);
	}

}