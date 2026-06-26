package org.ltzin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ltzin.menus.MenuProfile;
import org.ltzin.player.Profile;

public class TesteCmd extends Commands{

    public TesteCmd() {
        super("a");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        new MenuProfile(Profile.getProfile(sender.getName()));
    }
}
