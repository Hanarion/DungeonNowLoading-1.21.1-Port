package dev.hexnowloading.dungeonnowloading.entity.util.move_selector;

import dev.hexnowloading.dungeonnowloading.entity.util.WeightBaseMoveSet;

import java.util.ArrayList;
import java.util.List;

public class ExhaustibleMoveSet<T> extends WeightBaseMoveSet<T> {

    List<Move> moveSet = new ArrayList<>();

    private class Move {
        T object;
        int weight;
        int cooldown;
        int currentCooldown;
        int exhaustion;
    }

    public void addMove(T object, int weight, int cooldown, int initialCooldown, int exhaustion) {
        Move move = new Move();
        move.object = object;
        move.weight = weight;
        move.cooldown = cooldown;
        move.currentCooldown = initialCooldown;
        move.exhaustion = exhaustion;
        moveSet.add(move);
    }

    public int getExhaustion(T object) {
        for (Move move : moveSet) {
            if (move.object.equals(object)) {
                return move.exhaustion;
            }
        }
        return 0;
    }
}
