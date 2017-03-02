package eu.domainobjects.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import eu.domainobjects.controller.events.DomainObjectInstanceSelection;
import eu.domainobjects.controller.events.StepEvent;
import eu.domainobjects.presentation.main.MainWindow;
import eu.domainobjects.presentation.main.action.listener.DomainObjectDefinitionSelectionByName;
import eu.domainobjects.presentation.main.events.DomainObjectInstanceSelectionByName;
import eu.domainobjects.presentation.main.events.StoryboardLoadedEvent;
import eu.domainobjects.utils.DoiBean;
import eu.domainobjects.utils.ExternalEvent;
import eu.domainobjects.utils.HandlerInstance;
import eu.domainobjects.utils.PlayRunner;
import eu.domainobjects.utils.ResourceLoader;
import eu.domainobjects.utils.UserData;
import eu.fbk.das.domainobject.executable.Rome2RioCallExecutable;
import eu.fbk.das.process.engine.api.DomainObjectInstance;
import eu.fbk.das.process.engine.api.DomainObjectManagerInterface;
import eu.fbk.das.process.engine.api.domain.DomainObjectDefinition;
import eu.fbk.das.process.engine.api.domain.ObjectDiagram;
import eu.fbk.das.process.engine.api.domain.OnMessageActivity;
import eu.fbk.das.process.engine.api.domain.PickActivity;
import eu.fbk.das.process.engine.api.domain.ProcessActivity;
import eu.fbk.das.process.engine.api.domain.ProcessDiagram;
import eu.fbk.das.process.engine.api.domain.ScopeActivity;
import eu.fbk.das.process.engine.api.domain.WhileActivity;
import eu.fbk.das.process.engine.api.jaxb.ActivityType;
import eu.fbk.das.process.engine.api.jaxb.EventHandlerType;
import eu.fbk.das.process.engine.api.jaxb.ScopeType;
import eu.fbk.das.process.engine.impl.ProcessEngineImpl;

/**
 * Domain Objects Demonstrator's Main controller, use it to post/subscribe
 * events using google's EventBus
 * 
 * @see EventBus
 */
public class MainController {

	private static final Logger logger = LogManager
			.getLogger(ProcessEngineFacade.class);

	private static EventBus eventBus;
	private MainWindow window;
	private ProcessEngineFacade processEngineFacade;

	private Map<String, UserData> userData = new HashMap<String, UserData>();

	private DoiBean current;

	private List<ExternalEvent> externalEvents = new ArrayList<ExternalEvent>();

	private Map<String, Integer> monitorSelectedMap = new HashMap<String, Integer>();

	/**
	 * Construct controller for Demonstrator. Initialize {@link EventBus} with
	 * {@link MainController} instance
	 * 
	 * @param window
	 *            instance
	 */
	public MainController(MainWindow window) {
		eventBus = new EventBus();
		register(this);
		this.window = window;
	}

	/**
	 * Post an event on eventBus
	 * 
	 * @param event
	 *            - a generic object that represent an event, subscribed by a
	 *            method
	 * @see EventBus#post(Object)
	 * 
	 */
	public static void post(Object event) {
		if (eventBus == null) {
			logger.warn("EventBus is not initialized correctly, not possible to post event");
			return;
		}
		eventBus.post(event);
	}

	/**
	 * Register a subscriber to be notified by events
	 * 
	 * 
	 * @param subscriber
	 *            object
	 * @see EventBus#register(Object)
	 */
	public static void register(Object subscriber) {
		if (eventBus == null) {
			logger.warn("EventBus is not initialized correctly, not possible to register subscriber");
			return;
		}
		eventBus.register(subscriber);
	}

