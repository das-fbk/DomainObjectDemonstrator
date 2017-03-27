package eu.domainobjects.presentation.main;

import java.awt.LayoutManager; 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Window.Type;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import eu.allowensembles.presentation.main.map.viewer.MapViewerComponentBuilder;
//import eu.allowensembles.privacyandsecurity.presentation.PSView;
//import eu.allowensembles.robustness.controller.RobustnessController;
//import eu.allowensembles.utility.presentation.UtilityView;
import eu.domainobjects.DemonstratorConstant;
import eu.domainobjects.controller.MainController;
import eu.domainobjects.presentation.main.action.SelectedEntitiesButtonListener;
import eu.domainobjects.presentation.main.action.listener.CorrelateEntitiesListener;
import eu.domainobjects.presentation.main.action.listener.EntityDetailActionListener;
import eu.domainobjects.presentation.main.action.listener.EntityTableSelectionListener;
import eu.domainobjects.presentation.main.action.listener.MenuExitListener;
import eu.domainobjects.presentation.main.action.listener.OpenScenarioListener;
import eu.domainobjects.presentation.main.action.listener.SelectInstanceListener;
import eu.domainobjects.presentation.main.action.listener.SelectedComboEntityListener;
import eu.domainobjects.presentation.main.action.listener.StepButtonActionListener;
import eu.domainobjects.presentation.main.process.ProcessModelPanel;
import eu.domainobjects.utils.DoiBean;
import eu.domainobjects.utils.UserData;
import eu.fbk.das.process.engine.api.domain.ProcessDiagram;

public class MainWindow {

	private static final Logger logger = LogManager.getLogger(MainWindow.class);

	// main frame
	public JFrame frame;

	// other windows
	private JFrame newEntityFrame;
	private AddNewEntityWindow addNewEntityWindow;

	// main window components
	private JTable generalTable;
	private JScrollPane entitiesScrollPane;
	private JPanel mainPanel;
	private Label label_1;
	private Label label_2;
	private JList<String> providedFragmentsList;
	private JScrollPane mainScrollPane;
	private Label label_3;
	private JList<String> cellInstancesList;
	private ProcessModelPanel processModelPanel;
	private JLabel lblNewLabel;
	private JLabel lblProcessExecution;
	private ProcessModelPanel processExecutionPanel;
	private JLabel lblCorrelatedEntities;
	private JList<String> correlatedEntitiesList;
	private JMenu mnHelp;
	private JMenuItem mntmAbout;

	// window builder
	// private MapViewerComponentBuilder mvcb = new MapViewerComponentBuilder();

	private JScrollPane modelScrollPane;

	private MainController controller;

	private JToolBar toolbar;

	private JButton btnNextEntity;

	private JButton btnPreviousEntity;

	private JButton btnStep;

	private JButton btnPlaypause;

	private JList<String> entityDetailsList;

	private ActivityWindow refinementView;

	private JFrame utilityFrame;
	private JFrame refinementFrame;

	private JFrame PSFrame;
	private JScrollPane entityDetailScrollPane;

	private JList<String> entityKnowledgeList;

	private JScrollPane entityKnowledgeScrollPane;

	private JComboBox<String> comboEntities;

	private JLabel lblComboEntities;

	private SelectJourneyAlternativeWindow selectAlternativeWindow;

	private JFrame selectAlternativeFrame;

	private JTextArea logTextArea;

	private AboutDialog abtDialog;

	private JMenu mnEdit;

	private PreferencesDialog preferencesDialog;

	private JPanel storyboardOnePanel;

	private JList<String> monitorList;

	private Icon playIcon;

	private ImageIcon pauseIcon;

	private Vector<String> columnNames;

	private JTabbedPane tabEntity;

