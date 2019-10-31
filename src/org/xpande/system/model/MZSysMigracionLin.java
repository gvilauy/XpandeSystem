package org.xpande.system.model;

import java.sql.ResultSet;
import java.util.Properties;

/**
 * Modelo para linea del proceso de migración de diccionario.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/23/19.
 */
public class MZSysMigracionLin extends X_Z_Sys_MigracionLin {

    public MZSysMigracionLin(Properties ctx, int Z_Sys_MigracionLin_ID, String trxName) {
        super(ctx, Z_Sys_MigracionLin_ID, trxName);
    }

    public MZSysMigracionLin(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

}
