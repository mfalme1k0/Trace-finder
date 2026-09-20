package com.tracefinder.rulebook;

import java.util.List;

public class RulebookIsEmptyException extends RulebookFormatException {

    public RulebookIsEmptyException() {
        super("Rulebook is empty");
    }
}