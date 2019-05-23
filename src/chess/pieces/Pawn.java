package chess.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;

public class Pawn extends Piece {

	private final static int[] CANDIDATE_OFFSETS = { 7, 8, 9, 16 };

	public Pawn(final Alliance pieceAlliance, final int piecePosition) {
		super(piecePosition, pieceAlliance, PieceType.PAWN, true);
	}

	public Pawn(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
		super(piecePosition, pieceAlliance, PieceType.PAWN, isFirstMove);
	}

	@Override
	public Collection<Move> calculateLegalMoves(Board board) {

		final List<Move> legalMoves = new ArrayList<>();
		for (final int possibleOffset : CANDIDATE_OFFSETS) {
			final int possibleDestination = this.piecePosition + (possibleOffset * this.getPieceAlliance().getDirection());
			if (!BoardUtils.isValidTileCoordinate(possibleDestination)) {
				continue;
			}
			if (possibleOffset == 8 && !board.getTile(possibleDestination).isTileOccupied()) {
				if (this.pieceAlliance.isPawnPromotionSquare(possibleDestination)) {
					legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board, this, possibleDestination)));
				} else {
					legalMoves.add(new Move.PawnMove(board, this, possibleDestination));
				}
			} 
			else if (possibleOffset == 16 && this.isFirstMove() && ((BoardUtils.SEVENTH_RANK[this.piecePosition] && this.getPieceAlliance().isBlack()) || 
					(BoardUtils.SECOND_RANK[this.piecePosition] && this.getPieceAlliance().isWhite()))) {
				final int behindPossibleDestination = this.piecePosition + (this.pieceAlliance.getDirection() * 8);
				if (!board.getTile(behindPossibleDestination).isTileOccupied() && 
					!board.getTile(possibleDestination).isTileOccupied()) {
					legalMoves.add(new Move.PawnMove(board, this, possibleDestination));
				}
			} 
			else if (possibleOffset == 7 && !((BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()) || 
					(BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()))) {
				if (board.getTile(possibleDestination).isTileOccupied()) {
					final Piece pieceOnCandidate = board.getTile(possibleDestination).getPiece();
					if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
						if (this.pieceAlliance.isPawnPromotionSquare(possibleDestination)) {
							legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board, this, possibleDestination)));
						} else {
							legalMoves.add(new Move.PawnAttackMove(board, this, possibleDestination, pieceOnCandidate));
						}
					}
				} 
				else if (board.getEnPassantPawn() != null) {
					if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition + (this.pieceAlliance.getOppositeDirection()))) {
						final Piece pieceOnCandidate = board.getEnPassantPawn();
						if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
							legalMoves.add(new Move.PawnEnPassantAttackMove(board, this, possibleDestination, pieceOnCandidate));
						}
					}
				}
			} 
			else if (possibleOffset == 9 && !((BoardUtils.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()) || 
					(BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack()))) {
				if (board.getTile(possibleDestination).isTileOccupied()) {
					final Piece pieceOnCandidate = board.getTile(possibleDestination).getPiece();
					if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
						if (this.pieceAlliance.isPawnPromotionSquare(possibleDestination)) {
							legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board, this, possibleDestination)));
						} else {
							legalMoves.add(new Move.PawnAttackMove(board, this, possibleDestination, pieceOnCandidate));
						}
					}
				} 
				else if (board.getEnPassantPawn() != null) {
					if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition - (this.pieceAlliance.getOppositeDirection()))) {
						final Piece pieceOnCandidate = board.getEnPassantPawn();
						if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
							legalMoves.add(new Move.PawnEnPassantAttackMove(board, this, possibleDestination, pieceOnCandidate));
						}
					}
				}
			}
		}
		return ImmutableList.copyOf(legalMoves);
	}

	public Piece getPromotionPiece() {
		return new Queen(this.pieceAlliance, this.piecePosition, false);
	}

	@Override
	public String toString() {
		return PieceType.PAWN.toString();
	}

	@Override
	public Pawn movePiece(Move move) {
		return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
	}
}