package edu.tseidler.states;

import edu.tseidler.model.*;
import edu.tseidler.service.GameQuitException;
import edu.tseidler.service.InputParser;

class Running extends GameState {

    private static final int MAXIMUM_ROUNDS_PLAYED = 3;

    Running(GameState previousState) {
        super(previousState);
    }

    @Override
    GameState getNextState() {
        if (GameState.gamesPlayed == MAXIMUM_ROUNDS_PLAYED)
            return new GameOverState(this);
        output.accept(board.draw());
        Player currentPlayer = players.getNext();
        Choice choice = new Choice();
        try {
            while (!choice.isValid()) {
                choice = getSelectionFromUser(currentPlayer, Language.build("_PLAYER_ " + currentPlayer.getName() + " _NEXT_MOVE_ " + currentPlayer.getMark()));
                if (board.doWeHaveAWinner())
                    return new WinnerState(this);
            }
            output.accept(Language.build("_PLAYER_ " + currentPlayer.getName() + " _PUT_ " + currentPlayer.getMark() + " _ON_ _FIELD_") + " : " + choice.getSelection() + "\n");
        } catch (GameQuitException e) {
            GameState.gamesPlayed = MAXIMUM_ROUNDS_PLAYED;
            return new GameOverState(this);
        }
        if (!board.ifFull())
            return new Running(this);
        else
            return new DrawState(this);

    }

    private Choice getSelectionFromUser(Player currentPlayer, String message) {
        output.accept(message);
        Choice choice = board.put(new Choice(InputParser.parsePlayerMarkInput(input.get())), currentPlayer.getMark());
        while (!choice.isValid()) {
            choice = getSelectionFromUser(currentPlayer, Language.build("_TRY_ _AGAIN_ _SELECT_NUMBER_OF_FREE_FIELD_"));
        }
        return choice;
    }
}
