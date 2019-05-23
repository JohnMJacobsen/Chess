package chess.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Tile;
import chess.pieces.Alliance;

public class Knight extends Piece {

	private final static int[] CANDIDATE_OFFSETS = { -17, -15, -10, -6, 6, 10, 15, 17 };

	public Knight(final Alliance pieceAlliance, final int piecePosition) {
		super(piecePosition, pieceAlliance, PieceType.KNIGHT, true);
	}
	
	public Knight(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
		super(piecePosition, pieceAlliance, PieceType.KNIGHT, isFirstMove);
	}

	@Override
	public Collection<Move> calculateLegalMoves(final Board board) {

		final List<Move> legalMoves = new ArrayList<>();
		for (final int possibleOffset : CANDIDATE_OFFSETS) {
			final int possibleDestination = this.piecePosition + possibleOffset;
			if (BoardUtils.isValidTileCoordinate(possibleDestination)) {
				if (isFirstColumnExclusion(this.piecePosition, possibleOffset) || 
					isSecondColumnExclusion(this.piecePosition, possibleOffset) || 
					isSeventhColumnExclusion(this.piecePosition, possibleOffset) || 
					isEighthColumnExclusion(this.piecePosition, possibleOffset)) {
					continue;
				}
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
		return BoardUtils.FIRST_COLUMN[currentPosition] && 
				(candidateOffset == -17 || candidateOffset == -10 || candidateOffset == 6 || candidateOffset == 15);
	}

	private static boolean isSecondColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.SECOND_COLUMN[currentPosition] && (candidateOffset == -10 || candidateOffset == 6);
	}

	private static boolean isSeventhColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.SEVENTH_COLUMN[currentPosition] && (candidateOffset == -6 || candidateOffset == 10);
	}

	private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.EIGHTH_COLUMN[currentPosition] && 
				(candidateOffset == -15 || candidateOffset == -6 || candidateOffset == 10 || candidateOffset == 17);
	}
	
	@Override 
	public String toString() {
		return PieceType.KNIGHT.toString();
	}

	@Override
	public Knight movePiece(Move move) {
		return new Knight(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
	}

}
