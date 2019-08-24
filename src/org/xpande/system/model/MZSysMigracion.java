package org.xpande.system.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.util.DB;
import org.xpande.system.migration.ADVal_Rule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Modelo para tabla cabezal del proceso de migración de diccionario.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/23/19.
 */
public class MZSysMigracion extends X_Z_Sys_Migracion {

    private String whereClause = "";

    public MZSysMigracion(Properties ctx, int Z_Sys_Migracion_ID, String trxName) {
        super(ctx, Z_Sys_Migracion_ID, trxName);
    }

    public MZSysMigracion(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    /***
     * Método que obtiene elementos del diccionario de datos según opciones y filtros indicados por el usuario.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    public String getData() {

        String message = null;
        try{

            // Elimino información anterior
            String action = " delete from z_sys_migracionlin where z_sys_migracion_id =" + this.get_ID();
            DB.executeUpdateEx(action, get_TrxName());

            // Armo condiciones de filtros
            this.setWhereClause();

            // Obtener elementos
            if (this.isMigElemento()){
                message = this.getElementos();
                if (message != null){
                    return message;
                }
            }
            // Obtener validaciones
            if (this.isMigValidacion()){
                message = this.getValidaciones();
                if (message != null){
                    return message;
                }
            }
            // Obtener referencias
            if (this.isMigReferencia()){
                message = this.getReferencias();
                if (message != null){
                    return message;
                }
            }
            // Obtener tablas
            if (this.isMigTabla()){
                message = this.getTablas();
                if (message != null){
                    return message;
                }
            }
            // Obtener procesos
            if (this.isMigProceso()){
                message = this.getProcesos();
                if (message != null){
                    return message;
                }
            }
            // Obtener ventanas
            if (this.isMigVentana()){
                message = this.getVentanas();
                if (message != null){
                    return message;
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Arma condiciones de filtros seǵun opciones indicadas por el usuario.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     */
    private void setWhereClause() {

        try{
            this.whereClause = "";

            if (this.getEntityType() != null){
                whereClause += " and a.entitytype ='" + this.getEntityType() + "' ";
            }
            if ((this.getVersionNo() != null) && (!this.getVersionNo().trim().equalsIgnoreCase(""))){
                whereClause += " and a.versionno >='" + this.getVersionNo().trim() + "' ";
            }
            if ((this.getTextoFiltro() != null) && (!this.getTextoFiltro().trim().equalsIgnoreCase(""))){
                whereClause += " and lower(a.name) like '" + this.getTextoFiltro().trim().toLowerCase() + "' ";
            }
            if (this.getStartDate() != null){
                whereClause += " and a.updated >='" + this.getStartDate() + "' ";
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Obtiene elementos del diccionario según filtros indicados
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    private String getElementos() {

        String message = null;

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.ad_element_id, a.name, a.versionno, a.updated, a.entitytype, a.ad_reference_value_id " +
                    " from ad_element a " +
                    " where a.isactive ='Y' and a.entitytype !='D' " + whereClause +
                    " order by a.updated ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                if (this.existeTablaRecord(I_AD_Element.Table_ID, rs.getInt("ad_element_id"))){
                    continue;
                }

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO);
                sysMigracionLin.setName(rs.getString("name"));
                sysMigracionLin.setAD_Table_ID(I_AD_Element.Table_ID);
                sysMigracionLin.setRecord_ID(rs.getInt("ad_element_id"));
                sysMigracionLin.setStartDate(rs.getTimestamp("updated"));
                sysMigracionLin.setVersionNo(rs.getString("versionno"));
                sysMigracionLin.setEntityType(rs.getString("entitytype"));
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.saveEx();

                // Verifico si este elemento tiene una referencia, en cuyo caso debo considerarla en el proceso de migracion
                if (rs.getInt("ad_reference_value_id") > 0){

                    X_AD_Reference reference = new X_AD_Reference(getCtx(), rs.getInt("ad_reference_value_id"), null);
                    if (!reference.getEntityType().equalsIgnoreCase(ENTITYTYPE_Dictionary)){
                        if (!this.existeTablaRecord(I_AD_Reference.Table_ID, reference.get_ID())){
                            MZSysMigracionLin sysMigraRef = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                            sysMigraRef.setZ_Sys_Migracion_ID(this.get_ID());
                            sysMigraRef.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA);
                            sysMigraRef.setName(reference.getName());
                            sysMigraRef.setAD_Table_ID(I_AD_Reference.Table_ID);
                            sysMigraRef.setRecord_ID(reference.get_ID());
                            sysMigraRef.setStartDate(reference.getUpdated());
                            sysMigraRef.setVersionNo(reference.get_ValueAsString("VersionNo"));
                            sysMigraRef.setEntityType(reference.getEntityType());
                            sysMigraRef.setIsSelected(true);
                            sysMigraRef.setTipoSysMigraObjFrom(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO);
                            sysMigraRef.setParentName(sysMigracionLin.getName());
                            sysMigraRef.setParent_ID(sysMigracionLin.getRecord_ID());
                            sysMigraRef.saveEx();
                        }
                    }
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }

        return message;
    }

    /***
     * Obtiene validaciones del diccionario según filtros indicados
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    private String getValidaciones() {

        String message = null;

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.ad_val_rule_id, a.name, a.versionno, a.updated, a.entitytype " +
                    " from ad_val_rule a " +
                    " where a.isactive ='Y' and a.entitytype !='D' " + whereClause +
                    " order by a.updated ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                if (this.existeTablaRecord(I_AD_Val_Rule.Table_ID, rs.getInt("ad_val_rule_id"))){
                    continue;
                }

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VALIDACION);
                sysMigracionLin.setName(rs.getString("name"));
                sysMigracionLin.setAD_Table_ID(I_AD_Val_Rule.Table_ID);
                sysMigracionLin.setRecord_ID(rs.getInt("ad_val_rule_id"));
                sysMigracionLin.setStartDate(rs.getTimestamp("updated"));
                sysMigracionLin.setVersionNo(rs.getString("versionno"));
                sysMigracionLin.setEntityType(rs.getString("entitytype"));
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }

        return message;
    }

    /***
     * Obtiene referencias del diccionario según filtros indicados.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    private String getReferencias() {

        String message = null;

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.ad_reference_id, a.name, a.versionno, a.updated, a.entitytype " +
                    " from ad_reference a " +
                    " where a.isactive ='Y' and a.entitytype !='D' " + whereClause +
                    " order by a.updated ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                if (this.existeTablaRecord(I_AD_Reference.Table_ID, rs.getInt("ad_reference_id"))){
                    continue;
                }

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA);
                sysMigracionLin.setName(rs.getString("name"));
                sysMigracionLin.setAD_Table_ID(I_AD_Reference.Table_ID);
                sysMigracionLin.setRecord_ID(rs.getInt("ad_reference_id"));
                sysMigracionLin.setStartDate(rs.getTimestamp("updated"));
                sysMigracionLin.setVersionNo(rs.getString("versionno"));
                sysMigracionLin.setEntityType(rs.getString("entitytype"));
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }

        return message;
    }

    /***
     * Obtiene tablas del diccionario según filtros indicados.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    private String getTablas() {

        String message = null;

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.ad_table_id, a.name, a.versionno, a.updated, a.entitytype " +
                    " from ad_table a " +
                    " where a.isactive ='Y' and a.entitytype !='D' " + whereClause +
                    " order by a.updated ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                if (this.existeTablaRecord(I_AD_Table.Table_ID, rs.getInt("ad_table_id"))){
                    continue;
                }

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA);
                sysMigracionLin.setName(rs.getString("name"));
                sysMigracionLin.setAD_Table_ID(I_AD_Table.Table_ID);
                sysMigracionLin.setRecord_ID(rs.getInt("ad_table_id"));
                sysMigracionLin.setStartDate(rs.getTimestamp("updated"));
                sysMigracionLin.setVersionNo(rs.getString("versionno"));
                sysMigracionLin.setEntityType(rs.getString("entitytype"));
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }

        return message;
    }

    /***
     * Obtiene procesos del diccionario según filtros indicados.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    private String getProcesos() {

        String message = null;

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.ad_process_id, a.name, a.versionno, a.updated, a.entitytype " +
                    " from ad_process a " +
                    " where a.isactive ='Y' and a.entitytype !='D' " + whereClause +
                    " order by a.updated ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                if (this.existeTablaRecord(I_AD_Process.Table_ID, rs.getInt("ad_process_id"))){
                    continue;
                }

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO);
                sysMigracionLin.setName(rs.getString("name"));
                sysMigracionLin.setAD_Table_ID(I_AD_Process.Table_ID);
                sysMigracionLin.setRecord_ID(rs.getInt("ad_process_id"));
                sysMigracionLin.setStartDate(rs.getTimestamp("updated"));
                sysMigracionLin.setVersionNo(rs.getString("versionno"));
                sysMigracionLin.setEntityType(rs.getString("entitytype"));
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
        	rs = null; pstmt = null;
        }

        return message;
    }

    /***
     * Obtiene ventanas del diccionario según filtros indicados.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @return
     */
    private String getVentanas() {

        String message = null;

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.ad_window_id, a.name, a.versionno, a.updated, a.entitytype " +
                    " from ad_window a " +
                    " where a.isactive ='Y' and a.entitytype !='D' " + whereClause +
                    " order by a.updated ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA);
                sysMigracionLin.setName(rs.getString("name"));
                sysMigracionLin.setAD_Table_ID(I_AD_Window.Table_ID);
                sysMigracionLin.setRecord_ID(rs.getInt("ad_window_id"));
                sysMigracionLin.setStartDate(rs.getTimestamp("updated"));
                sysMigracionLin.setVersionNo(rs.getString("versionno"));
                sysMigracionLin.setEntityType(rs.getString("entitytype"));
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
            rs = null; pstmt = null;
        }

        return message;
    }


    /***
     * Verifica si para este proceso, ya existe una linea para determinada tabla y record recibidos.
     * Xpande. Created by Gabriel Vila on 8/23/19.
     * @param adTableID
     * @param recordID
     * @return
     */
    private boolean existeTablaRecord(int adTableID, int recordID){

        boolean value = false;

        try{
            String sql = " select count(*) as contador " +
                            " from z_sys_migracionlin " +
                            " where z_sys_migracion_id =" + this.get_ID() +
                            " and ad_table_id =" + adTableID +
                            " and record_id =" + recordID;
            int contador = DB.getSQLValueEx(get_TrxName(), sql);
            if (contador > 0){
                value = true;
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        return value;
    }

}
