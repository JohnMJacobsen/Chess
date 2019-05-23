package chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import com.google.common.primitives.Ints;

import chess.board.Move;
import chess.pieces.Piece;

@SuppressWarnings("serial")
public class TakenPiecesPanel extends JPanel {
	
	private final JPanel northPanel;
	private final JPanel southPanel;
	
	private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);
	private static final Color PANEL_COLOR = Color.decode("#2E8B57");
	private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(40, 80);

	public TakenPiecesPanel() {
		super(new BorderLayout());
		this.setBackground(PANEL_COLOR);
		this.setBorder(PANEL_BORDER);
		this.northPanel = new JPanel(new GridLayout(8, 2));
		this.southPanel = new JPanel(new GridLayout(8, 2));
		this.northPanel.setBackground(PANEL_COLOR);
		this.southPanel.setBackground(PANEL_COLOR);
		add(this.northPanel, BorderLayout.NORTH);
		add(this.southPanel, BorderLayout.SOUTH);
		setPreferredSize(TAKEN_PIECES_DIMENSION);
	}
	
	public void redo(final Table.MoveLog moveLog) {
		southPanel.removeAll();
		northPanel.removeAll();
		
		final List<Piece> whiteTakenPieces = new ArrayList<>();
		final List<Piece> blackTakenPieces = new ArrayList<>();
		
		for (final Move move : moveLog.getMoves()) {
			if (move.isAttack()) {
				final Piece takenPiece = move.getAttackedPiece();
				if (takenPiece.getPieceAlliance().isWhite()) {
					whiteTakenPieces.add(takenPiece);
				}
				else if (takenPiece.getPieceAlliance().isBlack()) {
					blackTakenPieces.add(takenPiece);
				}
				else {
					throw new RuntimeException("Invalid Piece Taken");
				}
			}
		}
		
		Collections.sort(whiteTakenPieces, new Comparator<Piece>() {
			@Override
			public int compare(final Piece piece1, Piece piece2) {
				return Ints.compare(piece1.getPieceValue(), piece2.getPieceValue());
			}
		});
		
		Collections.sort(blackTakenPieces, new Comparator<Piece>() {
			@Override
			public int compare(final Piece piece1, Piece piece2) {
				return Ints.compare(piece1.getPieceValue(), piece2.getPieceValue());
			}
		});
		
		for (final Piece takenPiece : whiteTakenPieces) {
			try {
				final BufferedImage image = ImageIO.read(new File("art/" 
						+ takenPiece.getPieceAlliance().toString().substring(0, 1) 
						+ "" + takenPiece.toString() + ".gif"));
				final ImageIcon icon = new ImageIcon(image);
				final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(icon.getIconWidth() - 15, icon.getIconWidth() - 15, Image.SCALE_SMOOTH)));
				this.southPanel.add(imageLabel);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		for (final Piece takenPiece : blackTakenPieces) {
			try {
				final BufferedImage image = ImageIO.read(new File("art/" 
						+ takenPiece.getPieceAlliance().toString().substring(0, 1) 
						+ "" + takenPiece.toString() + ".gif"));
				final ImageIcon icon = new ImageIcon(image);
				final JLabel imageLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(icon.getIconWidth() - 15, icon.getIconWidth() - 15, Image.SCALE_SMOOTH)));
				this.northPanel.add(imageLabel);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}	
		validate();
	}	
}