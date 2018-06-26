package org.palladiosimulator.simucom.swtbot.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimuComSwtBotTest {

    private static SWTWorkbenchBot bot;

    @BeforeClass public static void beforeClass() throws Exception {
        SWTBotPreferences.KEYBOARD_LAYOUT="EN_US";
        bot=new SWTWorkbenchBot();
        try {
            bot.viewByTitle("Welcome").close();
        }
        catch (  final Exception e) {
        }
        bot.toolbarButtonWithTooltip("Open Perspective").click();
        bot.table().select("Palladio");
        bot.button("OK").click();
        bot.viewByTitle("Project Explorer").show();
        bot.viewByTitle("Simulation Dock Status").show();
    }

    @Test
    public void testSimuCom() {
        bot.viewByTitle("Project Explorer").show();
        new SWTBotMenu(ContextMenuHelper.contextMenu(bot.tree(), "New", "Project...")).click();
        bot.text().setText("Palladio");
        bot.button("Next >").click();
        bot.textWithLabel("&Project name:").setText("test");
        bot.button("Next >").click();
        bot.table().select("Minimum Example Template");
        bot.button("Finish").click();
        bot.viewByTitle("Project Explorer").show();
        bot.tree().getTreeItem("test").expand();
        bot.menu("Run").menu("Run Configurations...").click();
        bot.tree().getTreeItem("SimuBench").select().doubleClick();
        bot.cTabItem("Architecture Model(s)").activate();
        bot.textInGroup("Allocation File").setText("platform:/resource/test/default.allocation");
        bot.textInGroup("Usage File").setText("platform:/resource/test/default.usagemodel");
        bot.cTabItem("Simulation").activate();
        bot.button("Browse...").click();
        bot.button("Add..").click();
        bot.button("Finish").click();
        bot.table().select(0);
        bot.button("OK").click();
        bot.button("Run").click();
        bot.waitUntil(new WaitForJobs("SimuCom"),5*60*1000);
    }

}
