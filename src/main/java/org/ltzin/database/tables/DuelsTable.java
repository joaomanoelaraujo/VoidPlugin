package org.ltzin.database.tables;

import org.ltzin.database.data.DataContainer;
import org.ltzin.database.data.DataTable;
import org.ltzin.database.data.interfaces.DataTableInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@DataTableInfo(
        name = "vPluginDuels",
        create = "CREATE TABLE IF NOT EXISTS `vPluginDuels` (" +
                "`name` VARCHAR(32)," +

                " `sumowinstreak` LONG," +
                " `soupwinstreak` LONG," +
                " `laststreak` LONG," +

                " `sumokills` LONG," +
                " `sumodeaths` LONG," +
                " `sumoassists` LONG," +
                " `sumogames` LONG," +
                " `sumowins` LONG," +

                " `soupkills` LONG," +
                " `soupdeaths` LONG," +
                " `soupassists` LONG," +
                " `soupgames` LONG," +
                " `soupwins` LONG," +

                " `coins` DOUBLE," +
                " `lastmap` LONG," +
                " `cosmetics` TEXT," +
                " `selected` TEXT," +
                " `kitconfig` TEXT," +
                " PRIMARY KEY(`name`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;",
        select = "SELECT * FROM `vPluginDuels` WHERE LOWER(`name`) = ?",
        insert = "INSERT INTO `vPluginDuels` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        update = "UPDATE `vPluginDuels` SET " +

                " `sumowinstreak` = ?," +
                " `soupwinstreak` = ?," +
                " `laststreak` = ?," +

                " `sumokills` = ?," +
                " `sumodeaths` = ?," +
                " `sumoassists` = ?," +
                " `sumogames` = ?," +
                " `sumowins` = ?," +

                " `soupkills` = ?," +
                " `soupdeaths` = ?," +
                " `soupassists` = ?," +
                " `soupgames` = ?," +
                " `soupwins` = ?," +

                " `coins` = ?," +
                " `lastmap` = ?," +
                " `cosmetics` = ?," +
                " `selected` = ?," +
                " `kitconfig` = ?" +
                " WHERE LOWER(`name`) = ?"
)
public class DuelsTable extends DataTable {

    @Override
    public Map<String, DataContainer> getDefaultValues() {
        Map<String, DataContainer> defaultValues = new LinkedHashMap<>();

        defaultValues.put("sumowinstreak", new DataContainer(0L));
        defaultValues.put("soupwinstreak", new DataContainer(0L));
        defaultValues.put("laststreak", new DataContainer(System.currentTimeMillis()));

        defaultValues.put("sumokills", new DataContainer(0L));
        defaultValues.put("sumodeaths", new DataContainer(0L));
        defaultValues.put("sumoassists", new DataContainer(0L));
        defaultValues.put("sumogames", new DataContainer(0L));
        defaultValues.put("sumowins", new DataContainer(0L));

        defaultValues.put("soupkills", new DataContainer(0L));
        defaultValues.put("soupdeaths", new DataContainer(0L));
        defaultValues.put("soupassists", new DataContainer(0L));
        defaultValues.put("soupgames", new DataContainer(0L));
        defaultValues.put("soupwins", new DataContainer(0L));

        defaultValues.put("coins", new DataContainer(0.0D));
        defaultValues.put("lastmap", new DataContainer(0L));
        defaultValues.put("cosmetics", new DataContainer("{}"));
        defaultValues.put("selected", new DataContainer("{}"));
        defaultValues.put("kitconfig", new DataContainer("{}"));
        return defaultValues;
    }
}