package chess.player.ai;

import chess.board.Board;
import chess.pieces.Piece;
import chess.player.Player;

public final class BoardEvaluator {
	
	private static final int CHECK_BONUS = 5;

	//@Override
	public int evaluate(final Board board, final int depth) {
		return ScorePlayer(board, board.whitePlayer(), depth) - 
				ScorePlayer(board, board.blackPlayer(), depth);
	}

	private int ScorePlayer(final Board board, final Player player, final int depth) {
		return pieceValue(player) + mobility(player) + check(player);
	}

	private static int mobility(final Player player) {
		return (int) player.getLegalMoves().size();
	}
	
	private static int check(final Player player) {
		return player.getOpponent().isInCheck() ? CHECK_BONUS : 0;
	}

	private static int pieceValue(final Player player) {
		int pieceValueScore = 0;
		for (final Piece piece : player.getActivePieces()) {
			pieceValueScore += piece.getPieceValue();
		}
		return pieceValueScore;
	}
}