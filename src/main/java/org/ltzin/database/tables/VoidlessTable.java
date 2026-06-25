package org.ltzin.database.tables;

import org.ltzin.database.data.DataContainer;
import org.ltzin.database.data.DataTable;
import org.ltzin.database.data.interfaces.DataTableInfo;
import org.ltzin.database.storage.implementation.StorageImplementation;

import java.util.LinkedHashMap;
import java.util.Map;

@DataTableInfo(
        name = "VoidProfile", //mudar para VoidProfile
        create = "CREATE TABLE IF NOT EXISTS `VoidProfile` ("
                + "`name` VARCHAR(32), "
                + "`cash` LONG, "
                + "`role` TEXT, "
                + "`created` LONG, "
                + "`lastlogin` LONG, "
                + "PRIMARY KEY(`name`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;",
        select = "SELECT * FROM `VoidProfile` WHERE LOWER(`name`) = ?",
        insert = "INSERT INTO `VoidProfile` VALUES (?, ?, ?, ?, ?)",
        update = "UPDATE `VoidProfile` SET "
                + "`cash` = ?, `role` = ?, `created` = ?, `lastlogin` = ? "
                + "WHERE LOWER(`name`) = ?"
)
public class VoidlessTable extends DataTable {

    @Override
    public void init(StorageImplementation storage) {
        //Nothing
           }

    @Override
    public Map<String, DataContainer> getDefaultValues() {
        Map<String, DataContainer> defaults = new LinkedHashMap<>();
        defaults.put("cash",      new DataContainer(0L));
        defaults.put("role",      new DataContainer("Membro"));
        defaults.put("created",   new DataContainer(System.currentTimeMillis()));
        defaults.put("lastlogin", new DataContainer(System.currentTimeMillis()));
        return defaults;
    }
}