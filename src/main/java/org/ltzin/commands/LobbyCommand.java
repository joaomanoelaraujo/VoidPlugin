package org.ltzin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ltzin.Main;
import org.ltzin.game.FakeGame;
import org.ltzin.game.Game;
import org.ltzin.player.Profile;

public class LobbyCommand extends Commands {
   public LobbyCommand() {
      super("lobby", "l");
   }

   public void perform(CommandSender sender, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("§cApenas jogadores podem utilizar este comando.");
      } else {
         Player player = (Player)sender;
         Profile profile = Profile.getProfile(player.getName());
         Game<?> game = profile.getGame();
         if (game != null && !(game instanceof FakeGame)) {
               if (profile.playingGame()){
                  game.leave(profile, null);
               }
         } else {
            Main.sendServer(profile, "lobby");
         }
      }
   }
}
