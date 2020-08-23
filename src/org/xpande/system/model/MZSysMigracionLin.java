package org.xpande.system.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.Query;

import java.sql.ResultSet;
import java.util.List;
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


    @Override
    protected boolean afterSave(boolean newRecord, boolean success) {

        if (!success) return false;

        if (!newRecord){
            if (is_ValueChanged(X_Z_Sys_MigracionLin.COLUMNNAME_IsSelected)){

                // Si el cabezal esta configurado para marcar lineas de manera recursiva, lo hago.
                if (this.getZ_Sys_Migracion().isMarcarRecursivo()){
                    // Seteo hijas de esta linea
                    this.setSelectedChilds(this.getTipoSysMigraObj(), this.getRecord_ID(), this.isSelected());
                }
            }
        }

        return true;
    }

    /***
     * Setea lineas seleccionadas según objeto padre recibido.
     * Xpande. Created by Gabriel Vila on 8/22/20.
     * @param tipoSysMigraObjParent
     * @param parentID
     * @param isSelected
     */
    private void setSelectedChilds(String tipoSysMigraObjParent, int parentID, boolean isSelected){

        try{
            // Obtengo lineas hijas del objeto recibido
            List<MZSysMigracionLin> childLineList = this.getChildObjects(tipoSysMigraObjParent, parentID);
            for (MZSysMigracionLin migracionLin: childLineList){

                // Seteo linea seleccionada o no segun parametro recibido
                migracionLin.setIsSelected(isSelected);
                migracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Obtiene y retorna lineas de este modelo que tienen determinado objeto padre.
     * Xpande. Created by Gabriel Vila on 8/22/20.
     * @param tipoSysMigraObjParent
     * @param parentID
     * @return
     */
    public List<MZSysMigracionLin> getChildObjects(String tipoSysMigraObjParent, int parentID){

        String whereClause = X_Z_Sys_MigracionLin.COLUMNNAME_Z_Sys_Migracion_ID + " =" + this.getZ_Sys_Migracion_ID() +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_TipoSysMigraObjFrom + " ='" + tipoSysMigraObjParent + "' " +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_Parent_ID + " =" + parentID;

        List<MZSysMigracionLin> lines = new Query(getCtx(), I_Z_Sys_MigracionLin.Table_Name, whereClause, get_TrxName()).list();

        return lines;
    }

}
