/*
 * Copyright (C) 2026 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 *
 * MechWarrior Copyright Microsoft Corporation. MegaMek was created under
 * Microsoft's "Game Content Usage Rules"
 * <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
 * affiliated with Microsoft.
 */
package megamek.client.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.Serial;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import megamek.MMConstants;
import megamek.client.ui.buttons.MMButton;
import megamek.client.ui.clientGUI.GUIPreferences;
import megamek.client.ui.dialogs.buttonDialogs.AbstractButtonDialog;
import megamek.client.ui.util.UIUtil;
import megamek.logging.MMLogger;

/**
 * Displays licensing, legal, and welcome information about the MegaMek Suite on
 * application startup. The user must click Acknowledge to proceed.
 *
 * <p>Includes a "don't show again" checkbox that takes effect when the user
 * acknowledges. The X button and Escape key are disabled.</p>
 *
 * <p>Subclasses in MegaMekLab and MekHQ can override {@link #buildHtmlContent()}
 * to customize the displayed text.</p>
 */
public class LicensingDialog extends AbstractButtonDialog {

    private static final MMLogger logger = MMLogger.create(LicensingDialog.class);

    @Serial
    private static final long serialVersionUID = 7924310587915442671L;

    private JCheckBox chkDontShowAgain;

    /**
     * Creates a new licensing dialog with the default MegaMek title.
     *
     * @param frame the parent frame
     */
    public LicensingDialog(JFrame frame) {
        super(frame, "LicensingDialog", "LicensingDialog.title");
        setTitle(getTitle() + " " + MMConstants.VERSION);
        initialize();
        preventDismissal();
    }

    /**
     * Creates a new licensing dialog with a custom title. Used by MegaMekLab
     * and MekHQ to show their own product name and version.
     *
     * @param frame       the parent frame
     * @param customTitle the full title string (e.g. "Welcome to MekHQ 0.50.13")
     */
    protected LicensingDialog(JFrame frame, String customTitle) {
        super(frame, "LicensingDialog", "LicensingDialog.title");
        setTitle(customTitle);
        initialize();
        preventDismissal();
    }

    private void preventDismissal() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "none");
        getRootPane().getInputMap(JComponent.WHEN_FOCUSED).put(escape, "none");
    }

    @Override
    public void windowClosing(WindowEvent evt) {
        // Do nothing - user must click Acknowledge
    }

    @Override
    protected Container createCenterPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.setName("licensingContent");
        editorPane.setText(buildHtmlContent());
        editorPane.setCaretPosition(0);
        editorPane.addHyperlinkListener(this::handleHyperlink);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setName("licensingScrollPane");
        scrollPane.setPreferredSize(UIUtil.scaleForGUI(550, 400));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        chkDontShowAgain = new JCheckBox(resources.getString("chkIgnore.text"));
        chkDontShowAgain.setToolTipText(resources.getString("chkIgnore.toolTipText"));
        chkDontShowAgain.setName("chkDontShowAgain");

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.add(chkDontShowAgain);

        int pad = UIUtil.scaleForGUI(10);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("licensingPanel");
        panel.setBorder(new EmptyBorder(pad, pad, UIUtil.scaleForGUI(5), pad));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(checkboxPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds the HTML content for the licensing dialog. Subclasses can override
     * this method to customize the displayed text.
     *
     * @return the HTML string to display
     */
    protected String buildHtmlContent() {
        int width = UIUtil.scaleForGUI(500);
        String wikiUrl = resources.getString("LicensingDialog.wikiUrl");
        String gameContentRulesUrl = resources.getString("LicensingDialog.gameContentRulesUrl");
        String gameContentRulesText = resources.getString("LicensingDialog.gameContentRulesText");
        String discordUrl = resources.getString("LicensingDialog.discordUrl");
        String discordText = resources.getString("LicensingDialog.discordText");

        return "<html><body width='" + width + "'>"
              + "<p><b>" + getTitle() + "</b></p>"
              + "<p>" + resources.getString("LicensingDialog.disclaimer") + "</p>"
              + "<p>" + resources.getString("LicensingDialog.licensing")
              + " <a href=\"" + gameContentRulesUrl + "\">" + gameContentRulesText + "</a>.</p>"
              + "<p>" + resources.getString("LicensingDialog.wiki")
              + " <a href=\"" + wikiUrl + "\">" + wikiUrl + "</a></p>"
              + "<p>" + resources.getString("LicensingDialog.community")
              + " <a href=\"" + discordUrl + "\">" + discordText + "</a>.</p>"
              + "<p><small><i>" + resources.getString("LicensingDialog.trademark")
              + "</i></small></p>"
              + "<p>" + resources.getString("LicensingDialog.acknowledgment") + "</p>"
              + "</body></html>";
    }

    @Override
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(new MMButton("acknowledgeButton",
              resources.getString("LicensingDialog.acknowledge"),
              this::okButtonActionPerformed));
        return panel;
    }

    @Override
    protected void okAction() {
        if ((chkDontShowAgain != null) && chkDontShowAgain.isSelected()) {
            GUIPreferences.getInstance().setNagForReadme(false);
        }
    }

    @Override
    protected void cancelAction() {
        // No cancel action - dialog requires acknowledgment
    }

    private void handleHyperlink(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                Desktop.getDesktop().browse(event.getURL().toURI());
            } catch (Exception ex) {
                logger.error(ex, "Failed to open URL: {}", event.getURL());
            }
        }
    }

    /**
     * Shows the licensing dialog if the user has not opted out.
     *
     * @param frame the parent frame
     */
    public static void showIfNeeded(JFrame frame) {
        if (!GUIPreferences.getInstance().getNagForReadme()) {
            return;
        }
        LicensingDialog dialog = new LicensingDialog(frame);
        dialog.showDialog();
    }

    /**
     * Shows the licensing dialog with a custom title if the user has not opted
     * out. Used by MegaMekLab and MekHQ to display their own product name.
     *
     * @param frame       the parent frame, or {@code null} if no frame exists yet
     * @param customTitle the full title string (e.g. "Welcome to MekHQ 0.50.13")
     */
    public static void showIfNeeded(JFrame frame, String customTitle) {
        if (!GUIPreferences.getInstance().getNagForReadme()) {
            return;
        }
        LicensingDialog dialog = new LicensingDialog(frame, customTitle);
        dialog.showDialog();
    }
}
