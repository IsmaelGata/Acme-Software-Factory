
package acme.features.authenticated.developer.trainingsession;

import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractService;
import acme.entities.trainingmodules.TrainingModule;
import acme.entities.trainingsessions.TrainingSession;
import acme.roles.Developer;

@Service
public class DeveloperTrainingSessionUpdateService extends AbstractService<Developer, TrainingSession> {

	// Internal state ---------------------------------------------------------

	@Autowired
	public DeveloperTrainingSessionRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int masterId;
		TrainingSession trainingSession;

		masterId = super.getRequest().getData("id", int.class);
		trainingSession = this.repository.findOneTrainingSessionById(masterId);
		status = trainingSession != null && trainingSession.isDraftMode() && super.getRequest().getPrincipal().hasRole(trainingSession.getTrainingModule().getDeveloper());

		super.getResponse().setAuthorised(status);
	}
	@Override
	public void load() {
		TrainingSession object;
		int masterId;

		masterId = super.getRequest().getData("id", int.class);
		object = this.repository.findOneTrainingSessionById(masterId);

		super.getBuffer().addData(object);

	}

	@Override
	public void bind(final TrainingSession object) {
		assert object != null;

		super.bind(object, "code", "startTime", "endTime", "location", "instructor", "contactEmail", "furtherInformationLink");

	}

	@Override
	public void validate(final TrainingSession object) {
		assert object != null;

		if (!super.getBuffer().getErrors().hasErrors("code")) {
			TrainingSession existing;

			existing = this.repository.findOneTrainingSessionByCode(object.getCode());
			super.state(existing == null, "reference", "developer.trainingSession.form.error.duplicated");
		}

		if (!super.getBuffer().getErrors().hasErrors("startTime") && !super.getBuffer().getErrors().hasErrors("endTime")) {
			Date startTime = MomentHelper.deltaFromMoment(object.getStartTime(), 1, ChronoUnit.WEEKS);

			// Comprobamos que sea una semana
			super.state(MomentHelper.isAfter(object.getEndTime(), startTime), "endTime", "developer.trainingSession.form.error.update-moment-less-than-week");
		}

		int masterId = super.getRequest().getData("trainingModuleId", int.class);
		TrainingModule trainingModule = this.repository.findOneTrainingModuleById(masterId);
		final boolean noDraftTrainingModule = trainingModule.isDraftMode();
		super.state(noDraftTrainingModule, "*", "developer.trainingSession.form.error.trainingModule-noDraft");

	}
	@Override
	public void perform(final TrainingSession object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final TrainingSession object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbind(object, "code", "startTime", "endTime", "location", "instructor", "contactEmail", "furtherInformationLink", "draftmode");
		dataset.put("masterId", object.getTrainingModule().getId());

		super.getResponse().addData(dataset);
	}

}
