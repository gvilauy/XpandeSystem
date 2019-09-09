package org.xpande.system.process;

import org.compiere.process.SvrProcess;
import org.xpande.system.model.MZSysMigracion;

/**
 * Proceso para leer información desde archivo de interface de migración de dicccionario.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 9/8/19.
 */
public class LeerArchivoMigracion extends SvrProcess {

    MZSysMigracion sysMigracion = null;

    @Override
    protected void prepare() {

    }

    @Override
    protected String doIt() throws Exception {

        String message = this.sysMigracion.getDataFile();

        if (message != null){
            return "@Error@ " + message;
        }

        return "OK";
    }
}
