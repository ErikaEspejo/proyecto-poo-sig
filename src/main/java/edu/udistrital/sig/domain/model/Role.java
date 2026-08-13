package edu.udistrital.sig.domain.model;

public enum Role {
    CONSULTATION,
    ADMINISTRATOR;

    public boolean canModifyEntities() {
        return this == ADMINISTRATOR;
    }
}