	@Subscribe
	public void onStoryboardLoaded(StoryboardLoadedEvent sle) {
		try {
			// init scenario: load domainObjectInstances
			processEngineFacade = new ProcessEngineFacade(ResourceLoader
					.getScenarioFile().getParent());
			processEngineFacade.loadScenario(ResourceLoader.getScenarioFile()
					.getName(), this);
			logger.debug("ProcessEngineFacade init complete");
			window.loadDomainObjectInstancesTable(processEngineFacade
					.getDomainObjectInstances());

			// load user information

			// update comboboxModels
			updateComboboxEntities();

			// show main window
			window.showMainScrollPane(true);

			// register handler for executable activities
			registerHandlersForProcessEngine();

			addLog("Storyboard loaded: "
					+ ResourceLoader.getScenarioFile().getAbsolutePath());
		} catch (Exception e) {
			logger.debug("Problem on loading storyboard");
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Register handlers for executable activity. This is a bridge with actual
	 * implementation of a given activity; when processEngine process an
	 * activity with a given name, registered handler will be called and
	 * executed
	 */
	private void registerHandlersForProcessEngine() {

		/**************************************************************/
		processEngineFacade.addExecutableHandler(
				"R2Rcall",
				new Rome2RioCallExecutable(processEngineFacade
						.getProcessEngine()));
		/**************************************************************/

		// handler for hoaa for pre-phase
		// processEngineFacade.addExecutableHandler(
		// "TO_HOAAorganizeTrip",
		// new ToHoaaOrganizeTripExecutable(processEngineFacade
		// .getProcessEngine()));
		// handler for hoaa for execute phase
		// processEngineFacade
		// .addExecutableHandler(
		// "USER_ExecuteTrip",
		// new UserExecuteTripHoaa(processEngineFacade
		// .getProcessEngine()));
	}

	@Subscribe
	public void onDomainObjectInstanceSelection(DomainObjectInstanceSelection e) {
		DoiBean instance = findDoiBeanByName(e.getName());
		if (instance != null) {
			current = instance;
			updateInterface(current);
		}
	}

	private boolean isProcessEnded(String userName) {
		Optional<DomainObjectInstance> dr = processEngineFacade
				.getProcessEngine().getDomainObjectInstances().stream()
				.filter(d -> d.getId().equals(userName)).findFirst();
		if (dr.isPresent()) {
			if (dr.get().getProcess().isEnded()
					|| dr.get().getProcess().isTerminated()) {
				return true;
			}
		}
		return false;
	}

	@Subscribe
	public void onDomainObjectInstanceSelection(
			DomainObjectInstanceSelectionByName e) {
		try {
			if (e.name != null) {
				current = findDoiBeanByName(e.name);
				updateInterface(current);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Subscribe
	public void onDomainObjectDefinition(DomainObjectDefinitionSelectionByName e) {
		// given model name, get first instance: if there is at least one,
		// update interface with it, otherwise search process model by type an
		// populate interface with info
		DoiBean instance = null;
		for (DoiBean db : processEngineFacade.getDomainObjectInstances()) {
			if (db.getName().startsWith(e.name)) {
				instance = db;
				break;
			}
		}
		if (instance != null) {
			current = instance;
			updateInterface(current);
			// reset all information not related directly to cell model
			window.resetCorrelatedCells();
			window.resetContextDetails();
			window.resetCellDetails();
			window.resetProcessExecution();
			window.resetProcessModel();
		} else {
			ProcessDiagram p = processEngineFacade.getModelByType(e.name);
			if (p != null) {
				// display model information
				resetInterface();
				window.displayProcess(p, true, false);
				// update fragments list
				List<String> f = processEngineFacade.getFragmentNames(e.name);
				window.updateSelectedEntityProvidedFragments(f);
				// update monitors
				List<String> monitors = getMonitor(p);
				window.updateMonitors(monitors, -1);
			}
		}
	}

	private void resetInterface() {
		window.resetCellInstances();
		window.resetCorrelatedCells();
		window.resetCellDetails();
		window.resetContextDetails();
		window.resetProcessExecution();
		window.resetProcessModel();
	}

	/**
	 * Update window interface and refresh it
	 */
	public void updateInterface() {
		updateInterface(getCurrentDoiBean());
	}

	/**
	 * Update window interface and refresh it
	 */
	public void updateInterface(DoiBean db) {
		try {
			displayProcessExecution(db);
			displayProcessModel(db);
			updateSelectedEntityDetails(db);
			updateSelectedEntityCorrelations(db);
			updateSelectedEntityProvidedFragments(db);
			updateCellInstances(db);
			updateSelectedEntityKnowledge(db);
			updateComboboxEntities();
			updateMonitor(db);
			window.refreshWindow();
		} catch (Exception e) {
			logger.error("Error in interface update", e);
		}
	}

	private void updateMonitor(DoiBean db) {
		// update monitor list using current db
		DomainObjectInstance doi = processEngineFacade
				.getDomainObjectInstanceForProcess(processEngineFacade
						.getProcessDiagram(db));
		if (doi != null) {
			ProcessDiagram process = doi.getProcess();
			List<String> monitors = getMonitor(process);
			if (monitorSelectedMap.containsKey(doi.getId())) {
				window.updateMonitors(monitors,
						monitorSelectedMap.get(doi.getId()));
			} else {
				window.updateMonitors(monitors, -1);
			}
		}
	}

	private List<String> getMonitor(ProcessDiagram process) {
		List<String> result = new ArrayList<String>();
		if (process == null) {
			return result;
		}
		for (ProcessActivity act : process.getActivities()) {
			if (act.isWhile()) {
				result.addAll(getMonitor(((WhileActivity) act)
						.getDefaultBranch()));
			} else if (act.isScope()) {
				ScopeActivity scope = (ScopeActivity) act;
				for (EventHandlerType eh : scope.getEventHandler()) {
					result.add(getEventHandlerAsString(eh));
				}
				result.addAll(getMonitor(((ScopeActivity) act).getBranch()));
			} else if (act.isPick()) {
				PickActivity pick = (PickActivity) act;
				for (OnMessageActivity msg : pick.getOnMessages()) {
					result.addAll(getMonitor(msg.getBranch()));
				}
			}
		}
		return result;
	}

	private String getEventHandlerAsString(EventHandlerType eh) {
		StringBuilder sb = new StringBuilder();
		// conditions
		sb.append("on(");
		if (eh.getOnDPchange() != null) {
			sb.append(eh.getDpChange().getDpName()).append(" on ")
					.append(eh.getDpChange().getEventName());
		}
		if (eh.getOnExternalEvent() != null) {
			sb.append(eh.getOnExternalEvent().getOnEventName());
		}
		sb.append(") => ");
		// actions

		// if (eh.getDpChange() != null) {
		// for (DomainProperty dp : eh.getOnDPchange().getDomainProperty()) {//
		// sb.append(dp.getDpName() + " = " + dp.getState().toString());
		// }
		// }
		if (eh.getTriggerEvent() != null) {
			sb.append(eh.getTriggerEvent().getName());
		}

		return sb.toString();
	}

	private void updateComboboxEntities() {
		List<String> response = new ArrayList<String>();
		for (DomainObjectDefinition dod : processEngineFacade
				.getDomainObjectDefinitions()) {
			if (!response.contains(dod.getDomainObject().getName())) {
				response.add(dod.getDomainObject().getName());
			}
		}
		window.updateComboxEntities(response);

	}

	private void updateSelectedEntityKnowledge(DoiBean db) {
		if (db == null) {
			return;
		}
		DomainObjectInstance doi = processEngineFacade
				.getDomainObjectInstanceForProcess(processEngineFacade
						.getProcessDiagram(db));
		if (doi != null) {
			List<String> response = new ArrayList<String>();
			response = getKnowledgeValues(response, doi.getInternalKnowledge());
			response = getKnowledgeValues(response, doi.getExternalKnowledge());
			window.updateEntityKnowledge(response);
		}
	}

	private List<String> getKnowledgeValues(List<String> response,
			List<ObjectDiagram> internal) {
		String v = "";
		if (internal != null) {
			for (ObjectDiagram in : internal) {
				v = in.getOid() + " = " + in.getCurrentState();
				if (!response.contains(v)) {
					response.add(v);
				}
			}
		}
		return response;
	}

	private void updateCellInstances(DoiBean db2) {
		if (db2 == null) {
			return;
		}
		List<String> toDisplay = new ArrayList<String>();
		List<DoiBean> instances = getProcessEngineFacade()
				.getDomainObjectInstances();
		if (instances == null) {
			logger.warn("domainObjectInstances null");
			return;
		}
		// put ino cellInstances list a list of all cell instances of same type
		// of current
		String type = getCurrentType(processEngineFacade.getProcessDiagram(db2));
		if (type.isEmpty()) {
			window.updateCellInstances(toDisplay);
			logger.debug("type of current domainObjectInstance not found");
			return;
		}
		// return all instances of same type (f.e. all UMS instances using
		// current type
		List<DoiBean> result = new ArrayList<DoiBean>();
		for (DoiBean db : instances) {
			ProcessDiagram p = processEngineFacade.getProcessDiagram(db);
			if (db.getName().startsWith(type) && p != null && !p.isEnded()) {
				result.add(db);
			}
		}
		for (DoiBean db : result) {
			toDisplay.add(db.getName());
		}

		window.updateCellInstances(toDisplay);
	}

	/**
	 * @param processDiagram
	 * @return type of current process or an empty string if not found
	 * @see ProcessEngineImpl#buildRelevantServices(ProcessDiagram) for info how
	 *      this Id is generated
	 */
	private String getCurrentType(ProcessDiagram process) {
		if (process != null) {
			DomainObjectInstance currentDoi = processEngineFacade
					.getDomainObjectInstanceForProcess(process);
			String name = "";
			if (currentDoi == null) {
				name = process.getName();
			} else {
				if (currentDoi.getId() != null) {
					name = currentDoi.getId();
				}
			}
			int separatorIndex = name
					.indexOf(DomainObjectManagerInterface.ID_SEPARATOR);
			if (separatorIndex != -1) {
				return name.substring(0, separatorIndex);
			}

			return name;
		}
		return "";
	}

	private void displayProcessModel(DoiBean db) {
		if (db == null) {
			return;
		}
		ProcessDiagram model = processEngineFacade.getModel(db.getName());
		window.displayProcess(model, true, false);
	}

	private void displayProcessExecution(DoiBean db) {
		if (db == null) {
			return;
		}
		logger.debug("Display process execution model for db: " + db.getName());
		ProcessDiagram pd = processEngineFacade.getProcessDiagram(db);
		window.displayProcess(pd, false, true);
	}

	@Subscribe
	public void onStep(StepEvent se) {
		try {
			// one step for process engine
			processEngineFacade.step();
			addLog("ProcessEngine step completed");
			// update domainObjectInstances list
			window.updateDomainObjectInstancesTable(processEngineFacade
					.getDomainObjectInstances());
			// update current selected domainObjectInstance
			if (getCurrentDoiBean() == null) {
				window.selectFirstEntityInTable();
				current = getCurrentDoiBean();
			} else if (getCurrentDoiBean().getId() == null
					&& window.getSelectedEntityInTable().isEmpty()
					&& !window.getSelectedCorrelatedEntity().isEmpty()) {
				window.selectFirstEntityInTable();
				current = getCurrentDoiBean();
			} else if (!window.getSelectedCorrelatedEntity().isEmpty()) {
				updateInterface(findDoiBeanByName(window
						.getSelectedCorrelatedEntity()));
				return;
			}
			// update selected entity details
			updateInterface(getCurrentDoiBean());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void updateSelectedEntityProvidedFragments(DoiBean db) {
		if (db == null) {
			return;
		}
		List<String> response = processEngineFacade.getFragmentsFromDoiBean(db
				.getName());
		window.updateSelectedEntityProvidedFragments(response);
	}

	private void updateSelectedEntityDetails(DoiBean db) {
		if (db == null) {
			return;
		}
		List<String> toDisplay = new ArrayList<String>();
		ProcessDiagram process = processEngineFacade.getModel(db.getName());
		if (process != null) {
			String doiName = processEngineFacade.fromProcessModelToDoi(process);
			toDisplay.add("Name :" + doiName);
			toDisplay.add("Process name: " + process.getName());

			window.updateSelectedEntityDetails(toDisplay);
		}
	}

	/**
	 * Update in window correlation of current process identified by index
	 * 
	 * Correlated DomainObjectInstances are :</br> - refinement process
	 * correlated instances, for example User -> refinement1 -> UMS</br> - and
	 * vice versa, so UMS -> refinement 1 -> User
	 * 
	 * @param db
	 */
	private void updateSelectedEntityCorrelations(DoiBean db) {
		if (db == null) {
			return;
		}
		List<String> response = new ArrayList<String>();
		ProcessDiagram process = processEngineFacade.getProcessDiagram(db);
		if (process != null) {
			// using correlated process, get origin of this correlation. So for
			// example if User -> refinement1 -> UMS, origin for UMS is User
			DomainObjectInstance doi = processEngineFacade
					.getDomainObjectInstanceForProcess(process);
			List<DomainObjectInstance> corrs = processEngineFacade
					.getCorrelations(doi);
			if (corrs != null) {
				for (DomainObjectInstance corr : corrs) {
					if (corr != null && !response.contains(corr.getId())
							&& !doi.getId().equals(corr.getId())) {
						response.add(corr.getId());
					}
				}
			}
			// using current process, using refinements information, get in
			// correlation processes, so for example if User-> refinement 1 ->
			// Ums, result is Ums for user, in recursive way
			window.updateSelectedEntityCorrelations(response);
		}
	}

	/**
	 * @return instance of {@link ProcessEngineFacade}
	 */
	public ProcessEngineFacade getProcessEngineFacade() {
		return processEngineFacade;
	}

	/**
	 * @return process currently selected and executed, null if not found. Be
	 *         aware: this is process get directly from processEngine
	 */
	public ProcessDiagram getCurrentProcess() {
		return processEngineFacade.getProcessDiagram(getCurrentDoiBean());
	}

	/**
	 * Using currently selected domain object, select into interface
	 */
	public void selectDomainObject() {
		updateInterface(getCurrentDoiBean());
	}

	public void setUserData(String name, UserData data) {
		userData.put(name, data);
	}

	/**
	 * @return {@link UserData} for given userName
	 */
	public UserData getUserData(String name) {
		if (userData.get(name) == null) {
			userData.put(name, new UserData());
		}
		return userData.get(name);
	}

	/**
	 * Returns a process diagram refined up to the point of current execution
	 * 
	 * @return requested process diagram or void process diagram (no activities)
	 *         if there is no current process
	 */
	public ProcessDiagram getCurrentRefinedProcessDiagram() {
		List<ProcessActivity> activityList = new ArrayList<ProcessActivity>();
		ProcessDiagram pd = getCurrentProcess();
		if (pd == null) {
			return new ProcessDiagram();
		}
		activityList.addAll(pd.getActivities());
		ProcessActivity currentActivity = pd.getCurrentActivity();
		while (processEngineFacade.getRefinement(pd) != null) {
			pd = processEngineFacade.getRefinement(pd);
			int index = activityList.indexOf(currentActivity);
			activityList.addAll(index, pd.getActivities());
			activityList.remove(index + pd.getActivities().size());
			currentActivity = pd.getCurrentActivity();
		}
		// Create process from a list of activities
		ProcessDiagram retPd = new ProcessDiagram(activityList);
		return retPd;
	}

	/**
	 * Returns the activity currently being executed at the bottom of the
	 * refinement tree.
	 * 
	 * @return
	 */
	public ProcessActivity getCurrentlyExecutingRefinedActivity() {
		ProcessDiagram pd = getCurrentProcess();
		while (processEngineFacade.getRefinement(pd) != null) {
			pd = processEngineFacade.getRefinement(pd);
		}
		return pd.getCurrentActivity();
	}

	/**
	 * Display journey alternative in window for given username
	 * 
	 * @param userName
	 */
	public void displayAlternativesFor(String userName) {
		if (userName == null) {
			logger.error("Not possible to show alternatives for a null username");
			return;
		}
		UserData ud = userData.get(userName);
		if (ud == null) {
			logger.warn("Not found any data for name " + userName);
			return;
		}
		window.displayAlternativesWindow(ud);
	}

	/**
	 * @return selected DoiBean using interface information
	 */
	public DoiBean getCurrentDoiBean() {
		String id = window.getSelectedEntityInTable();
		if (id != null && !id.isEmpty()) {
			for (DoiBean d : processEngineFacade.getDomainObjectInstances()) {
				if (d.getId().equals(id)) {
					return d;
				}
			}
		}
		return current;
	}

	private DoiBean findDoiBeanById(String id) {
		for (DoiBean d : processEngineFacade.getDomainObjectInstances()) {
			if (d.getId().equals(id)) {
				return d;
			}
		}
		return null;
	}

	public DoiBean findDoiBeanByName(String name) {
		for (DoiBean d : processEngineFacade.getDomainObjectInstances()) {
			if (d.getName().equals(name)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Add a line of log into window
	 * 
	 * @param line
	 */
	public void addLog(String line) {
		window.addLog(line);
	}

	public void play() {
		try {
			if (PlayRunner.getDefault().getController() == null) {
				PlayRunner.getDefault().setController(this);
			}
			if (!PlayRunner.getDefault().isRunning()) {
				// PlayRunner.getDefault().stop();
				PlayRunner.getDefault().start();
				setPlayButton(false);
			} else {
				PlayRunner.getDefault().stop();
				setPlayButton(true);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void selectPreviousEntity() {
		if (current == null) {
			window.selectFirstEntityInTable();
			current = getCurrentDoiBean();
			return;
		}
		// search current then select previous
		DoiBean previous = null;
		for (DoiBean d : processEngineFacade.getDomainObjectInstances()) {
			if (d.getId().equals(current.getId())) {
				break;
			}
			previous = d;
		}
		if (previous == null) {
			window.selectFirstEntityInTable();
			current = getCurrentDoiBean();
			return;
		}
		current = previous;
		window.selectEntityOnTable(previous.getId());
		updateInterface(current);
	}

	public void selectNextEntity() {
		if (current == null) {
			window.selectFirstEntityInTable();
			current = getCurrentDoiBean();
			return;
		}
		// search current then select next
		DoiBean next = null;
		for (int i = 0; i < processEngineFacade.getDomainObjectInstances()
				.size(); i++) {
			DoiBean d = processEngineFacade.getDomainObjectInstances().get(i);
			if (d.getId().equals(current.getId())) {
				if (i + 1 < processEngineFacade.getDomainObjectInstances()
						.size()) {
					next = processEngineFacade.getDomainObjectInstances().get(
							i + 1);
					break;
				}
			}

		}
		if (next != null) {
			current = next;
			updateInterface(current);
			window.selectEntityOnTable(next.getId());
		}

	}

	/**
	 * Return current user name. <br>
	 * From current displayed process, search into {@link DomainObjectInstance}
	 * if is not of user Type, then search using correlation and refinements
	 * 
	 * @return current user
	 */
	public String getCurrentUser() {
		if (getCurrentDoiBean() == null) {
			return null;
		}
		if (getCurrentDoiBean().getModel() != null) {
			DomainObjectInstance doi = processEngineFacade
					.getDomainObjectInstanceForProcess(getCurrentDoiBean()
							.getModel());
			if (doi != null) {
				if (doi.getType().startsWith("User")) {
					return doi.getId();
				} else {
					// search into correlated
					List<DomainObjectInstance> corr = processEngineFacade
							.getCorrelations(doi);
					for (DomainObjectInstance c : corr) {
						if (c != null) {
							if (c.getType().startsWith("User")) {
								return c.getId();
							}
						}
					}
				}
			}
		}
		return current.getName();
	}

	/**
	 * @param proc
	 *            - {@link ProcessDiagram} to analyze
	 * @param type
	 *            - type of domainObjectInstance to find
	 * @return correlated instance type from a process. Note: all process in
	 *         demonstrator are related to at least one type (user,flexibus,
	 *         etc..). Return empty string if error or not found
	 */
	public String getTypeForProcess(ProcessDiagram proc, String type) {
		if (proc == null) {
			logger.warn("Impossible to find user for null process");
			return "";
		}
		DomainObjectInstance doi = processEngineFacade
				.getDomainObjectInstanceForProcess(proc);
		if (doi == null) {
			return "";
		}
		if (doi.getType().startsWith(type)) {
			return doi.getId();
		} else {
			// search into correlated
			List<DomainObjectInstance> corr = processEngineFacade
					.getCorrelations(doi);
			for (DomainObjectInstance c : corr) {
				if (c != null) {
					if (c.getType().startsWith(type)) {
						return c.getId();
					}
				}
			}

		}
		return "";
	}

	public void setPlayButton(boolean b) {
		if (b) {
			window.setPlayButton();
		} else {
			window.setPauseButton();
		}
	}

	public void addExternalEvent(ExternalEvent event) {
		this.externalEvents.add(event);
	}

	/**
	 * Find right handler for external events and call collective adaptation
	 */
	/*
	 * public void processExternalEvents() { Iterator<ExternalEvent> iter =
	 * externalEvents.iterator(); while (iter.hasNext()) { ExternalEvent event =
	 * iter.next(); HandlerInstance handler = findHandler(event.getEvent()); if
	 * (handler != null) { TriggerEventType trigger = handler.getHandler()
	 * .getTriggerEvent(); sendCollectiveAdaptationProblem(trigger.getName(),
	 * event.getEnsemble()); iter.remove(); } } }
	 */
	private HandlerInstance findHandler(String event) {
		for (DomainObjectDefinition def : processEngineFacade
				.getDomainObjectDefinitions()) {
			if (def.getProcess() != null) {
				for (ActivityType act : def.getProcess().getActivity()) {
					if (act != null && act instanceof ScopeType) {
						ScopeType scope = (ScopeType) act;
						if (scope.getEventHandler() != null) {
							for (EventHandlerType handler : scope
									.getEventHandler()) {
								if (handler != null
										&& handler.getOnExternalEvent() != null) {
									if (handler.getOnExternalEvent()
											.getOnEventName().equals(event)) {
										return new HandlerInstance(handler);
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void selectMonitorTab(String name, Integer index) {
		monitorSelectedMap.put(name, index);
	}

	public void enableStep(boolean b) {
		window.enableStep(b);
	}

}
