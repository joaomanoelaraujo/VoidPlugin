package org.ltzin.database.data.interfaces;

import org.ltzin.database.data.DataContainer;

public abstract class AbstractContainer {

    protected DataContainer dataContainer;

    public AbstractContainer(DataContainer dataContainer) {
        this.dataContainer = dataContainer;
    }

    public void gc() {
        this.dataContainer = null;
    }
}