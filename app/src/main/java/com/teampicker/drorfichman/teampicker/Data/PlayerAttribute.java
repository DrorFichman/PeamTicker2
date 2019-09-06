package com.teampicker.drorfichman.teampicker.Data;

public enum PlayerAttribute {
    isGK("_is_gk"),
    isDefender("_is_defender"),
    isPlaymaker("_is_playmaker"),
    isUnbreakable("_is_breakable");

    public String attribute;

    PlayerAttribute(String att) {
        attribute = att;
    }
}

