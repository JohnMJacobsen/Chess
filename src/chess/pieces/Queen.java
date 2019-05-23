package chess.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Tile;

public class Queen extends Piece {

	private static final int[] CANDIDATE_OFFSET_VECTORS = { -9, -8, -7, -1, 1, 7, 8, 9 };

	public Queen(final Alliance pieceAlliance, final int piecePosition) {
		super(piecePosition, pieceAlliance, PieceType.QUEEN, true);
	}

	public Queen(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
		super(piecePosition, pieceAlliance, PieceType.QUEEN, isFirstMove);
	}

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {

		final List<Move> legalMoves = new ArrayList<>();
		for (final int possibleOffset : CANDIDATE_OFFSET_VECTORS) {
			int possibleDestination = this.piecePosition;
			while (BoardUtils.isValidTileCoordinate(possibleDestination)) {
				if (isFirstColumnExclusion(possibleDestination, possibleOffset) || 
					isEighthColumnExclusion(possibleDestination, possibleOffset)) {
					break;
				}
				possibleDestination += possibleOffset;
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
						break;
					}
				}
			}
		}
		return ImmutableList.copyOf(legalMoves);
	}

	private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.FIRST_COLUMN[currentPosition] && 
				(candidateOffset == -9 || candidateOffset == -1 || candidateOffset == 7);
	}

	private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset) {
		return BoardUtils.EIGHTH_COLUMN[currentPosition] && 
				(candidateOffset == -7 || candidateOffset == 1 || candidateOffset == 9);
	}
	
	@Override
	public String toString() {
		return PieceType.QUEEN.toString();
	}

	@Override
	public Queen movePiece(Move move) {
		return new Queen(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
	}
}