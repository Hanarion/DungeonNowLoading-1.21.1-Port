package dev.hexnowloading.dungeonnowloading.entity.util;

import dev.hexnowloading.dungeonnowloading.util.WeightedRandomBag;

import java.util.ArrayList;
import java.util.List;

public class WeightBaseMoveSet<T extends Object> {

    List<Move> moveSet = new ArrayList<>();

    protected class Move {
        T object;
        int weight;
        int cooldown;
        int currentCooldown;
    }

    public void addMove(T object, int weight, int cooldown) {
        Move move = new Move();
        move.object = object;
        move.weight = weight;
        move.cooldown = cooldown;
        move.currentCooldown = 0;
        moveSet.add(move);
    }

    public void addMove(T object, int weight, int cooldown, int initialCooldown) {
        Move move = new Move();
        move.object = object;
        move.weight = weight;
        move.cooldown = cooldown;
        move.currentCooldown = initialCooldown;
        moveSet.add(move);
    }

    public void removeMove(T removeObject) {
        moveSet.removeIf(move -> move.object.equals(removeObject));
    }

    public T selectMove() {
        Move chosenMove;
        WeightedRandomBag<Move> weightedPool = new WeightedRandomBag<>();
        moveSet.forEach(move -> {
            if (move.currentCooldown <= 0) {
                weightedPool.addEntry(move, move.weight);
            } else {
                move.currentCooldown--;
            }
        });

        chosenMove = weightedPool.getRandom();
        if (chosenMove != null) {
            chosenMove.currentCooldown = chosenMove.cooldown;
            return chosenMove.object;
        }
        return null;
    }

    public T selectMoveWithoutCooldownReduction() {
        Move chosenMove;
        WeightedRandomBag<Move> weightedPool = new WeightedRandomBag<>();
        moveSet.forEach(move -> {
            if (move.currentCooldown <= 0) {
                weightedPool.addEntry(move, move.weight);
            }
        });

        chosenMove = weightedPool.getRandom();
        if (chosenMove != null) {
            chosenMove.currentCooldown = chosenMove.cooldown;
            return chosenMove.object;
        }
        return null;
    }

    public void reduceAllCooldown() {
        reduceAllCooldownBy(1);
    }

    public void reduceAllCooldownBy(int cooldown) {
        moveSet.forEach(move -> move.currentCooldown -= cooldown);
    }

    public void increaseAllCooldown() {
        increaseAllCooldownBy(1);
    }

    public void increaseAllCooldownBy(int cooldown) {
        moveSet.forEach(move -> move.currentCooldown += cooldown);
    }


    public void reduceCooldown(T object) {
        moveSet.forEach(move -> {
            if (move.object.equals(object)) {
                move.currentCooldown--;
            }
        });
    }

    public void setOnCooldown(T object) {
        moveSet.forEach(move -> {
            if (move.object.equals(object)) {
                move.currentCooldown = move.cooldown;
            }
        });
    }

    public void addWeight(T object, int additionalWeight) {
        moveSet.forEach(move -> {
            if (move.object.equals(object)) {
                move.weight += additionalWeight;
            }
        });
    }

    public void setWeight(T object, int newWeight) {
        moveSet.forEach(move -> {
            if (move.object.equals(object)) {
                move.weight = newWeight;
            }
        });
    }

    public int getTotalWeight() {
        int totalWeight = 0;
        for (Move move : moveSet) {
            totalWeight += move.weight;
        }
        return totalWeight;
    }


    public boolean isEmpty() {
        return moveSet.isEmpty();
    }

    public void clear() {
        moveSet.clear();
    }

    public void displayAllStats() {
        System.out.println("Moves: ---------------------------------------------------------");
        moveSet.forEach(move -> {
            System.out.println(move.object + " : " + move.weight + " : " + move.cooldown + " : " + move.currentCooldown);
        });
    }
}
