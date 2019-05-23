package chess.gui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import chess.gui.Table.PlayerType;
import chess.pieces.Alliance;
import chess.player.Player;

@SuppressWarnings("serial")
class GameSetup extends JDialog {

	private PlayerType whitePlayerType;
	private PlayerType blackPlayerType;
	private static JSpinner searchDepthSpinner;

	GameSetup(final JFrame frame, final boolean model) {
		super(frame, model);
		final JPanel myPanel = new JPanel(new GridLayout(0, 1));
		final JRadioButton whiteHumanButton = new JRadioButton("Human");
		final JRadioButton whiteComputerButton = new JRadioButton("AI");
		final JRadioButton blackHumanButton = new JRadioButton("Human");
		final JRadioButton blackComputerButton = new JRadioButton("AI");
		whiteHumanButton.setActionCommand("Human");
		final ButtonGroup whiteGroup = new ButtonGroup();
		whiteGroup.add(whiteHumanButton);
		whiteGroup.add(whiteComputerButton);
		whiteHumanButton.setSelected(true);

		final ButtonGroup blackGroup = new ButtonGroup();
		blackGroup.add(blackHumanButton);
		blackGroup.add(blackComputerButton);
		blackHumanButton.setSelected(true);

		getContentPane().add(myPanel);
		myPanel.add(new JLabel("White"));
		myPanel.add(whiteHumanButton);
		myPanel.add(whiteComputerButton);
		myPanel.add(new JLabel("Black"));
		myPanel.add(blackHumanButton);
		myPanel.add(blackComputerButton);

		searchDepthSpinner = addLabeledSpinner(myPanel, "Difficulty (1 - 5)",
				new SpinnerNumberModel(3, 1, 5, 1));

		final JButton cancelButton = new JButton("Cancel");
		final JButton okButton = new JButton("OK");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				whitePlayerType = whiteComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
				blackPlayerType = blackComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
				GameSetup.this.setVisible(false);
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Cancel");
				GameSetup.this.setVisible(false);
			}
		});

		myPanel.add(cancelButton);
		myPanel.add(okButton);

		setLocationRelativeTo(frame);
		pack();
		setVisible(false);
	}

	void promptUser() {
		setVisible(true);
		repaint();
	}

	boolean isAIPlayer(final Player player) {
		if (player.getAlliance() == Alliance.WHITE) {
			return getWhitePlayerType() == PlayerType.COMPUTER;
		}
		return getBlackPlayerType() == PlayerType.COMPUTER;
	}

	PlayerType getWhitePlayerType() {
		return this.whitePlayerType;
	}

	PlayerType getBlackPlayerType() {
		return this.blackPlayerType;
	}

	private static JSpinner addLabeledSpinner(final Container c, final String label, final SpinnerModel model) {
		final JLabel l = new JLabel(label);
		c.add(l);
		final JSpinner spinner = new JSpinner(model);
		l.setLabelFor(spinner);
		c.add(spinner);
		return spinner;
	}

	static int getSearchDepth() {
		return (Integer) searchDepthSpinner.getValue();
	}
}