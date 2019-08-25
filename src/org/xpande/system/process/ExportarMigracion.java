package org.xpande.system.process;

import org.compiere.process.SvrProcess;
import org.xpande.system.model.MZSysMigracion;

/**
 * Proceso para exportar información de migración.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/24/19.
 */
public class ExportarMigracion extends SvrProcess {

    MZSysMigracion sysMigracion = null;

    @Override
    protected void prepare() {
        this.sysMigracion = new MZSysMigracion(getCtx(), this.getRecord_ID(), get_TrxName());
    }

    @Override
    protected String doIt() throws Exception {

        String message = this.sysMigracion.export();

        if (message != null){
            return "@Error@ " + message;
        }

        return "OK";
    }
}
