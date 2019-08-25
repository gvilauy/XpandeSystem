package org.xpande.system.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.util.DB;
import org.compiere.util.ValueNamePair;
import org.xpande.system.migration.*;

import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 * Modelo para tabla cabezal del proceso de migración de diccionario.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/23/19.
 */
public class MZSysMigracion extends X_Z_Sys_Migracion {

    private String whereClause = "";
    private CabezalMigracion cabezalMigracion = null;

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
                X_AD_Element element = new X_AD_Element(getCtx(), rs.getInt("ad_element_id"), null);
                this.setElementoLin(element, null, null, 0);
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
     * Setea linea de este documento con datos de un Elemento del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param element
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setElementoLin(X_AD_Element element, String parentType, String parentName, int parentID){

        try{
            if (element.getEntityType().equalsIgnoreCase(X_AD_Element.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_Element.Table_ID, element.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO);
            sysMigracionLin.setName(element.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Element.Table_ID);
            sysMigracionLin.setRecord_ID(element.get_ID());
            sysMigracionLin.setStartDate(element.getUpdated());
            sysMigracionLin.setVersionNo(element.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(element.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Verifico si este elemento tiene una referencia, en cuyo caso debo considerarla en el proceso de migracion
            if (element.getAD_Reference_Value_ID() > 0){
                X_AD_Reference reference = new X_AD_Reference(getCtx(), element.getAD_Reference_Value_ID(), null);
                this.setReferenciaLin(reference, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO, element.getName(), element.get_ID());
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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
                X_AD_Val_Rule valRule = new X_AD_Val_Rule(getCtx(), rs.getInt("ad_val_rule_id"), null);
                this.setValidacionLin(valRule, null, null, 0);
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
     * Setea linea de este documento con datos de una Validacion del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param valRule
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setValidacionLin(X_AD_Val_Rule valRule, String parentType, String parentName, int parentID){

        try{
            if (valRule.getEntityType().equalsIgnoreCase(X_AD_Val_Rule.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_Val_Rule.Table_ID, valRule.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VALIDACION);
            sysMigracionLin.setName(valRule.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Val_Rule.Table_ID);
            sysMigracionLin.setRecord_ID(valRule.get_ID());
            sysMigracionLin.setStartDate(valRule.getUpdated());
            sysMigracionLin.setVersionNo(valRule.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(valRule.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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
                X_AD_Reference reference = new X_AD_Reference(getCtx(), rs.getInt("ad_reference_id"), null);
                this.setReferenciaLin(reference, null, null, 0);
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
     * Setea linea de este documento con datos de una Referencia del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param reference
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setReferenciaLin(X_AD_Reference reference, String parentType, String parentName, int parentID){

        try{
            if (reference.getEntityType().equalsIgnoreCase(X_AD_Reference.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_Reference.Table_ID, reference.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA);
            sysMigracionLin.setName(reference.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Reference.Table_ID);
            sysMigracionLin.setRecord_ID(reference.get_ID());
            sysMigracionLin.setStartDate(reference.getUpdated());
            sysMigracionLin.setVersionNo(reference.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(reference.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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
                X_AD_Table table = new X_AD_Table(getCtx(), rs.getInt("ad_table_id"), null);
                this.setTablaLin(table, null, null, 0);
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
     * Setea linea de este documento con datos de una Tabla del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param table
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setTablaLin(X_AD_Table table, String parentType, String parentName, int parentID){

        try{
            if (table.getEntityType().equalsIgnoreCase(X_AD_Table.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_Table.Table_ID, table.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA);
            sysMigracionLin.setName(table.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Table.Table_ID);
            sysMigracionLin.setRecord_ID(table.get_ID());
            sysMigracionLin.setStartDate(table.getUpdated());
            sysMigracionLin.setVersionNo(table.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(table.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Recorro columnas de esta tabla para procesar objetos del diccionario asociados a las mismas
            List<MColumn> columnList = ((MTable) table).getColumnsAsList();
            for (MColumn column: columnList){

                // Procso elemento de la columna
                X_AD_Element element = (X_AD_Element) column.getAD_Element();
                this.setElementoLin(element, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA, table.getName(), table.get_ID());

                // Si tengo referencia asociada a esta columna la proceso
                if (column.getAD_Reference_Value_ID() > 0){
                    X_AD_Reference reference = new X_AD_Reference(getCtx(), column.getAD_Reference_Value_ID(), null);
                    this.setReferenciaLin(reference, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA, table.getName(), table.get_ID());
                }

                // Si tengo validacion asociada a esta columna la proceso
                if (column.getAD_Val_Rule_ID() > 0){
                    X_AD_Val_Rule valRule = new X_AD_Val_Rule(getCtx(), column.getAD_Val_Rule_ID(), null);
                    this.setValidacionLin(valRule, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA, table.getName(), table.get_ID());
                }

                // Si tengo proceso asociado a esta columna lo considero
                if (column.getAD_Process_ID() > 0){
                    X_AD_Process process = new X_AD_Process(getCtx(), column.getAD_Process_ID(), null);
                    this.setProcesoLin(process, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA, table.getName(), table.get_ID());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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
                X_AD_Process process = new X_AD_Process(getCtx(), rs.getInt("ad_process_id"), null);
                this.setProcesoLin(process, null, null, 0);
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
     * Setea linea de este documento con datos de un Proceso del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param process
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setProcesoLin(X_AD_Process process, String parentType, String parentName, int parentID){

        try{
            if (process.getEntityType().equalsIgnoreCase(X_AD_Process.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_Process.Table_ID, process.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO);
            sysMigracionLin.setName(process.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Process.Table_ID);
            sysMigracionLin.setRecord_ID(process.get_ID());
            sysMigracionLin.setStartDate(process.getUpdated());
            sysMigracionLin.setVersionNo(process.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(process.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Si tengo vista de informe asociada, la proceso
            if (process.getAD_ReportView_ID() > 0){
                X_AD_ReportView reportView = (X_AD_ReportView) process.getAD_ReportView();
                this.setVistaInformeLin(reportView, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO, process.getName(), process.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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
                MWindow window = new MWindow(getCtx(), rs.getInt("ad_window_id"), null);
                this.setVentanaLin(window, null, null, 0);
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
     * Setea linea de este documento con datos de un Elemento del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param window
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setVentanaLin(MWindow window, String parentType, String parentName, int parentID){

        try{
            if (window.getEntityType().equalsIgnoreCase(X_AD_Window.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_Window.Table_ID, window.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA);
            sysMigracionLin.setName(window.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Window.Table_ID);
            sysMigracionLin.setRecord_ID(window.get_ID());
            sysMigracionLin.setStartDate(window.getUpdated());
            sysMigracionLin.setVersionNo(window.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(window.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Obtengo y recorro pestañas asociadas a esta ventana
            MTab[] tabList = window.getTabs(false, null);
            for (int i = 0; i < tabList.length; i++){
                MTab tab = tabList[i];

                // Proceso Tabla asociada a esta pestaña
                X_AD_Table table = (X_AD_Table) tab.getAD_Table();
                this.setTablaLin(table, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA, window.getName(), window.get_ID());

                // Si tengo un proceso asociado a esta pestaña, lo considero
                if (tab.getAD_Process_ID() > 0){
                    X_AD_Process process = (X_AD_Process) tab.getAD_Process();
                    this.setProcesoLin(process, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA, window.getName(), window.get_ID());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Setea linea de este documento con datos de una Vista de Informe del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param reportView
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setVistaInformeLin(X_AD_ReportView reportView, String parentType, String parentName, int parentID){

        try{
            if (reportView.getEntityType().equalsIgnoreCase(X_AD_ReportView.ENTITYTYPE_Dictionary)){
                return;
            }
            if (this.existeTablaRecord(I_AD_ReportView.Table_ID, reportView.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REPORTVIEW);
            sysMigracionLin.setName(reportView.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_ReportView.Table_ID);
            sysMigracionLin.setRecord_ID(reportView.get_ID());
            sysMigracionLin.setStartDate(reportView.getUpdated());
            sysMigracionLin.setVersionNo(reportView.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(reportView.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Proceso Tabla asociada a esta vista de informe
            X_AD_Table table = (X_AD_Table) reportView.getAD_Table();
            this.setTablaLin(table, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REPORTVIEW, reportView.getName(), reportView.get_ID());
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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

    public String export(){

        String message = null;

        try{

            this.cabezalMigracion = new CabezalMigracion();

            // Exporto Referencias
            this.exportReferencias();

            // Exporto Elementos
            this.exportElementos();

            FileOutputStream os = new FileOutputStream("/tmp/" + "prueba.xml");
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(this.cabezalMigracion);
            encoder.close();

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    private void exportElementos() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_element_id " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Element.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

        	pstmt = DB.prepareStatement(sql, get_TrxName());
        	rs = pstmt.executeQuery();

        	while(rs.next()){
                ADElement element = new ADElement(getCtx(), rs.getInt("ad_element_id"), null);
                this.cabezalMigracion.getElementList().add(element);
        	}
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
        	rs = null; pstmt = null;
        }

    }

    private void exportReferencias() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_reference_id " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Reference.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADReference reference = new ADReference(getCtx(), rs.getInt("ad_reference_id"), null);
                this.cabezalMigracion.getReferenceList().add(reference);

                // Referencia de Lista
                for (ValueNamePair vp : MRefList.getList(getCtx(), reference.getAD_Reference_ID(), false)){
                    MRefList mRefList = MRefList.get(getCtx(), reference.getAD_Reference_ID(), vp.getValue(), null);
                    if (mRefList != null){
                        ADRef_List adRefList = new ADRef_List(getCtx(), mRefList.get_ID(), null);
                        if (adRefList != null){
                            this.cabezalMigracion.getRefListList().add(adRefList);
                        }
                    }
                }

                // Referencia de Tabla
                MRefTable mRefTable = MRefTable.getById(getCtx(), reference.getAD_Reference_ID());
                if (mRefTable != null){
                    ADRef_Table refTable = new ADRef_Table(getCtx(), mRefTable.get_ID(), null);
                    if (refTable != null){
                        this.cabezalMigracion.getRefTableList().add(refTable);
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

    }
}
