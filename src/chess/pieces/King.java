package chess.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Tile;

public class King extends Piece {

	private static final int[] CANDIDATE_OFFSETS = { -9, -8, -7, -1, 1, 7, 8, 9 };

	public King(final Alliance pieceAlliance, final int piecePosition) {
		super(piecePosition, pieceAlliance, PieceType.KING, true);
	}
	
	public King(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
		super(piecePosition, pieceAlliance, PieceType.KING, isFirstMove);
	}

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {

		final List<Move> legalMoves = new ArrayList<>();
		for (final int possibleOffset : CANDIDATE_OFFSETS) {
			final int possibleDestination = this.piecePosition + possibleOffset;
			if (isFirstColumnExclusion(this.piecePosition, possibleOffset) || 
				(isEighthColumnExclusion(this.piecePosition, possibleOffset))) {
				continue;
			}
			if (BoardUtils.isValidTileCoordinate(possibleDestination)) {
				final Tile candidateDestinationTile = board.getTile(possibleDestination);
				if (!candidateDestinationTile.isTileOccupied()) {
					legalMoves.add(new Move.MajorMove(board, this, possibleDestination));
				} else {
					final Piece pieceAtDestination = candidateDestinationTile.getPiece();
					final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();
					if (this.pieceAlliance != pieceAlliance) {
						legalMoves.add(new Move.MajorAttackMove(board, this, possibleDestination, pieceAtDestination));
					}
				}
			}
		}
		return ImmutableList.copyOf(legalMoves);
	}

	private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.FIRST_COLUMN[currentPosition]
				&& (candidateOffset == -9 || candidateOffset == -1 || candidateOffset == 7);
	}

	private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.EIGHTH_COLUMN[currentPosition]
				&& (candidateOffset == -7 || candidateOffset == 1 || candidateOffset == 9);
	}
	
	@Override 
	public String toString() {
		return PieceType.KING.toString();
	}

	@Override
	public King movePiece(Move move) {
		return new King(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
	}
}