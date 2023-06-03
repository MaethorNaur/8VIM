package inc.flide.vim8.utils;

import java.util.ArrayList;
import java.util.List;

import inc.flide.vim8.structures.CharacterPosition;
import inc.flide.vim8.structures.Constants;
import inc.flide.vim8.structures.Direction;
import inc.flide.vim8.structures.FingerPosition;
import inc.flide.vim8.structures.Quadrant;
import inc.flide.vim8.structures.yaml.ExtraLayer;

public final class MovementSequenceHelper {
    private MovementSequenceHelper() {
    }

    public static List<FingerPosition> computeMovementSequence(int layer, Quadrant quadrant, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();

        if (layer == Constants.HIDDEN_LAYER) {
            return movementSequence;
        }

        List<FingerPosition> movementSequencesForDefaultLayer = movementSequencesForLayer(layer, quadrant, position);
        if (movementSequencesForDefaultLayer.isEmpty()) {
            return movementSequencesForDefaultLayer;
        } else {
            movementSequence.addAll(movementSequencesForDefaultLayer);
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);
        return movementSequence;
    }

    private static List<FingerPosition> movementSequencesForLayer(int layer, Quadrant quadrant, CharacterPosition position) {
        List<FingerPosition> movementSequence = new ArrayList<>();
        int maxMovements = position.ordinal() + 1;

        if (layer > Constants.DEFAULT_LAYER) {
            ExtraLayer extraLayer = ExtraLayer.values()[layer - 2];
            List<FingerPosition> extraLayerMovementSequence = ExtraLayer.MOVEMENT_SEQUENCES.get(extraLayer);
            if (extraLayerMovementSequence != null) {
                movementSequence.addAll(extraLayerMovementSequence);
            }
        }

        movementSequence.add(FingerPosition.INSIDE_CIRCLE);

        for (int i = 0; i <= maxMovements; i++) {
            FingerPosition lastPosition = movementSequence.get(movementSequence.size() - 1);
            FingerPosition nextPosition = getNextPosition(quadrant, lastPosition);
            movementSequence.add(nextPosition);
        }
        return movementSequence;
    }

    private static FingerPosition getNextPosition(Quadrant quadrant, FingerPosition lastPosition) {
        FingerPosition currentSector = Direction.toFingerPosition(quadrant.getSector());
        FingerPosition oppositeSector = Direction.toFingerPosition(Direction.getOpposite(quadrant.getSector()));
        FingerPosition currentPart = Direction.toFingerPosition(quadrant.getPart());
        FingerPosition oppositePart = Direction.toFingerPosition(Direction.getOpposite(quadrant.getPart()));

        if (lastPosition == FingerPosition.INSIDE_CIRCLE || lastPosition == oppositePart) {
            return currentSector;
        } else if (lastPosition == oppositeSector) {
            return oppositePart;
        } else if (lastPosition == currentPart) {
            return oppositeSector;
        } else {
            return currentPart;
        }
    }
}