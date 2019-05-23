package chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.google.common.collect.Lists;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Tile;
import chess.pieces.Piece;
import chess.player.MoveTransition;
import chess.player.ai.MiniMax;
//import chess.player.ai.MoveStrategy;

public class Table extends Observable {
	
	private final JFrame gameFrame;
	private final GameHistoryPanel gameHistoryPanel;
	private final TakenPiecesPanel takenPiecesPanel;
	private final BoardPanel boardPanel;
	private final MoveLog moveLog;
	private final GameSetup gameSetup;
	private Board chessBoard;
	private Tile sourceTile;
	private Tile destinationTile;
	private Piece humanMovedPiece;
	private BoardDirection boardDirection;
	private Move computerMove;
	
	private boolean highlightLegalMoves;
	
	private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
	private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
	private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
	private static String defaultPieceImagesPath = "art/";
	
	
	private final Color lightTileColor = Color.decode("#FFFACD");
	private final Color darkTileColor = Color.decode("#593E1A");
	
	private static final Table INSTANCE = new Table();
	
	private Table() {
		this.gameFrame = new JFrame("Java Chess");
		this.gameFrame.setLayout(new BorderLayout());
	    final JMenuBar tableMenuBar = new JMenuBar();
        populateMenuBar(tableMenuBar);
		this.chessBoard = Board.createStandardBoard();
		this.gameHistoryPanel = new GameHistoryPanel();
		this.takenPiecesPanel = new TakenPiecesPanel();
		this.moveLog = new MoveLog();
		this.boardPanel = new BoardPanel();
		this.addObserver(new TableGameAIWatcher());
		this.gameSetup = new GameSetup(this.gameFrame, true);
		this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
		this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
		this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
		this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
		this.boardDirection = BoardDirection.NORMAL;
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.highlightLegalMoves = false;
        center(this.gameFrame);
        this.gameFrame.setVisible(true);
	}
	
	public static Table get() {
		return INSTANCE;
	}
	
	public void show() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Table.get().getMoveLog().clear();
				Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
				Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
				Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
			}
		});
	}
	
	private GameSetup getGameSetup() {
		return this.gameSetup;
	}
	
	private Board getGameBoard() {
		return this.chessBoard;
	}
	
	private MoveLog getMoveLog() {
		return this.moveLog;
	}
	
	private GameHistoryPanel getGameHistoryPanel() {
		return this.gameHistoryPanel;
	}
	
	private TakenPiecesPanel getTakenPiecesPanel() {
		return this.takenPiecesPanel;
	}
	
	private BoardPanel getBoardPanel() {
		return this.boardPanel;
	}
	
