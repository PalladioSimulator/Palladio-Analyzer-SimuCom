package de.uka.ipd.sdq.simucomframework.ui;

import java.util.List;

import de.uka.ipd.sdq.errorhandling.core.SeverityAndIssue;
import de.uka.ipd.sdq.errorhandling.dialogs.issues.DisplayIssuesDialog;
import de.uka.ipd.sdq.simucomframework.AbstractMain;

public abstract class AbstractMainUi extends AbstractMain {

    @Override
    protected void handleModelIssues(List<SeverityAndIssue> issues) {
        final DisplayIssuesDialog runner = new DisplayIssuesDialog(issues);
        DisplayIssuesDialog.showDialogSync(runner);
    }

}
