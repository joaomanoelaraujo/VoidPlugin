package org.ltzin.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ltzin.Main;
import org.ltzin.libraries.PlayerMenu;

import java.text.SimpleDateFormat;

public class MenuProfile extends PlayerMenu {

    public MenuProfile(Player profile) {
        super(profile.getPlayer(), "Teste", 4);

//        this.setItem(0, ItemStack.deserialize());

        this.register(Main.getInstance());
        this.open();
    }


}
