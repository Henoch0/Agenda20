package edu.hm.cs.katz.swt2.agenda.common;

public enum TaskTypeEnum {
    NOTYPE("Kein Aufgabentyp"), PFLICHT("Pflichtaufgabe"), OPTIONAL("Freiwillig"), GROUP("Gruppenarbeit"),
    CERTIFICATE("Abgabe für Schein"), PRESENTATION("Präsentation");

    private final String displayValue;

    private TaskTypeEnum(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
