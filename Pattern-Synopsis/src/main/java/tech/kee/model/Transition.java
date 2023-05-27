package tech.kee.model;


public record Transition(int sourceEventId, int targetEventId, int transitionWindow) {
}
