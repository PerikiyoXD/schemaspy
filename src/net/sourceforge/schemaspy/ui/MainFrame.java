// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane;
	private JPanel dbConfigPanel;
	private JPanel buttonBar;
	private JButton launchButton;
	private JPanel header;

	public MainFrame()
	{
		this.jContentPane = null;
		this.dbConfigPanel = null;
		this.buttonBar = null;
		this.launchButton = null;
		this.initialize();
	}

	private void initialize()
	{
		this.setContentPane(this.getJContentPane());
		this.setTitle("SchemaSpy");
		this.setSize(new Dimension(500, 312));
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent windowEvent)
			{
				System.exit(0);
			}
		});
	}

	private JPanel getDbConfigPanel()
	{
		if (this.dbConfigPanel == null)
		{
			this.dbConfigPanel = new DbConfigPanel();
		}
		return this.dbConfigPanel;
	}

	private JPanel getJContentPane()
	{
		if (this.jContentPane == null)
		{
			(this.jContentPane = new JPanel()).setLayout(new GridBagLayout());
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = -1;
			constraints.weightx = 1.0;
			constraints.anchor = 10;
			constraints.insets = new Insets(4, 0, 4, 0);
			this.jContentPane.add(this.getHeaderPanel(), constraints);
			constraints.insets = new Insets(0, 0, 0, 0);
			constraints.fill = 1;
			constraints.anchor = 18;
			constraints.weighty = 1.0;
			this.jContentPane.add(this.getDbConfigPanel(), constraints);
			constraints.anchor = 14;
			constraints.fill = 0;
			constraints.weighty = 0.0;
			this.jContentPane.add(this.getButtonBar(), constraints);
		}
		return this.jContentPane;
	}

	private JPanel getButtonBar()
	{
		if (this.buttonBar == null)
		{
			(this.buttonBar = new JPanel()).setLayout(new FlowLayout(4));
			this.buttonBar.add(this.getLaunchButton(), null);
		}
		return this.buttonBar;
	}

	private JPanel getHeaderPanel()
	{
		if (this.header == null)
		{
			(this.header = new JPanel()).setLayout(new GridBagLayout());
			final GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			this.header.add(new JLabel("SchemaSpy - Graphical Database Metadata Browser"), gridBagConstraints);
			gridBagConstraints.gridx = 0;
			final GridBagConstraints gridBagConstraints2 = gridBagConstraints;
			++gridBagConstraints2.gridy;
			this.header.add(new JLabel("Select a database type and fill in the required fields"), gridBagConstraints);
		}
		return this.header;
	}

	private JButton getLaunchButton()
	{
		if (this.launchButton == null)
		{
			(this.launchButton = new JButton()).setText("Launch");
		}
		return this.launchButton;
	}
}
