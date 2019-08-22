package org.xpande.system.process;

import org.compiere.process.SvrProcess;
import org.xpande.system.model.Init;

/**
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/22/19.
 */
public class PruebaMigracion extends SvrProcess {

    @Override
    protected void prepare() {

    }

    @Override
    protected String doIt() throws Exception {

        Init init = new Init();

        init.prueba(getCtx());

        return "OK";
    }
}