	/**
	 * Create the application.
	 */
	public MainWindow() {
		try {
			initialize();
		} catch (IOException e) {
			logger.error("Error in initialization", e);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		frame = new JFrame("Domain Objects Demonstrator");
		// LayoutManager manager = null;
		// frame.setLayout(manager);
		mainPanel = new JPanel();
		mainPanel.setVisible(true);
		mainPanel.setLayout(null);
		// mettere solo preferredSize per far comparire le barre di scorrimento
		// verticali
		mainPanel.setPreferredSize(new Dimension(1024, 1000));

		storyboardOnePanel = new JPanel();
		storyboardOnePanel.setLayout(null);
		// original size
		// storyboardOnePanel.setBounds(0, 5, 1024, 800);
		// new size
		storyboardOnePanel.setBounds(0, 8, 1050, 800);
		// storyboardOnePanel.setBorder(new LineBorder(Color.red, 2));
		mainPanel.add(storyboardOnePanel, -1);

		// load play/pause icon
		playIcon = new ImageIcon(
				MainWindow.class.getResource("/images/knob_play_green.png"));
		pauseIcon = new ImageIcon(
				MainWindow.class.getResource("/images/knob_pause_green.png"));

		// init column names for general table
		columnNames = new Vector<String>();
		columnNames.add("Id");
		columnNames.add("Name");
		columnNames.add("Status");

		btnStep = new JButton("Step");
		btnStep.setActionCommand(DemonstratorConstant.STEP);
		btnStep.addActionListener(new StepButtonActionListener());
		btnStep.setIcon(new ImageIcon(MainWindow.class
				.getResource("/images/knob_walk.png")));

		btnPlaypause = new JButton("Play");
		// btnPlaypause.setPreferredSize(new Dimension(153, 23));
		btnPlaypause.setIcon(playIcon);
		btnPlaypause.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				controller.play();
			}

		});

		label_1 = new Label("Domain Object details");
		label_1.setBounds(10, 464, 160, 22);
		storyboardOnePanel.add(label_1);

		// entityDetails
		tabEntity = new JTabbedPane(JTabbedPane.TOP);
		tabEntity.setBounds(10, 492, 230, 119);
		storyboardOnePanel.add(tabEntity);

		entityDetailsList = new JList<String>();
		entityDetailsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityDetailsList.setBounds(10, 892, 223, 119);

		entityKnowledgeList = new JList<String>();
		entityKnowledgeList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityKnowledgeList.setBounds(10, 892, 223, 119);

		entityDetailScrollPane = new JScrollPane(entityDetailsList);
		tabEntity.addTab("Details", null, entityDetailScrollPane);
		entityKnowledgeScrollPane = new JScrollPane(entityKnowledgeList);
		tabEntity.addTab("Context", null, entityKnowledgeScrollPane);

		monitorList = new JList<String>();
		monitorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		monitorList.setBounds(10, 892, 223, 119);
		DefaultListModel<String> testModelMonitorList = new DefaultListModel<String>();
		monitorList.setModel(testModelMonitorList);
		monitorList.setCellRenderer(new MonitorCellListRenderer(this));

		// provided fragments
		label_2 = new Label("Provided fragments");
		label_2.setBounds(871, 25, 200, 22);
		storyboardOnePanel.add(label_2);

		providedFragmentsList = new JList<String>();
		providedFragmentsList.setBounds(843, 47, 170, 200);
		storyboardOnePanel.add(providedFragmentsList);

		// combo for entities selection
		lblComboEntities = new JLabel("Domain Objects models");
		lblComboEntities.setBounds(550, 0, 200, 23);
		storyboardOnePanel.add(lblComboEntities);

		comboEntities = new JComboBox<String>();
		comboEntities.setBounds(740, 0, 270, 23);
		comboEntities.addActionListener(new SelectedComboEntityListener());
		storyboardOnePanel.add(comboEntities);

		label_3 = new Label("Domain Object instances");
		label_3.setBounds(10, 19, 175, 22);
		storyboardOnePanel.add(label_3);

		cellInstancesList = new JList();
		cellInstancesList.setBounds(10, 47, 223, 200);
		cellInstancesList.addListSelectionListener(new SelectInstanceListener(
				this));
		cellInstancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		storyboardOnePanel.add(cellInstancesList);

		JLabel lblLog = new JLabel("Log");
		lblLog.setBounds(10, 800, 223, 22);
		mainPanel.add(lblLog);

		// log inside a scrollpane
		logTextArea = new JTextArea("");
		logTextArea.setBounds(10, 810, 982, 90);
		logTextArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane logScrollPane = new JScrollPane(logTextArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		logScrollPane.setBounds(10, 850, 982, 90);
		mainPanel.add(logScrollPane);

		lblNewLabel = new JLabel("Process model");
		lblNewLabel.setBounds(538, 19, 200, 22);
		storyboardOnePanel.add(lblNewLabel);

		lblProcessExecution = new JLabel("Process execution");
		lblProcessExecution.setBounds(538, 265, 161, 22);
		storyboardOnePanel.add(lblProcessExecution);

		// aggiungere lo scrollPane per far comparire le barre verticali
		mainScrollPane = new JScrollPane(mainPanel);

		lblCorrelatedEntities = new JLabel("Correlated Domain Objects");
		lblCorrelatedEntities.setBounds(10, 265, 220, 22);
		storyboardOnePanel.add(lblCorrelatedEntities);

		correlatedEntitiesList = new JList<String>();
		correlatedEntitiesList.setBounds(10, 293, 223, 159);
		correlatedEntitiesList
				.addMouseListener(new CorrelateEntitiesListener());
		JScrollPane correlatedScroll = new JScrollPane(correlatedEntitiesList);
		correlatedScroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		correlatedScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		correlatedScroll.setBounds(10, 293, 223, 159);
		correlatedScroll.setBorder(null);
		storyboardOnePanel.add(correlatedScroll);

		mainScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		mainScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mainScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		frame.getContentPane().add(mainScrollPane);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// change here for final frame size
		// frame.setSize(1024, 768);
		// mine
		frame.setSize(1100, 768);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null); // *** this will center your app
		frame.setIconImage(ImageIO.read(getClass().getResource(
				"/images/1435065408_gear.png")));

		// Entity preference window
		newEntityFrame = new JFrame("Domain Objects Demonstrator");
		newEntityFrame.setType(Type.UTILITY);
		newEntityFrame.setVisible(false);
		newEntityFrame.setResizable(false);
		newEntityFrame.setSize(530, 600);
		addNewEntityWindow = new AddNewEntityWindow(this);
		newEntityFrame.setContentPane(addNewEntityWindow);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnScenario = new JMenu("File");
		menuBar.add(mnScenario);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new OpenScenarioListener());
		mnScenario.add(mntmOpen);

		JMenuItem mnExit = new JMenuItem("Exit");
		mnExit.addActionListener(new MenuExitListener());

		mnScenario.add(new JSeparator());
		JMenuItem scenario1 = new JMenuItem("Open scenario");
		scenario1
				.setActionCommand(DemonstratorConstant.SCENARIO_CELL_SPECIALIZATION);
		scenario1.addActionListener(new OpenScenarioListener());
		mnScenario.add(scenario1);

		// JMenuItem scenario2 = new JMenuItem(
		// "Open collective adaptation 1 scenario");
		// scenario2
		// .setActionCommand(DemonstratorConstant.SCENARIO_COLLECTIVE_ADAPTATION_1);
		// scenario2.addActionListener(new OpenScenarioListener());
		// mnScenario.add(scenario2);

		mnScenario.add(new JSeparator());
		mnScenario.add(mnExit);

		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		JMenuItem mnPreferences = new JMenuItem("Preferences");
		mnEdit.add(mnPreferences);

		preferencesDialog = new PreferencesDialog();
		preferencesDialog.setVisible(false);

		mnPreferences.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				preferencesDialog.setVisible(true);
			}
		});

		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		abtDialog = new AboutDialog();
		abtDialog.setVisible(false);

		mntmAbout = new JMenuItem("About");
		mntmAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				abtDialog.setVisible(true);
			}
		});
		mnHelp.add(mntmAbout);

		// add toolbar
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(false);
		toolbar.setEnabled(false);
		toolbar.setBorder(LineBorder.createGrayLineBorder());
		toolbar.setPreferredSize(new Dimension(100, 40));
		toolbar.add(btnPlaypause);
		toolbar.add(btnStep);
		toolbar.addSeparator();

		// create move next/previous entities
		btnPreviousEntity = new JButton("Previous");
		btnPreviousEntity.setIcon(new ImageIcon(MainWindow.class
				.getResource("/images/knob_left.png")));
		btnPreviousEntity
				.setActionCommand(SelectedEntitiesButtonListener.PREVIOUS);
		btnPreviousEntity.addActionListener(new SelectedEntitiesButtonListener(
				this));
		toolbar.add(btnPreviousEntity);

		btnNextEntity = new JButton("Next");
		btnNextEntity.setIcon(new ImageIcon(MainWindow.class
				.getResource("/images/knob_forward.png")));
		btnNextEntity.setActionCommand(SelectedEntitiesButtonListener.NEXT);
		btnNextEntity
				.addActionListener(new SelectedEntitiesButtonListener(this));

		toolbar.add(btnNextEntity);
		toolbar.addSeparator();

		// add toolbar
		frame.getContentPane().add(toolbar, BorderLayout.NORTH);

		// add and hide activity view inside another frame
		try {
			refinementFrame = new JFrame("Activity Details");
			refinementFrame.setType(Type.UTILITY);
			refinementView = new ActivityWindow(refinementFrame);
			// refinementView.setVisible(true);
			refinementFrame.setSize(867, 700);
			refinementFrame.setContentPane(refinementView);
			showRefinementFrame(false);

			// frame.getContentPane().add(utilityView);
		} catch (JAXBException | URISyntaxException e2) {
			logger.error(e2.getMessage(), e2);
		}

		//
		try {
			selectAlternativeFrame = new JFrame(
					"Allow Ensembles - select alternative");
			selectAlternativeFrame.setType(Type.UTILITY);
			selectAlternativeFrame.setSize(675, 435);
			selectAlternativeWindow = new SelectJourneyAlternativeWindow(this,
					selectAlternativeFrame);
			selectAlternativeFrame.setContentPane(selectAlternativeWindow);
			selectAlternativeFrame.setVisible(false);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// last init part: hide mainScrollPane
		showMainScrollPane(false);
		showToolbar(false);
	}

	public void addButtonToTab(final JTabbedPane tabEntity) {
		JButton button = new JButton("...");
		button.addMouseListener(new EntityDetailActionListener(this));
		tabEntity.setTabComponentAt(tabEntity.getTabCount() - 1, button);

	}

	public void showMainScrollPane(boolean value) {
		mainScrollPane.setVisible(value);
		showToolbar(true);
		refreshWindow();
	}

	private void showToolbar(boolean visible) {
		toolbar.setVisible(visible);
		btnPlaypause.setVisible(visible);
		btnStep.setVisible(visible);
		btnPreviousEntity.setVisible(visible);
		btnNextEntity.setVisible(visible);
	}

	public void loadDomainObjectInstancesTable(
			List<DoiBean> domainObjectInstances) {
		Vector<Vector<String>> data = convertAndFilterForJtable(domainObjectInstances);
		generalTable = new JTable(data, columnNames) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false; // Disallow the editing of any cell
			}
		};

		generalTable.getColumnModel().getColumn(0).setResizable(false);
		generalTable.getColumnModel().getColumn(1).setResizable(false);
		generalTable.getColumnModel().getColumn(2).setResizable(false);

		generalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		generalTable.setBounds(10, 10, 200, 223);
		generalTable.setFillsViewportHeight(true);
		generalTable.getSelectionModel().addListSelectionListener(
				new EntityTableSelectionListener(generalTable));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 11, 223, 321);
		// mainPanel.add(tabbedPane);

		entitiesScrollPane = new JScrollPane(generalTable);
		tabbedPane.addTab("Domain Object instances", null, entitiesScrollPane, null);
		tabbedPane.setEnabledAt(0, true);

		refreshWindow();
	}

	public void resetDomainObjectInstanceTable() {
		if (generalTable != null) {
			generalTable.setModel(new DefaultTableModel());
		}
	}

	/**
	 * Convert input and filter only with mappable domainObjectInstances
	 * (mappable means with lat and lon)
	 * 
	 * @param domainObjectInstances
	 * @return
	 */
	private Vector<Vector<String>> convertAndFilterForJtable(
			List<DoiBean> domainObjectInstances) {
		Vector<Vector<String>> response = new Vector<Vector<String>>();
		for (DoiBean d : domainObjectInstances) {
			if (d.getLat() != null && d.getLon() != null) {
				Vector<String> v = new Vector<String>();
				v.add(d.getId());
				v.add(d.getName());
				v.add(d.getStatus());
				response.add(v);
			}
		}
		return response;
	}

	public void displayProcess(ProcessDiagram p, boolean model,
			boolean execution) {
		if (p == null) {
			logger.warn("ProcessDiagram must be not null");
			return;
		}
		// create processExecutionPanel or processModelPanel on need
		if (processModelPanel == null) {
			processModelPanel = new ProcessModelPanel(
					controller.getProcessEngineFacade());

			processModelPanel.setLayout(new GridBagLayout());
			JScrollPane modelScrollPane = new JScrollPane(processModelPanel);
			modelScrollPane.setBounds(247, 47, 586, 200);
			modelScrollPane.setBorder(new LineBorder(new Color(0, 0, 0)));

			storyboardOnePanel.add(modelScrollPane);
		}
		if (processExecutionPanel == null) {
			// update process execution
			processExecutionPanel = new ProcessModelPanel(
					controller.getProcessEngineFacade());
			processExecutionPanel.init(this);
			processExecutionPanel.setLayout(new FlowLayout());
			modelScrollPane = new JScrollPane(processExecutionPanel);
			modelScrollPane.setBounds(247, 293, 745, 464);
			modelScrollPane.setBorder(new LineBorder(new Color(0, 0, 0)));

			storyboardOnePanel.add(modelScrollPane);
		}
		// display them
		if (model) {
			processModelPanel.updateProcess(p);
		}
		if (execution) {
			processExecutionPanel.updateProcess(p);
		}
		// then refresh window
		refreshWindow();
	}

	public void refreshWindow() {
		frame.getContentPane().validate();
		frame.getContentPane().repaint();
	}

	public void setController(MainController controller) {
		this.controller = controller;
		addNewEntityWindow.setController(controller);
	}

	public void updateDomainObjectInstancesTable(
			List<DoiBean> domainObjectInstances) {
		Vector<Vector<String>> data = convertAndFilterForJtable(domainObjectInstances);

		generalTable.setModel(new DefaultTableModel(data, columnNames));
		generalTable.getColumnModel().getColumn(0).setResizable(false);
		generalTable.getColumnModel().getColumn(1).setResizable(false);
		generalTable.getColumnModel().getColumn(2).setResizable(false);

		refreshWindow();
	}

	public void selectFirstEntityInTable() {
		if (generalTable.getModel().getValueAt(0, 0) != null) {
			generalTable.setRowSelectionInterval(0, 0);
		}
	}

	public String getSelectedEntityInTable() {
		int sr = generalTable.getSelectedRow();
		if (sr == -1 || sr >= generalTable.getModel().getRowCount()) {
			return "";
		}
		return (String) generalTable.getModel().getValueAt(sr, 0);
	}

	public void updateSelectedEntityDetails(List<String> toDisplay) {
		String[] array = new String[toDisplay.size()];
		for (int i = 0; i < toDisplay.size(); i++) {
			array[i] = toDisplay.get(i);
		}
		entityDetailsList.setListData(array);
	}

	public void updateSelectedEntityCorrelations(List<String> pids) {
		String[] array = new String[pids.size()];
		for (int i = 0; i < pids.size(); i++) {
			array[i] = String.valueOf(pids.get(i));
		}
		correlatedEntitiesList.setListData(array);
	}

	public void updateSelectedEntityProvidedFragments(List<String> values) {
		String[] array = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			array[i] = values.get(i);
		}
		providedFragmentsList.setListData(array);
	}

	public void showPSFrame(boolean visible) {
		PSFrame.setVisible(visible);
	}

	public void showRefinementFrame(boolean visible) {
		refinementFrame.setVisible(visible);
	}

	/**
	 * 
	 * @return the model of current process (not the executed one, see
	 *         {@link MainController} method getCurrentProcess for it )
	 */
	public ProcessDiagram getCurrentProcess() {
		if (generalTable.getRowCount() > 0) {
			DoiBean current = controller.getCurrentDoiBean();
			ProcessDiagram process = controller.getProcessEngineFacade()
					.getModel(current.getName());
			return process;
		}
		return null;
	}

	public MainController getController() {
		return controller;
	}

	public void updateCellInstances(List<String> toDisplay) {
		String[] array = new String[toDisplay.size()];
		for (int i = 0; i < toDisplay.size(); i++) {
			array[i] = String.valueOf(toDisplay.get(i));
		}
		cellInstancesList.setListData(array);
	}

	public void updateEntityKnowledge(List<String> toDisplay) {
		String[] array = new String[toDisplay.size()];
		for (int i = 0; i < toDisplay.size(); i++) {
			array[i] = String.valueOf(toDisplay.get(i));
		}
		entityKnowledgeList.setListData(array);
	}

	public ActivityWindow getActivityWindow() {

		return refinementView;
	}

	public void updateComboxEntities(List<String> input) {
		comboEntities.removeAllItems();
		Collections.sort(input);
		input.add(0, "");
		for (String s : input) {
			comboEntities.addItem(s);
		}
	}

	public void resetCellInstances() {
		String[] temp = { "" };
		cellInstancesList.setListData(temp);
	}

	public void resetCorrelatedCells() {
		String[] temp = { "" };
		correlatedEntitiesList.setListData(temp);
	}

	public void resetCellDetails() {
		String[] temp = { "" };
		entityDetailsList.setListData(temp);
	}

	public void resetContextDetails() {
		String[] temp = { "" };
		entityKnowledgeList.setListData(temp);
	}

	public void resetProcessExecution() {
		if (processExecutionPanel != null) {
			processExecutionPanel.clear();
		}
	}

	public void resetProcessModel() {
		if (processModelPanel != null) {
			processModelPanel.clear();
		}
	}

	public void displayAlternativesWindow(UserData ud) {
		// set data and display window
		selectAlternativeWindow.setData(ud);
		selectAlternativeFrame.setVisible(true);
	}

	/**
	 * Add a line at the bottom of the log
	 * 
	 * @param line
	 */
	public void addLog(String line) {
		if (line == null) {
			logger.warn("addLog: line must be not null");
			return;
		}
		logTextArea.append(line + System.lineSeparator());

	}

	public void selectCellInstances(int index) {
		cellInstancesList.setSelectedIndex(index);

	}

	public String getSelectedCorrelatedTable(int rowIndex) {
		return (String) generalTable.getModel().getValueAt(rowIndex, 0);
	}

	public String getSelectedCorrelatedEntity() {
		int sr = correlatedEntitiesList.getSelectedIndex();
		if (sr == -1 || sr >= correlatedEntitiesList.getModel().getSize()) {
			return "";
		}
		return correlatedEntitiesList.getModel().getElementAt(sr);
	}

	public void selectEntityOnTable(String id) {
		for (int i = 0; i < generalTable.getModel().getRowCount(); i++) {
			if (generalTable.getModel().getValueAt(i, 0) != null
					&& generalTable.getModel().getValueAt(i, 0).equals(id)) {
				generalTable.setRowSelectionInterval(i, i);
			}
		}
	}


	public void resetProcessDisplay() {
		processExecutionPanel = null;
		processModelPanel = null;
	}

	public void updateMonitors(List<String> values, Integer index) {
		monitorList.setModel(new DefaultListModel<String>());
		if (values != null && !values.isEmpty()) {
			DefaultListModel<String> model = (DefaultListModel<String>) monitorList
					.getModel();
			for (String v : values) {
				model.addElement(v);
			}
			MonitorCellListRenderer renderer = (MonitorCellListRenderer) monitorList
					.getCellRenderer();
			if (index != null && index >= 0) {
				renderer.setStyle(index);
			} else {
				renderer.resetStyle();
			}
		}
	}

	public void setPauseButton() {
		btnPlaypause.setIcon(pauseIcon);
		btnPlaypause.setText("Pause");
	}

	public void setPlayButton() {
		btnPlaypause.setIcon(playIcon);
		btnPlaypause.setText("Play");
	}

	public void enableStep(boolean b) {
		btnStep.setEnabled(b);
		btnPlaypause.setEnabled(b);
	}
}
