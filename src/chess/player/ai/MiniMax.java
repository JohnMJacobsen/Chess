package chess.player.ai;

import chess.board.Board;
import chess.board.Move;
import chess.player.MoveTransition;

public class MiniMax {

	private final BoardEvaluator boardEvaluater;
	private final int searchDepth;

	public MiniMax(final int searchDepth) {
		this.boardEvaluater = new BoardEvaluator();
		this.searchDepth = searchDepth;
	}

	public Move execute(Board board) {

		final long startTime = System.currentTimeMillis();
		Move bestMove = null;
		int highestSeenValue = Integer.MIN_VALUE;
		int lowestSeenValue = Integer.MAX_VALUE;
		int currentValue;

		System.out.println(board.currentPlayer() + " THINKING with depth: " + this.searchDepth);

		for (final Move move : board.currentPlayer().getLegalMoves()) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			if (moveTransition.getMoveStatus().isDone()) {
				currentValue = board.currentPlayer().getAlliance().isWhite()
						? min(moveTransition.getTransitionBoard(), this.searchDepth - 1)
						: max(moveTransition.getTransitionBoard(), this.searchDepth - 1);

				if (board.currentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue) {
					highestSeenValue = currentValue;
					bestMove = move;
				} else if (board.currentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue) {
					lowestSeenValue = currentValue;
					bestMove = move;
				}
			}
		}
		final long executionTime = System.currentTimeMillis() - startTime;
		System.out.println("Took " + executionTime + " milliseconds to decide");
		return bestMove;
	}

	public int min(final Board board, final int depth) {

		if (depth == 0 || isEndGameScenario(board)) {
			return this.boardEvaluater.evaluate(board, depth);
		}
		int lowestSeenValue = Integer.MAX_VALUE;
		for (final Move move : board.currentPlayer().getLegalMoves()) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			if (moveTransition.getMoveStatus().isDone()) {
				final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1);
				if (currentValue <= lowestSeenValue) {
					lowestSeenValue = currentValue;
				}
			}
		}
		return lowestSeenValue;
	}

	public int max(final Board board, final int depth) {
		if (depth == 0 || isEndGameScenario(board)) {
			return this.boardEvaluater.evaluate(board, depth);
		}

		int highestSeenValue = Integer.MIN_VALUE;
		for (final Move move : board.currentPlayer().getLegalMoves()) {
			final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
			if (moveTransition.getMoveStatus().isDone()) {
				final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1);
				if (currentValue >= highestSeenValue) {
					highestSeenValue = currentValue;
				}
			}
		}
		return highestSeenValue;
	}

	private static boolean isEndGameScenario(final Board board) {
		return board.currentPlayer().isInCheckMate() || board.currentPlayer().isInStaleMate();
	}
}