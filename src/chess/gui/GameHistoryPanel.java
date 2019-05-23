package chess.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import chess.board.Board;
import chess.board.Move;

@SuppressWarnings("serial")
public class GameHistoryPanel extends JPanel {

	private final DataModel model;
	private final JScrollPane scrollPane;
	private static final Dimension HISTORY_PANEL_DIMENSION = new Dimension(100, 300);
	private static final int DESIRED_ROW_HEIGHT = 20;

	GameHistoryPanel() {
		this.setLayout(new BorderLayout());
		this.model = new DataModel();
		final JTable table = new JTable(model);
		table.setRowHeight(DESIRED_ROW_HEIGHT);
		this.scrollPane = new JScrollPane(table);
		scrollPane.setColumnHeaderView(table.getTableHeader());
		scrollPane.setPreferredSize(HISTORY_PANEL_DIMENSION);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setVisible(true);
	}

	void redo(final Board board, final Table.MoveLog moveHistory) {
		int numberOfCompletedTurns = 0;
		this.model.clear();
		for (final Move move : moveHistory.getMoves()) {
			final String moveText = move.toString();
			if (move.getMovedPiece().getPieceAlliance().isWhite()) {
				this.model.setValueAt(moveText, numberOfCompletedTurns, 0);
			}
			else if (move.getMovedPiece().getPieceAlliance().isBlack()) {
				this.model.setValueAt(moveText, numberOfCompletedTurns, 1);
				numberOfCompletedTurns++;
			}
		}
		if (moveHistory.getMoves().size() > 0) {
			final Move lastMove = moveHistory.getMoves().get(moveHistory.size() - 1);
			final String moveText = lastMove.toString();

			if (lastMove.getMovedPiece().getPieceAlliance().isWhite()) {
				this.model.setValueAt(moveText + calculateCheckAndCheckMateHash(board), numberOfCompletedTurns, 0);
			}
			else if (lastMove.getMovedPiece().getPieceAlliance().isBlack()) {
				this.model.setValueAt(moveText + calculateCheckAndCheckMateHash(board), numberOfCompletedTurns - 1, 1);
			}
		}
		final JScrollBar vertical = scrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	private String calculateCheckAndCheckMateHash(final Board board) {
		if (board.currentPlayer().isInCheck()) {
			return "+";
		}
		else if (board.currentPlayer().isInCheckMate()) {
			return "#";
		}
		else {
			return "";
		}
	}

	private static class DataModel extends DefaultTableModel {

		private final List<Row> values;
		private static final String[] NAMES = {"White", "Black"};

		DataModel() {
			this.values = new ArrayList<>();
		}

		public void clear() {
			this.values.clear();
			setRowCount(0);
		}

		public int getRowCount() {
			if (this.values == null) {
				return 0;
			}
			return this.values.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			final Row currentRow = this.values.get(row);
			if(column == 0) {
				return currentRow.getWhiteMove();
			}
			else if (column == 1) {
				return currentRow.getBlackMove();
			}
			else {
				return null;
			}
		}

		@Override
		public void setValueAt(final Object mnemonic, final int row, final int column) {
			final Row currentRow;
			if (this.values.size() <= row) {
				currentRow = new Row();
				this.values.add(currentRow);
			}
			else {
				currentRow = this.values.get(row);
			}
			if (column == 0) {
				currentRow.setWhiteMove((String) mnemonic);
				fireTableRowsInserted(row, row);
			}
			else if (column == 1) {
				currentRow.setBlackMove((String) mnemonic);
				fireTableCellUpdated(row, column);
			}
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return Move.class;
		}

		@Override
		public String getColumnName(final int column) {
			return NAMES[column];
		}
	}

	private static class Row {

		private String whiteMove;
		private String blackMove;

		Row () {
		}

		public String getWhiteMove() {
			return this.whiteMove;
		}

		public String getBlackMove() {
			return this.blackMove;
		}

		public void setWhiteMove(final String move) {
			this.whiteMove = move;
		}

		public void setBlackMove(final String move) {
			this.blackMove = move;
		}
	}
}