//	private JFrame getGameFrame() {
//        return this.gameFrame;
//    }
//    
//    private boolean getHighlightLegalMoves() {
//        return this.highlightLegalMoves;
//    }

	private void populateMenuBar(final JMenuBar tableMenuBar) {
		tableMenuBar.add(createExitButton());
		tableMenuBar.add(createPreferencesMenu());
		tableMenuBar.add(createOptionsMenu());
	}

	private JMenu createExitButton() {
		final JMenu fileMenu = new JMenu("Exit");
		final JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);
		return fileMenu;
	}
	
	private JMenu createPreferencesMenu() {
		final JMenu preferencesMenu = new JMenu("Preferences");
		final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
		flipBoardMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				boardDirection = boardDirection.opposite();
				boardPanel.drawBoard(chessBoard);
			}
		});
		preferencesMenu.add(flipBoardMenuItem);
		
		final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight Legal Moves", false);
		legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
			}
		});
		preferencesMenu.add(legalMoveHighlighterCheckbox);
		return preferencesMenu;
	}
	
	private JMenu createOptionsMenu() {
		final JMenu optionsMenu = new JMenu("Options");
		final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
		setupGameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Table.get().getGameSetup().promptUser();
				Table.get().setupUpdate(Table.get().getGameSetup());
			}
		});
		optionsMenu.add(setupGameMenuItem);
		return optionsMenu;
	}
	
	private void setupUpdate(final GameSetup gameSetup) {
		setChanged();
		notifyObservers(gameSetup);
	}
	
	private void moveMadeUpdate(final PlayerType playerType) {
		setChanged();
		notifyObservers(playerType);
	}
	
	public void updateGameBoard(final Board board) {
		this.chessBoard = board;
	}
	
	public void updateComputerMove(final Move move) {
		this.computerMove = move;
	}
	
	private static void center(final JFrame frame) {
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (dim.width - frame.getSize().width) / 2;
        final int y = (dim.height - frame.getSize().height) / 2;
        frame.setLocation(x, y);
    }
	
	private static class TableGameAIWatcher implements Observer {

		@Override
		public void update(final Observable o, final Object arg) {
			if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) && 
				!Table.get().getGameBoard().currentPlayer().isInCheckMate() && 
				!Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
				final AIThinkTank thinkTank = new AIThinkTank();
				thinkTank.execute();
			}
			if (Table.get().getGameBoard().currentPlayer().isInCheckMate()) {
				System.out.println("Game Over: " + Table.get().getGameBoard().currentPlayer() + " is in checkmate");
			}
			if (Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
				System.out.println("Game Over: " + Table.get().getGameBoard().currentPlayer() + " is in stalemate");
			}
		}
	}
	
	private static class AIThinkTank extends SwingWorker<Move, String> {
		
		private AIThinkTank() {
		}
		
		@Override
		protected Move doInBackground() throws Exception {
			final MiniMax miniMax  = new MiniMax(GameSetup.getSearchDepth());
			final Move bestMove = miniMax.execute(Table.get().getGameBoard());
			return bestMove;
		}
		
		@Override
		public void done() {
			try {
				final Move bestMove = get();
				Table.get().updateComputerMove(bestMove);
				Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
				Table.get().getMoveLog().addMove(bestMove);
				Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
				Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
				Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
				Table.get().moveMadeUpdate(PlayerType.COMPUTER);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	public enum BoardDirection {
		NORMAL {
			List<TilePanel> traverse(final List<TilePanel> boardTiles) {
				return boardTiles;
			}
			
			BoardDirection opposite() {
				return FLIPPED;
			}
		},
		FLIPPED {
			List<TilePanel> traverse(final List<TilePanel> boardTiles) {
				return Lists.reverse(boardTiles);
			}
			
			BoardDirection opposite() {
				return NORMAL;
			}
		};
		
		abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
		abstract BoardDirection opposite();
	}
	
	@SuppressWarnings("serial")
	private class BoardPanel extends JPanel {
		final List<TilePanel> boardTiles;
		
		BoardPanel() {
			super(new GridLayout(8, 8));
			this.boardTiles = new ArrayList<>();
			for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
				final TilePanel tilePanel = new TilePanel(this, i);
				this.boardTiles.add(tilePanel);
				add(tilePanel);
			}
			setPreferredSize(BOARD_PANEL_DIMENSION);
			validate();
		}
		
		public void drawBoard(final Board board) {
			removeAll();
			for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
				tilePanel.drawTilePanel(board);
				add(tilePanel);
			}
			validate();
			repaint();
		}
		
	}
	
	public static class MoveLog {
		private final List<Move> moves;
		
		MoveLog() {
			this.moves = new ArrayList<>();
		}
		
		public List<Move> getMoves() {
			return this.moves;
		}
		
		public void addMove(final Move move) {
			this.moves.add(move);
		}
		
		public int size() {
			return this.moves.size();
		}
		
		public void clear() {
			this.moves.clear();
		}
		
		public Move removeMove(int index) {
			return this.moves.remove(index);
		}
		
		public boolean removeMove(final Move move) {
			return this.moves.remove(move);
		}
	}
	
	enum PlayerType {
		HUMAN, 
		COMPUTER
	}
	
	@SuppressWarnings("serial")
	private class TilePanel extends JPanel {
		private final int tileId;
		
		TilePanel(final BoardPanel boardPanel, final int tileId){
			super(new GridBagLayout());
			this.tileId = tileId;
			setPreferredSize(TILE_PANEL_DIMENSION);
			assignTileColor();
			assignTilePieceIcon(chessBoard);
			highlightLegals(chessBoard);
			
			
			addMouseListener(new MouseListener() {
				public void mouseClicked(final MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        sourceTile = null;
                        destinationTile = null;
                        humanMovedPiece = null;
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (sourceTile == null) {
                            sourceTile = chessBoard.getTile(tileId);
                            humanMovedPiece = sourceTile.getPiece();
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            } else if (humanMovedPiece.getPieceAlliance() != chessBoard.currentPlayer().getAlliance()){
                                humanMovedPiece = null;
                                sourceTile = null;
                            }
                        } else {
                            destinationTile = chessBoard.getTile(tileId);
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(),
                                    destinationTile.getTileCoordinate());
                            if (destinationTile.isTileOccupied() && sourceTile.getPiece().getPieceAlliance() ==
                                    destinationTile.getPiece().getPieceAlliance()) {
                                sourceTile = chessBoard.getTile(tileId);
                                humanMovedPiece = sourceTile.getPiece();
                            } else if ((!destinationTile.isTileOccupied() || move.isAttack()) && move.getMovedPiece() != null) {
                                final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                                if (transition.getMoveStatus().isDone()) {
                                    chessBoard = transition.getTransitionBoard();
                                    moveLog.addMove(move);
                                }
                                sourceTile = null;
                                destinationTile = null;
                                humanMovedPiece = null;
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            gameHistoryPanel.redo(chessBoard, moveLog);
                            takenPiecesPanel.redo(moveLog);
                            if (gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                Table.get().moveMadeUpdate(PlayerType.HUMAN);
                            }
                            boardPanel.drawBoard(chessBoard);
                        }
                    });
				}

				@Override
				public void mouseEntered(final MouseEvent e) {

				}

				@Override
				public void mouseExited(final MouseEvent e) {

				}

				@Override
				public void mousePressed(final MouseEvent e) {

				}

				@Override
				public void mouseReleased(final MouseEvent e) {

				}
			});
			
			validate();
		}
		
		public void drawTilePanel(final Board board) {
			assignTileColor();
			assignTilePieceIcon(board);
			validate();
			repaint();
		}
		
		private void assignTilePieceIcon(final Board board) {
			this.removeAll();
			if(board.getTile(this.tileId).isTileOccupied()) {
				try {
					final BufferedImage	image = ImageIO.read(new File(defaultPieceImagesPath + 
							board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0, 1) +
							board.getTile(this.tileId).getPiece().toString() + ".gif"));
					add(new JLabel(new ImageIcon(image)));
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		private void highlightLegals(final Board board) {
			if (highlightLegalMoves) {
				for (final Move move : pieceLegalMoves(board)) {
					if (move.getDestinationCoordinate() == this.tileId) {
						try {
							add(new JLabel (new ImageIcon(ImageIO.read(new File("")))));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		private Collection<Move> pieceLegalMoves(final Board board) {
			if (humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()) {
				return humanMovedPiece.calculateLegalMoves(board);
			}
			return Collections.emptyList();
		}
		
		private void assignTileColor() {
			if (BoardUtils.EIGHTH_RANK[this.tileId] ||
					BoardUtils.SIXTH_RANK[this.tileId] ||
					BoardUtils.FOURTH_RANK[this.tileId] ||
					BoardUtils.SECOND_RANK[this.tileId]) {
				setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
			}
			else if (BoardUtils.SEVENTH_RANK[this.tileId] ||
					BoardUtils.FIFTH_RANK[this.tileId] ||
					BoardUtils.THIRD_RANK[this.tileId] ||
					BoardUtils.FIRST_RANK[this.tileId]) {
				setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
			}
		}
	}
}