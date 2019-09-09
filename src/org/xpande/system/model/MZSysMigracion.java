package org.xpande.system.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.util.DB;
import org.compiere.util.ValueNamePair;
import org.xpande.system.migration.*;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
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
    public String getDataDB() {

        String message = null;
        try{

            // Elimino información anterior
            String action = " delete from z_sys_migracionlin where z_sys_migracion_id =" + this.get_ID();
            DB.executeUpdateEx(action, get_TrxName());

            // Armo condiciones de filtros
            this.setWhereClause();

            // Obtener elementos
            if (this.isMigElemento()){
                message = this.getElementosDB();
                if (message != null){
                    return message;
                }
            }
            // Obtener validaciones
            if (this.isMigValidacion()){
                message = this.getValidacionesDB();
                if (message != null){
                    return message;
                }
            }
            // Obtener referencias
            if (this.isMigReferencia()){
                message = this.getReferenciasDB();
                if (message != null){
                    return message;
                }
            }
            // Obtener tablas
            if (this.isMigTabla()){
                message = this.getTablasDB();
                if (message != null){
                    return message;
                }
            }
            // Obtener procesos
            if (this.isMigProceso()){
                message = this.getProcesosDB();
                if (message != null){
                    return message;
                }
            }
            // Obtener ventanas
            if (this.isMigVentana()){
                message = this.getVentanasDB();
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
    private String getElementosDB() {

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
    private String getValidacionesDB() {

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
    private String getReferenciasDB() {

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
    private String getTablasDB() {

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

            if (this.existeTablaRecord(I_AD_Table.Table_ID, table.get_ID())){
                return;
            }

            // Considero tabla aunque sea del diccionario para luego poder procesar las columnas agregadas que no son diccionario.
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

            // Recorro columnas de esta tabla para procesar objetos del diccionario asociados a las mismas.
            // Si la tabla es diccionario, considero solo las que no son del diccionario.
            List<MColumn> columnList = ((MTable) table).getColumnsAsList();
            for (MColumn column: columnList){

                if (!column.getEntityType().equalsIgnoreCase(X_AD_Column.ENTITYTYPE_Dictionary)){
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
    private String getProcesosDB() {

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
    private String getVentanasDB() {

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

                // Si la pestaña no esta activa, no la considero
                if (!tab.isActive()){
                    continue;
                }

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

    /***
     * Exporta objetos del diccionario seleccionados a archivo de interface.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     * @return
     */
    public String export(){

        String message = null;

        try{

            if ((this.getFilePathOrName() == null) || (this.getFilePathOrName().trim().equalsIgnoreCase(""))){
                return "Debe indicar Archivo Destino";
            }

            this.cabezalMigracion = new CabezalMigracion();

            // Exporto Validaciones
            this.exportValidaciones();

            // Exporto Referencias
            this.exportReferencias();

            // Exporto Elementos
            this.exportElementos();

            // Exporto Tablas
            this.exportTablas();

            // Exporto Procesos
            this.exportProcesos();

            // Exporto Ventanas
            this.exportVentanas();

            // Genero archivo de interface
            //FileOutputStream os = new FileOutputStream("/tmp/" + "prueba.xml");
            FileOutputStream os = new FileOutputStream(this.getFilePathOrName());
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(this.cabezalMigracion);
            encoder.close();

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }


    /***
     * Agrega validaciones seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     */
    private void exportValidaciones() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_val_rule_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Val_Rule.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADVal_Rule valRule = new ADVal_Rule(getCtx(), rs.getInt("ad_val_rule_id"), null);
                valRule.setParentType(rs.getString("TipoSysMigraObjFrom"));
                valRule.setParentName(rs.getString("parentName"));
                valRule.setParentID(rs.getInt("parent_ID"));

                this.cabezalMigracion.getValRuleList().add(valRule);
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


    /***
     * Agrega elementos seleccionados para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     */
    private void exportElementos() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_element_id, TipoSysMigraObjFrom, parentName, parent_ID  " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Element.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

        	pstmt = DB.prepareStatement(sql, get_TrxName());
        	rs = pstmt.executeQuery();

        	while(rs.next()){
                ADElement element = new ADElement(getCtx(), rs.getInt("ad_element_id"), null);
                element.setParentType(rs.getString("TipoSysMigraObjFrom"));
                element.setParentName(rs.getString("parentName"));
                element.setParentID(rs.getInt("parent_ID"));

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

    /***
     * Agrega referencias seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     */
    private void exportReferencias() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_reference_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Reference.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADReference reference = new ADReference(getCtx(), rs.getInt("ad_reference_id"), null);
                reference.setParentType(rs.getString("TipoSysMigraObjFrom"));
                reference.setParentName(rs.getString("parentName"));
                reference.setParentID(rs.getInt("parent_ID"));

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

    /***
     * Agrega Tablas seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 9/2/19.
     */
    private void exportTablas() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_table_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Table.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                MTable table = new MTable(getCtx(), rs.getInt("ad_table_id"), null);

                // Exporto esta tabla si no es tipo de entidad Diccionario
                if (!table.getEntityType().equalsIgnoreCase(X_AD_Table.ENTITYTYPE_Dictionary)){
                    ADTable adTable = new ADTable(getCtx(), rs.getInt("ad_table_id"), null);
                    adTable.setParentType(rs.getString("TipoSysMigraObjFrom"));
                    adTable.setParentName(rs.getString("parentName"));
                    adTable.setParentID(rs.getInt("parent_ID"));

                    this.cabezalMigracion.getTableList().add(adTable);
                }

                // Recorro columnas de esta tabla para exportar (en caso que la tabla sea diccionario, solo importo las columnas no diccionario
                List<MColumn> columnList = table.getColumnsAsList();
                for (MColumn column: columnList){
                    if (!table.getEntityType().equalsIgnoreCase(X_AD_Table.ENTITYTYPE_Dictionary)){
                        ADColumn adColumn = new ADColumn(getCtx(), column.get_ID(), null);
                        this.cabezalMigracion.getColumnList().add(adColumn);
                    }
                    else {
                        if (!column.getEntityType().equalsIgnoreCase(X_AD_Column.ENTITYTYPE_Dictionary)){
                            ADColumn adColumn = new ADColumn(getCtx(), column.get_ID(), null);
                            this.cabezalMigracion.getColumnList().add(adColumn);
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

    }

    /***
     * Agrega Procesos seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 9/2/19.
     */
    private void exportProcesos() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_process_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Process.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADProcess adProcess = new ADProcess(getCtx(), rs.getInt("ad_process_id"), null);
                adProcess.setParentType(rs.getString("TipoSysMigraObjFrom"));
                adProcess.setParentName(rs.getString("parentName"));
                adProcess.setParentID(rs.getInt("parent_ID"));

                this.cabezalMigracion.getProcessList().add(adProcess);

                // Recorro parametros de este proceso para exportar
                MProcess process = new MProcess(getCtx(), rs.getInt("ad_process_id"), null);
                MProcessPara[] processParaList = process.getParameters();
                for (int i = 0; i < processParaList.length; i++){
                    ADProcessPara adProcessPara = new ADProcessPara(getCtx(), processParaList[i].get_ID(), null);
                    this.cabezalMigracion.getProcessParaList().add(adProcessPara);
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

    /***
     * Agrega Ventanas seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 9/2/19.
     */
    private void exportVentanas(){

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_window_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Window.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADWindow adWindow = new ADWindow(getCtx(), rs.getInt("ad_window_id"), null);
                this.cabezalMigracion.getWindowList().add(adWindow);

                // Recorro Tabs de este proceso para exportar
                MWindow window = new MWindow(getCtx(), rs.getInt("ad_window_id"), null);
                MTab[] tabList = window.getTabs(false, null);
                for (int i = 0; i < tabList.length; i++){
                    ADTab adTab = new ADTab(getCtx(), tabList[i].get_ID(), null);
                    this.cabezalMigracion.getTabList().add(adTab);

                    // Recorro Fields de esta tab para exportar
                    MField[] fieldList = tabList[i].getFields(false, null);
                    for (int j = 0; j < fieldList.length; j++){
                        ADField adField = new ADField(getCtx(), fieldList[j].get_ID(), null);
                        this.cabezalMigracion.getFieldList().add(adField);
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

    public String getDataFile() {

        String message = null;

        try{

            if ((this.getFilePathOrName() == null) || (this.getFilePathOrName().trim().equalsIgnoreCase(""))){
                return "Debe indicar archivo a procesar.";
            }

            this.cabezalMigracion = null;

            // Deserializo objeto contenido en el archivo de interface
            Object value = null;
            FileInputStream os1 = new FileInputStream(this.getFilePathOrName().trim());
            XMLDecoder decoder = new XMLDecoder(os1);
            value = decoder.readObject();
            decoder.close();

            if (value == null){
                return "No se pudo obtener información desde archivo indicado.";
            }

            // Obtengo modelo con la información contenido en el archivo
            this.cabezalMigracion = (CabezalMigracion) value;

            // Recorro objento contenidos en el modelo de datos y los cargo en las lineas de migracion

            // Obtener elementos desde archivo
            message = this.getElementosFile();
            if (message != null){
                return message;
            }

            // Obtener validaciones
            message = this.getValidacionesFile();
            if (message != null){
                return message;
            }

            // Obtener referencias
            message = this.getReferenciasFile();
            if (message != null){
                return message;
            }

            // Obtener tablas
            message = this.getTablasFile();
            if (message != null){
                return message;
            }

            // Obtener procesos
            message = this.getProcesosFile();
            if (message != null){
                return message;
            }

            // Obtener ventanas
            message = this.getVentanasFile();
            if (message != null){
                return message;
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo elementos del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getElementosFile() {

        String message = null;

        try{
            for (ADElement adElement: this.cabezalMigracion.getElementList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO);
                sysMigracionLin.setName(adElement.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Element.Table_ID);
                sysMigracionLin.setRecord_ID(adElement.get_ID());
                sysMigracionLin.setStartDate(adElement.getUpdated());
                sysMigracionLin.setVersionNo(adElement.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adElement.getEntityType());
                sysMigracionLin.setIsSelected(true);

                if (adElement.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adElement.getParentType());
                if (adElement.getParentName() != null) sysMigracionLin.setParentName(adElement.getParentName());
                if (adElement.getParentID() > 0) sysMigracionLin.setParent_ID(adElement.getParentID());

                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo procesos del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getProcesosFile() {

        String message = null;

        try{
            for (ADProcess adProcess: this.cabezalMigracion.getProcessList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO);
                sysMigracionLin.setName(adProcess.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Process.Table_ID);
                sysMigracionLin.setRecord_ID(adProcess.get_ID());
                sysMigracionLin.setStartDate(adProcess.getUpdated());
                sysMigracionLin.setVersionNo(adProcess.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adProcess.getEntityType());
                sysMigracionLin.setIsSelected(true);

                if (adProcess.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adProcess.getParentType());
                if (adProcess.getParentName() != null) sysMigracionLin.setParentName(adProcess.getParentName());
                if (adProcess.getParentID() > 0) sysMigracionLin.setParent_ID(adProcess.getParentID());

                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo referencias del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getReferenciasFile() {

        String message = null;

        try{
            for (ADReference adReference: this.cabezalMigracion.getReferenceList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA);
                sysMigracionLin.setName(adReference.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Reference.Table_ID);
                sysMigracionLin.setRecord_ID(adReference.get_ID());
                sysMigracionLin.setStartDate(adReference.getUpdated());
                sysMigracionLin.setVersionNo(adReference.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adReference.getEntityType());
                sysMigracionLin.setIsSelected(true);

                if (adReference.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adReference.getParentType());
                if (adReference.getParentName() != null) sysMigracionLin.setParentName(adReference.getParentName());
                if (adReference.getParentID() > 0) sysMigracionLin.setParent_ID(adReference.getParentID());

                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo tablas del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getTablasFile() {

        String message = null;

        try{
            for (ADTable adTable: this.cabezalMigracion.getTableList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA);
                sysMigracionLin.setName(adTable.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Table.Table_ID);
                sysMigracionLin.setRecord_ID(adTable.get_ID());
                sysMigracionLin.setStartDate(adTable.getUpdated());
                sysMigracionLin.setVersionNo(adTable.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adTable.getEntityType());
                sysMigracionLin.setIsSelected(true);

                if (adTable.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adTable.getParentType());
                if (adTable.getParentName() != null) sysMigracionLin.setParentName(adTable.getParentName());
                if (adTable.getParentID() > 0) sysMigracionLin.setParent_ID(adTable.getParentID());

                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo validaciones del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getValidacionesFile() {

        String message = null;

        try{
            for (ADVal_Rule adValRule: this.cabezalMigracion.getValRuleList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VALIDACION);
                sysMigracionLin.setName(adValRule.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Val_Rule.Table_ID);
                sysMigracionLin.setRecord_ID(adValRule.get_ID());
                sysMigracionLin.setStartDate(adValRule.getUpdated());
                sysMigracionLin.setVersionNo(adValRule.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adValRule.getEntityType());
                sysMigracionLin.setIsSelected(true);

                if (adValRule.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adValRule.getParentType());
                if (adValRule.getParentName() != null) sysMigracionLin.setParentName(adValRule.getParentName());
                if (adValRule.getParentID() > 0) sysMigracionLin.setParent_ID(adValRule.getParentID());

                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo ventanas del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getVentanasFile() {

        String message = null;

        try{
            for (ADWindow adWindow: this.cabezalMigracion.getWindowList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA);
                sysMigracionLin.setName(adWindow.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Window.Table_ID);
                sysMigracionLin.setRecord_ID(adWindow.get_ID());
                sysMigracionLin.setStartDate(adWindow.getUpdated());
                sysMigracionLin.setVersionNo(adWindow.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adWindow.getEntityType());
                sysMigracionLin.setIsSelected(true);

                sysMigracionLin.saveEx();
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

}
