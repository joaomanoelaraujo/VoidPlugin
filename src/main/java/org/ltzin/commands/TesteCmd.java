package org.ltzin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ltzin.menus.MenuProfile;

public class TesteCmd extends Commands{

    public TesteCmd() {
        super("a");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        new MenuProfile((Player) sender);
    }
}
