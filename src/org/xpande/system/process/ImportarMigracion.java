package org.xpande.system.process;

import org.compiere.process.SvrProcess;
import org.xpande.system.model.MZSysMigracion;

/**
 * Proceso para importar diccionario en base destino.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 10/31/19.
 */
public class ImportarMigracion extends SvrProcess {

    MZSysMigracion sysMigracion = null;

    @Override
    protected void prepare() {
        this.sysMigracion = new MZSysMigracion(getCtx(), this.getRecord_ID(), get_TrxName());
    }

    @Override
    protected String doIt() throws Exception {

        String message = this.sysMigracion.importData();

        if (message != null){
            return "@Error@ " + message;
        }

        return "OK";
    }
}
