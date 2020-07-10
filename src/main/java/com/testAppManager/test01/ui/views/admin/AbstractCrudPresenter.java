package com.testAppManager.test01.ui.views.admin;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.inject.spi.CDI;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.StatusChangeEvent;
import com.vaadin.data.ValidationException;
import com.vaadin.data.ValidationResult;
import com.vaadin.navigator.ViewBeforeLeaveEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.testAppManager.test01.app.HasLogger;
import com.testAppManager.test01.backend.data.entity.AbstractEntity;
import com.testAppManager.test01.backend.service.CrudService;
import com.testAppManager.test01.backend.service.UserFriendlyDataException;
import com.testAppManager.test01.ui.components.ConfirmPopup;
import com.testAppManager.test01.ui.components.FilterablePageableDataProvider;
import com.testAppManager.test01.ui.navigation.NavigationManager;
import com.testAppManager.test01.ui.views.orderedit.PersistenceExceptionUtil;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public abstract class AbstractCrudPresenter<T extends AbstractEntity, S extends CrudService<T>, V extends AbstractCrudView<T>>
		implements HasLogger, Serializable {

	private V view;

	private final NavigationManager navigationManager;

	private final S service;

	private FilterablePageableDataProvider<T, Object> dataProvider;

	private BeanValidationBinder<T> binder;

	// The model for the view. Not extracted to a class to reduce clutter. If
	// the model becomes more complex, it could be encapsulated in a separate
	// class.
	private T editItem;

	private final Class<T> entityType;

	protected AbstractCrudPresenter(NavigationManager navigationManager, S service, Class<T> entityType,
			FilterablePageableDataProvider<T, Object> dataProvider) {
		this.service = service;
		this.navigationManager = navigationManager;
		this.entityType = entityType;
		this.dataProvider = dataProvider;
		createBinder();
	}

	public void viewEntered(ViewChangeEvent event) {
		if (!event.getParameters().isEmpty()) {
			editRequest(event.getParameters());
		}
	}

	public void beforeLeavingView(ViewBeforeLeaveEvent event) {
		runWithConfirmation(event::navigate, () -> {
			// Nothing special needs to be done if user aborts the navigation
		});
	}

	protected void createBinder() {
		binder = new BeanValidationBinder<>(getEntityType());
		binder.addStatusChangeListener(this::onFormStatusChange);
	}

	protected BeanValidationBinder<T> getBinder() {
		return binder;
	}

	protected S getService() {
		return service;
	}

	protected void filterGrid(String filter) {
		dataProvider.setFilter(filter);
	}

	protected T loadEntity(long id) {
		return service.load(id);
	}

	protected Class<T> getEntityType() {
		return entityType;
	}

	private T createEntity() {
		try {
			return getEntityType().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new UnsupportedOperationException(
					"Entity of type " + getEntityType().getName() + " is missing a public no-args constructor", e);
		}
	}

	protected void deleteEntity(T entity) {
		if (entity.isNew()) {
			throw new IllegalArgumentException("Cannot delete an entity which is not in the database");
		} else {
			service.delete(entity.getId());
		}
	}

	public void init(V view) {
		this.view = view;
		view.setDataProvider(dataProvider);
		view.bindFormFields(getBinder());
		view.showInitialState();
	}

	protected V getView() {
		return view;
	}

	public void editRequest(String parameters) {
		long id;
		try {
			id = Long.parseLong(parameters);
		} catch (NumberFormatException e) {
			id = -1;
		}

		if (id == -1) {
			editItem(createEntity());
		} else {
			selectAndEditEntity(loadEntity(id));
		}
	}

	private void selectAndEditEntity(T entity) {
		getView().getGrid().select(entity);
		editRequest(entity);
	}

	public void editRequest(T entity) {
		runWithConfirmation(() -> {
			// Fetch a fresh item so we have the latest changes (less optimistic
			// locking problems)
			T freshEntity = loadEntity(entity.getId());
			editItem(freshEntity);
		}, () -> {
			// Revert selection in grid
			Grid<T> grid = getView().getGrid();
			if (editItem == null) {
				grid.deselectAll();
			} else {
				grid.select(editItem);
			}
		});
	}

	protected void editItem(T item) {
		if (item == null) {
			throw new IllegalArgumentException("The entity to edit cannot be null");
		}
		this.editItem = item;

		boolean isNew = item.isNew();
		if (isNew) {
			navigationManager.updateViewParameter("new");
		} else {
			navigationManager.updateViewParameter(String.valueOf(item.getId()));
		}

		getBinder().readBean(editItem);
		getView().editItem(isNew);
	}

	public void addNewClicked() {
		runWithConfirmation(() -> {
			T entity = createEntity();
			editItem(entity);
		}, () -> {
		});
	}

	/**
	 * Runs the given command if the form contains no unsaved changes or if the
	 * user clicks ok in the confirmation dialog telling about unsaved changes.
	 *
	 * @param onConfirmation
	 *            the command to run if there are not changes or user pushes
	 *            {@literal confirm}
	 * @param onCancel
	 *            the command to run if there are changes and the user pushes
	 *            {@literal cancel}
	 * @return <code>true</code> if the {@literal confirm} command was run
	 *         immediately, <code>false</code> otherwise
	 */
	private void runWithConfirmation(Runnable onConfirmation, Runnable onCancel) {
		if (hasUnsavedChanges()) {
			ConfirmPopup confirmPopup = CDI.current().select(ConfirmPopup.class).get();
			confirmPopup.showLeaveViewConfirmDialog(view, onConfirmation, onCancel);
		} else {
			onConfirmation.run();
		}
	}

	private boolean hasUnsavedChanges() {
		return editItem != null && getBinder().hasChanges();
	}

	public void updateClicked() {
		try {
			// The validate() call is needed only to ensure that the error
			// indicator is properly shown for the field in case of an error
			getBinder().validate();
			getBinder().writeBean(editItem);
		} catch (ValidationException e) {
			// Commit failed because of validation errors
			List<BindingValidationStatus<?>> fieldErrors = e.getFieldValidationErrors();
			if (!fieldErrors.isEmpty()) {
				// Field level error
				HasValue<?> firstErrorField = fieldErrors.get(0).getField();
				getView().focusField(firstErrorField);
			} else {
				// Bean validation error
				ValidationResult firstError = e.getBeanValidationErrors().get(0);
				Notification.show(firstError.getErrorMessage(), Type.ERROR_MESSAGE);
			}
			return;
		}

		boolean isNew = editItem.isNew();
		T entity;
		try {
			entity = service.save(editItem);
		} catch (UserFriendlyDataException e) {
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			getLogger().debug("Unable to update entity of type " + editItem.getClass().getName(), e);
			return;
		} catch (Exception e) {
			if (PersistenceExceptionUtil.isOptimisticLockingException(e)) {
				// Somebody else probably edited the data at the same time
				Notification.show("Somebody else might have updated the data. Please refresh and try again.",
						Type.ERROR_MESSAGE);
				getLogger().debug(
						"Optimistic locking error while saving entity of type " + editItem.getClass().getName(), e);
			} else if (PersistenceExceptionUtil.isConstraintViolationException(e)) {
				// Should not get here if validation is setup properly
				Notification.show("Please check the contents of the fields: " + e.getMessage(), Type.ERROR_MESSAGE);
				getLogger().error(
						"Constraint violation error during save entity of type " + editItem.getClass().getName(), e);
			} else {
				// Something went wrong, no idea what
				Notification.show("A problem occured while saving the data. Please check the fields.",
						Type.ERROR_MESSAGE);
				getLogger().error("Unable to save entity of type " + editItem.getClass().getName(), e);
			}
			return;
		}

		if (isNew) {
			// Move to the "Updating an entity" state
			dataProvider.refreshAll();
			selectAndEditEntity(entity);
		} else {
			// Stay in the "Updating an entity" state
			dataProvider.refreshItem(entity);
			editRequest(entity);
		}
	}

	public void cancelClicked() {
		if (editItem.isNew()) {
			revertToInitialState();
		} else {
			editItem(editItem);
		}
	}

	private void revertToInitialState() {
		editItem = null;
		getBinder().readBean(null);
		getView().showInitialState();
		navigationManager.updateViewParameter("");
	}

	public void deleteClicked() {
		try {
			deleteEntity(editItem);
		} catch (UserFriendlyDataException e) {
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			getLogger().debug("Unable to delete entity of type " + editItem.getClass().getName(), e);
			return;
		} catch (Exception e) {
			if (PersistenceExceptionUtil.isConstraintViolationException(e)) {
				Notification.show("The given entity cannot be deleted as there are references to it in the database",
						Type.ERROR_MESSAGE);
				getLogger().error("Unable to delete entity of type " + editItem.getClass().getName(), e);
			} else {
				// Something went wrong, no idea what
				Notification.show("An unknown problem occured while deleting the data.", Type.ERROR_MESSAGE);
				getLogger().error("Unable to delete entity of type " + editItem.getClass().getName(), e);
			}
			return;
		}
		dataProvider.refreshAll();
		revertToInitialState();
	}

	public void onFormStatusChange(StatusChangeEvent event) {
		boolean hasChanges = event.getBinder().hasChanges();
		boolean hasValidationErrors = event.hasValidationErrors();
		getView().setUpdateEnabled(hasChanges && !hasValidationErrors);
		getView().setCancelEnabled(hasChanges);
	}

}
