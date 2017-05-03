package eu.domainobjects.presentation.main.action.listener;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eu.domainobjects.presentation.main.MainWindow;
import eu.domainobjects.utils.DoiBean;

public class SelectFragmentListener implements ListSelectionListener {

	private MainWindow window;

	public SelectFragmentListener(MainWindow mainWindow) {
		this.window = mainWindow;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e != null && e.getSource() instanceof JList<?>) {
			JList<?> fsm = (JList<?>) e.getSource();
			for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
				if (fsm.isSelectedIndex(i)) {
					setFragmentWithName(fsm.getSelectedValue());
					break;
				}
			}
		}
	}

	public void setFragmentWithName(Object selectedValue) { // .getProcessEngine().getDefinitionByFragment(v)
		if (selectedValue instanceof String) {
			String v = (String) selectedValue;
			for (DoiBean db : window.getController().getProcessEngineFacade()
					.getDomainObjectInstances()) {
				for (String fragmentName : window.getController()
						.getProcessEngineFacade()
						.getFragmentsFromDoiBean(db.getName())) {
					if (fragmentName.equals(v)) {
						// qui fare solo update process model, per il momento
						// window.getController()
						// .updateProcessModelWithSelectedFragment(db,
						// fragmentName);
						break;
					}
				}

			}
		}
	}
}
