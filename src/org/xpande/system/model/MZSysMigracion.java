package org.xpande.system.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.util.DB;
import org.compiere.util.ValueNamePair;
import org.xpande.system.migration.*;

import javax.el.MapELResolver;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<Integer, Integer> hashValidaciones = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashReferencias = new HashMap<Integer, Integer>();

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

            if (!this.isDictionary()){
                whereClause += " and a.entitytype !='D' ";
            }

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
                    " where a.isactive ='Y' " + whereClause +
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
            if (!this.isDictionary()){
                if (element.getEntityType().equalsIgnoreCase(X_AD_Element.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Element.Table_ID, element.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO);
            sysMigracionLin.setName(element.getColumnName());
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
                this.setReferenciaLin(reference, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO, element.getColumnName(), element.get_ID());
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Setea linea de este documento con datos de una Columna del diccionario.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     * @param element
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setColumnaLin(X_AD_Column column, String parentType, String parentName, int parentID){

        try{

            if (!this.isDictionary()){
                if (column.getEntityType().equalsIgnoreCase(X_AD_Column.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Column.Table_ID, column.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA);
            sysMigracionLin.setName(column.getColumnName());
            sysMigracionLin.setAD_Table_ID(I_AD_Column.Table_ID);
            sysMigracionLin.setRecord_ID(column.get_ID());
            sysMigracionLin.setStartDate(column.getUpdated());
            sysMigracionLin.setVersionNo(column.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(column.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            if ((this.isDictionary()) || (!column.getEntityType().equalsIgnoreCase(X_AD_Column.ENTITYTYPE_Dictionary))){

                // Proceso elemento de la columna
                X_AD_Element element = (X_AD_Element) column.getAD_Element();
                this.setElementoLin(element, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA, column.getColumnName(), column.get_ID());

                // Si tengo referencia asociada a esta columna la proceso
                if (column.getAD_Reference_Value_ID() > 0){
                    X_AD_Reference reference = new X_AD_Reference(getCtx(), column.getAD_Reference_Value_ID(), null);
                    this.setReferenciaLin(reference, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA, column.getColumnName(), column.get_ID());
                }

                // Si tengo validacion asociada a esta columna la proceso
                if (column.getAD_Val_Rule_ID() > 0){
                    X_AD_Val_Rule valRule = new X_AD_Val_Rule(getCtx(), column.getAD_Val_Rule_ID(), null);
                    this.setValidacionLin(valRule, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA, column.getColumnName(), column.get_ID());
                }

                // Si tengo proceso asociado a esta columna lo considero (no considero la columna DocAction)
                if (!column.getColumnName().equalsIgnoreCase("DocAction")){
                    if (column.getAD_Process_ID() > 0){
                        X_AD_Process process = new X_AD_Process(getCtx(), column.getAD_Process_ID(), null);
                        this.setProcesoLin(process, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA, column.getColumnName(), column.get_ID());
                    }
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Setea linea de este documento con datos de un Field del diccionario.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     * @param field
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setFieldLin(X_AD_Field field, String parentType, String parentName, int parentID){

        try{
            if (!this.isDictionary()){
                if (field.getEntityType().equalsIgnoreCase(X_AD_Field.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Field.Table_ID, field.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD);
            sysMigracionLin.setName(field.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Field.Table_ID);
            sysMigracionLin.setRecord_ID(field.get_ID());
            sysMigracionLin.setStartDate(field.getUpdated());
            sysMigracionLin.setVersionNo(field.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(field.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Si tengo referencia asociada a este field la proceso
            if (field.getAD_Reference_Value_ID() > 0){
                X_AD_Reference reference = new X_AD_Reference(getCtx(), field.getAD_Reference_Value_ID(), null);
                this.setReferenciaLin(reference, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD, field.getName(), field.get_ID());
            }

            // Si tengo validacion asociada a este field la proceso
            if (field.getAD_Val_Rule_ID() > 0){
                X_AD_Val_Rule valRule = new X_AD_Val_Rule(getCtx(), field.getAD_Val_Rule_ID(), null);
                this.setValidacionLin(valRule, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD, field.getName(), field.get_ID());
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
                    " where a.isactive ='Y' " + whereClause +
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

            if (!this.isDictionary()){
                if (valRule.getEntityType().equalsIgnoreCase(X_AD_Val_Rule.ENTITYTYPE_Dictionary)){
                    return;
                }
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
            sysMigracionLin.setExisteItem(false);

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
                    " where a.isactive ='Y' " + whereClause +
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

            if (!this.isDictionary()){
                if (reference.getEntityType().equalsIgnoreCase(X_AD_Reference.ENTITYTYPE_Dictionary)){
                    return;
                }
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

            // Referencia de Lista
            for (ValueNamePair vp : MRefList.getList(getCtx(), reference.getAD_Reference_ID(), false)){
                MRefList mRefList = MRefList.get(getCtx(), reference.getAD_Reference_ID(), vp.getValue(), null);
                if (mRefList != null){
                    ADRef_List adRefList = new ADRef_List(getCtx(), mRefList.get_ID(), null);
                    if (adRefList != null){
                        X_AD_Ref_List refList = new X_AD_Ref_List(getCtx(), adRefList.get_ID(), null);
                        this.setReferenciaListLin(refList, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA, reference.getName(), reference.get_ID());
                    }
                }
            }

            // Referencia de Tabla
            MRefTable mRefTable = MRefTable.getById(getCtx(), reference.getAD_Reference_ID());
            if (mRefTable != null){
                ADRef_Table adRefTable = new ADRef_Table(getCtx(), mRefTable.get_ID(), null);
                if (adRefTable != null){
                    X_AD_Ref_Table refTable = new X_AD_Ref_Table(getCtx(), adRefTable.get_ID(), null);
                    this.setReferenciaTableLin(refTable, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA, reference.getName(), reference.get_ID());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Setea linea de este documento con datos de una Referencia Lista del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param reference
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setReferenciaListLin(X_AD_Ref_List reference, String parentType, String parentName, int parentID){

        try{

            if (!this.isDictionary()){
                if (reference.getEntityType().equalsIgnoreCase(X_AD_Reference.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Ref_List.Table_ID, reference.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REF_LISTA);
            sysMigracionLin.setName(reference.getValue());
            sysMigracionLin.setAD_Table_ID(I_AD_Ref_List.Table_ID);
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
     * Setea linea de este documento con datos de una Referencia Tabla del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param reference
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setReferenciaTableLin(X_AD_Ref_Table reference, String parentType, String parentName, int parentID){

        try{

            if (!this.isDictionary()){
                if (reference.getEntityType().equalsIgnoreCase(X_AD_Reference.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Ref_Table.Table_ID, reference.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REF_TABLA);
            sysMigracionLin.setName(String.valueOf(reference.getAD_Table_ID()));
            sysMigracionLin.setAD_Table_ID(I_AD_Ref_Table.Table_ID);
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
                    " where a.isactive ='Y' " + whereClause +
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

            if (!this.isDictionary()){
                if (table.getEntityType().equalsIgnoreCase(X_AD_Process.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

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

            // Obtengo y Recorro columnas de esta tabla
            MTable mTable = new MTable(getCtx(), table.get_ID(), get_TrxName());
            List<MColumn> columnList = mTable.getColumnsAsList();
            for (MColumn column: columnList){
                // Agrego linea de proceso para esta columna
                this.setColumnaLin(column, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA, table.getName(), table.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Setea linea de este documento con datos de una Pestaña del diccionario.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     * @param tab
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setTabLin(X_AD_Tab tab, String parentType, String parentName, int parentID){

        try{

            if (!this.isDictionary()){
                if (tab.getEntityType().equalsIgnoreCase(X_AD_Process.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Tab.Table_ID, tab.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA);
            sysMigracionLin.setName(tab.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Tab.Table_ID);
            sysMigracionLin.setRecord_ID(tab.get_ID());
            sysMigracionLin.setStartDate(tab.getUpdated());
            sysMigracionLin.setVersionNo(tab.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(tab.getEntityType());
            sysMigracionLin.setIsSelected(true);

            if (parentType != null) sysMigracionLin.setTipoSysMigraObjFrom(parentType);
            if (parentName != null) sysMigracionLin.setParentName(parentName);
            if (parentID > 0) sysMigracionLin.setParent_ID(parentID);

            sysMigracionLin.saveEx();

            // Proceso Tabla asociada a esta pestaña
            X_AD_Table table = (X_AD_Table) tab.getAD_Table();
            this.setTablaLin(table, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA, tab.getName(), tab.get_ID());

            // Si tengo un proceso asociado a esta pestaña, lo considero
            if (tab.getAD_Process_ID() > 0){
                X_AD_Process process = (X_AD_Process) tab.getAD_Process();
                this.setProcesoLin(process, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA, tab.getName(), tab.get_ID());
            }

            // Obtengo y recorro fields de esta tab
            MTab mTab = new MTab(getCtx(), tab.get_ID(), get_TrxName());
            MField[] fieldList = mTab.getFields(false, null);
            for (int j = 0; j < fieldList.length; j++){

                MField field = fieldList[j];

                // Seteo linea de proceso para este Field
                this.setFieldLin(field, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA, tab.getName(), tab.get_ID());
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
                    " where a.isactive ='Y' " + whereClause +
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
            if (!this.isDictionary()){
                if (process.getEntityType().equalsIgnoreCase(X_AD_Process.ENTITYTYPE_Dictionary)){
                    return;
                }
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

            // Recorro parametros de este proceso
            MProcess mProcess = new MProcess(getCtx(), process.get_ID(), null);
            MProcessPara[] processParaList = mProcess.getParameters();
            for (int i = 0; i < processParaList.length; i++){
                this.setProcesoParaLin(processParaList[i], X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO, process.getName(), process.get_ID());
            }

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
     * Setea linea de este documento con datos de un Parametro de Proceso del diccionario.
     * Xpande. Created by Gabriel Vila on 8/24/19.
     * @param processPara
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setProcesoParaLin(X_AD_Process_Para processPara, String parentType, String parentName, int parentID){

        try{

            if (!this.isDictionary()){
                if (processPara.getEntityType().equalsIgnoreCase(X_AD_Process.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_Process_Para.Table_ID, processPara.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM);
            sysMigracionLin.setName(processPara.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_Process_Para.Table_ID);
            sysMigracionLin.setRecord_ID(processPara.get_ID());
            sysMigracionLin.setStartDate(processPara.getUpdated());
            sysMigracionLin.setVersionNo(processPara.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(processPara.getEntityType());
            sysMigracionLin.setIsSelected(true);
            sysMigracionLin.setExisteItem(false);

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
                    " where a.isactive ='Y' " + whereClause +
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

            if (!this.isDictionary()){
                if (window.getEntityType().equalsIgnoreCase(X_AD_Window.ENTITYTYPE_Dictionary)){
                    return;
                }
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

                // Seteo linea de proceso para esta pestaña
                this.setTabLin(tab, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA, window.getName(), window.get_ID());
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

            if (!this.isDictionary()){
                if (reportView.getEntityType().equalsIgnoreCase(X_AD_ReportView.ENTITYTYPE_Dictionary)){
                    return;
                }
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
            sysMigracionLin.setExisteItem(false);

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
    public String exportData(){

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

            // Exporto Referencias Lista
            this.exportReferenciasLista();

            // Exporto Referencias Tabla
            this.exportReferenciasTabla();

            // Exporto Elementos
            this.exportElementos();

            // Exporto Tablas
            this.exportTablas();

            // Exporto Columnas
            this.exportColumnas();

            // Exporto Procesos
            this.exportProcesos();

            // Exporto Parametros de Procesos
            this.exportProcesosParam();

            // Export vistas de informes
            this.exportVistaInformes();

            // Exporto Ventanas
            this.exportVentanas();

            // Exporto Pestañás
            this.exportTabs();

            // Exporto Fields
            this.exportFields();

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
     * Agrega vistas de informes seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     */
    private void exportVistaInformes() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_reportview_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_ReportView.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADReportView reportView = new ADReportView(getCtx(), rs.getInt("ad_reportview_id"), null);
                reportView.setParentType(rs.getString("TipoSysMigraObjFrom"));
                reportView.setParentName(rs.getString("parentName"));
                reportView.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionList = this.getTraducciones(reportView.Table_Name, reportView.get_ID(), "es_MX");
                reportView.setTraduccionList(traduccionList);

                this.cabezalMigracion.getReportViewList().add(reportView);
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

                List<Traduccion> traduccionList = this.getTraducciones(element.Table_Name, element.get_ID(), "es_MX");
                element.setTraduccionList(traduccionList);

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
     * Agrega columnas seleccionados para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     */
    private void exportColumnas() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_column_id, TipoSysMigraObjFrom, parentName, parent_ID  " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Column.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADColumn column = new ADColumn(getCtx(), rs.getInt("ad_column_id"), null);
                column.setParentType(rs.getString("TipoSysMigraObjFrom"));
                column.setParentName(rs.getString("parentName"));
                column.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionList = this.getTraducciones(column.Table_Name, column.get_ID(), "es_MX");
                column.setTraduccionList(traduccionList);

                this.cabezalMigracion.getColumnList().add(column);
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
     * Agrega pestañas seleccionados para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     */
    private void exportTabs() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_tab_id, TipoSysMigraObjFrom, parentName, parent_ID  " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Tab.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADTab tab = new ADTab(getCtx(), rs.getInt("ad_tab_id"), null);
                tab.setParentType(rs.getString("TipoSysMigraObjFrom"));
                tab.setParentName(rs.getString("parentName"));
                tab.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionList = this.getTraducciones(tab.Table_Name, tab.get_ID(), "es_MX");
                tab.setTraduccionList(traduccionList);

                this.cabezalMigracion.getTabList().add(tab);
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
     * Agrega fields seleccionados para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     */
    private void exportFields() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_field_id, TipoSysMigraObjFrom, parentName, parent_ID  " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Field.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADField field = new ADField(getCtx(), rs.getInt("ad_field_id"), null);
                field.setParentType(rs.getString("TipoSysMigraObjFrom"));
                field.setParentName(rs.getString("parentName"));
                field.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionList = this.getTraducciones(field.Table_Name, field.get_ID(), "es_MX");
                field.setTraduccionList(traduccionList);

                this.cabezalMigracion.getFieldList().add(field);
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

                List<Traduccion> traduccionList = this.getTraducciones(reference.Table_Name, reference.get_ID(), "es_MX");
                reference.setTraduccionList(traduccionList);

                this.cabezalMigracion.getReferenceList().add(reference);
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
     * Agrega referencias de lista seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     */
    private void exportReferenciasLista() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_ref_list_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Ref_List.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADRef_List adRefList = new ADRef_List(getCtx(), rs.getInt("ad_ref_list_id"), null);
                adRefList.setParentType(rs.getString("TipoSysMigraObjFrom"));
                adRefList.setParentName(rs.getString("parentName"));
                adRefList.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionRefList = this.getTraducciones(adRefList.Table_Name, adRefList.get_ID(), "es_MX");
                adRefList.setTraduccionList(traduccionRefList);

                this.cabezalMigracion.getRefListList().add(adRefList);
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
     * Agrega referencias de tabla seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 8/29/19.
     */
    private void exportReferenciasTabla() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_ref_table_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Ref_Table.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADRef_Table adRefTable = new ADRef_Table(getCtx(), rs.getInt("ad_ref_table_id"), null);
                adRefTable.setParentType(rs.getString("TipoSysMigraObjFrom"));
                adRefTable.setParentName(rs.getString("parentName"));
                adRefTable.setParentID(rs.getInt("parent_ID"));

                this.cabezalMigracion.getRefTableList().add(adRefTable);
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
     * Obtiene y retorna lista de traducciones para determinado registro-tabla.
     * Xpande. Created by Gabriel Vila on 9/9/19.
     * @param tableName
     * @param recordID
     * @return
     */
    private List<Traduccion> getTraducciones(String tableName, int recordID, String adLanguage) {

        List<Traduccion> traduccionList = new ArrayList<Traduccion>();

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{

            // Name
            if ((tableName.equalsIgnoreCase(X_AD_Column.Table_Name)) || (tableName.equalsIgnoreCase(X_AD_FieldGroup.Table_Name))
                || (tableName.equalsIgnoreCase(X_AD_Table.Table_Name))){

                sql = " select name, ad_language, created, createdby, updated, updatedby, istranslated " +
                        " from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + adLanguage + "'";

                pstmt = DB.prepareStatement(sql, get_TrxName());
                rs = pstmt.executeQuery();

                while(rs.next()){
                    Traduccion  traduccion = new Traduccion();
                    traduccion.setName(rs.getString("name"));
                    traduccion.setCreated(rs.getTimestamp("created"));
                    traduccion.setUpdated(rs.getTimestamp("updated"));
                    traduccion.setCreatedBy(rs.getInt("createdby"));
                    traduccion.setUpdatedBy(rs.getInt("updatedby"));
                    traduccion.setLanguage(rs.getString("ad_language"));
                    traduccion.setIsTranslated(rs.getString("istranslated"));
                    traduccionList.add(traduccion);
                }
            }

            // Name, Description
            if (tableName.equalsIgnoreCase(X_AD_Ref_List.Table_Name)){

                sql = " select name, description, ad_language, created, createdby, updated, updatedby, istranslated " +
                        " from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + adLanguage + "'";

                pstmt = DB.prepareStatement(sql, get_TrxName());
                rs = pstmt.executeQuery();

                while(rs.next()){
                    Traduccion  traduccion = new Traduccion();
                    traduccion.setName(rs.getString("name"));
                    traduccion.setDescription(rs.getString("description"));
                    traduccion.setCreated(rs.getTimestamp("created"));
                    traduccion.setUpdated(rs.getTimestamp("updated"));
                    traduccion.setCreatedBy(rs.getInt("createdby"));
                    traduccion.setUpdatedBy(rs.getInt("updatedby"));
                    traduccion.setLanguage(rs.getString("ad_language"));
                    traduccion.setIsTranslated(rs.getString("istranslated"));
                    traduccionList.add(traduccion);
                }
            }

            // Name, Description, Help
            if ((tableName.equalsIgnoreCase(X_AD_Window.Table_Name)) || (tableName.equalsIgnoreCase(X_AD_Field.Table_Name))
                    || (tableName.equalsIgnoreCase(X_AD_Process.Table_Name)) || (tableName.equalsIgnoreCase(X_AD_Process_Para.Table_Name))
                    || (tableName.equalsIgnoreCase(X_AD_Reference.Table_Name))){

                sql = " select name, description, help, ad_language, created, createdby, updated, updatedby, istranslated " +
                        " from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + adLanguage + "'";

                pstmt = DB.prepareStatement(sql, get_TrxName());
                rs = pstmt.executeQuery();

                while(rs.next()){
                    Traduccion  traduccion = new Traduccion();
                    traduccion.setName(rs.getString("name"));
                    traduccion.setDescription(rs.getString("description"));
                    traduccion.setHelp(rs.getString("help"));
                    traduccion.setCreated(rs.getTimestamp("created"));
                    traduccion.setUpdated(rs.getTimestamp("updated"));
                    traduccion.setCreatedBy(rs.getInt("createdby"));
                    traduccion.setUpdatedBy(rs.getInt("updatedby"));
                    traduccion.setLanguage(rs.getString("ad_language"));
                    traduccion.setIsTranslated(rs.getString("istranslated"));
                    traduccionList.add(traduccion);
                }
            }

            // Name, PrintName, Description, Help, PoNAme, PoDescription, PoHelp, PoPrintName
            if (tableName.equalsIgnoreCase(X_AD_Element.Table_Name)){

                sql = " select name, description, help, printname, po_name, po_description, po_help, po_printname, " +
                        " ad_language, created, createdby, updated, updatedby, istranslated " +
                        " from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + adLanguage + "'";

                pstmt = DB.prepareStatement(sql, get_TrxName());
                rs = pstmt.executeQuery();

                while(rs.next()){
                    Traduccion  traduccion = new Traduccion();
                    traduccion.setName(rs.getString("name"));
                    traduccion.setDescription(rs.getString("description"));
                    traduccion.setHelp(rs.getString("help"));
                    traduccion.setPrintName(rs.getString("printname"));
                    traduccion.setPoName(rs.getString("po_name"));
                    traduccion.setPoDescription(rs.getString("po_description"));
                    traduccion.setPoHelp(rs.getString("po_help"));
                    traduccion.setPoPrintName(rs.getString("po_printname"));
                    traduccion.setCreated(rs.getTimestamp("created"));
                    traduccion.setUpdated(rs.getTimestamp("updated"));
                    traduccion.setCreatedBy(rs.getInt("createdby"));
                    traduccion.setUpdatedBy(rs.getInt("updatedby"));
                    traduccion.setLanguage(rs.getString("ad_language"));
                    traduccion.setIsTranslated(rs.getString("istranslated"));
                    traduccionList.add(traduccion);
                }
            }

            // Name, Description, PrintName
            if (tableName.equalsIgnoreCase(X_AD_ReportView.Table_Name)){

                sql = " select name, description, printname, ad_language, created, createdby, updated, updatedby, istranslated " +
                        " from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + adLanguage + "'";

                pstmt = DB.prepareStatement(sql, get_TrxName());
                rs = pstmt.executeQuery();

                while(rs.next()){
                    Traduccion  traduccion = new Traduccion();
                    traduccion.setName(rs.getString("name"));
                    traduccion.setDescription(rs.getString("description"));
                    traduccion.setPrintName(rs.getString("printname"));
                    traduccion.setCreated(rs.getTimestamp("created"));
                    traduccion.setUpdated(rs.getTimestamp("updated"));
                    traduccion.setCreatedBy(rs.getInt("createdby"));
                    traduccion.setUpdatedBy(rs.getInt("updatedby"));
                    traduccion.setLanguage(rs.getString("ad_language"));
                    traduccion.setIsTranslated(rs.getString("istranslated"));
                    traduccionList.add(traduccion);
                }
            }

            // Name, Description, Help, CommitWarning
            if (tableName.equalsIgnoreCase(X_AD_Tab.Table_Name)){

                sql = " select name, description, help, commitwarning, ad_language, created, createdby, updated, updatedby, istranslated " +
                        " from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + adLanguage + "'";

                pstmt = DB.prepareStatement(sql, get_TrxName());
                rs = pstmt.executeQuery();

                while(rs.next()){
                    Traduccion  traduccion = new Traduccion();
                    traduccion.setName(rs.getString("name"));
                    traduccion.setDescription(rs.getString("description"));
                    traduccion.setHelp(rs.getString("help"));
                    traduccion.setCommitWarning(rs.getString("commitwarning"));
                    traduccion.setCreated(rs.getTimestamp("created"));
                    traduccion.setUpdated(rs.getTimestamp("updated"));
                    traduccion.setCreatedBy(rs.getInt("createdby"));
                    traduccion.setUpdatedBy(rs.getInt("updatedby"));
                    traduccion.setLanguage(rs.getString("ad_language"));
                    traduccion.setIsTranslated(rs.getString("istranslated"));
                    traduccionList.add(traduccion);
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

        return traduccionList;
    }

    /***
     * Importa traducciones para determinado registro-tabla.
     * Xpande. Created by Gabriel Vila on 10/31/19.
     * @param tableName
     * @param recordID
     * @return
     */
    private void importTraducciones(String tableName, int recordID, List<Traduccion> traduccionList) {

        String action = "";

        try{
            // Para cada traducción recibida
            for (Traduccion traduccion: traduccionList){

                // Elimino anterior
                action = " delete from " + tableName + "_trl " +
                        " where " + tableName + "_id =" + recordID +
                        " and ad_language ='" + traduccion.getLanguage() + "'";
                DB.executeUpdateEx(action, get_TrxName());

                // Cargo nueva segun tabla
                action = " insert into " + tableName + "_trl (" + tableName + "_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby, istranslated, ad_language, ";

                // Name
                if ((tableName.equalsIgnoreCase(X_AD_Column.Table_Name)) || (tableName.equalsIgnoreCase(X_AD_FieldGroup.Table_Name))
                        || (tableName.equalsIgnoreCase(X_AD_Table.Table_Name))){

                    action += "name) " +
                            " VALUES (" + recordID + ", 0, 0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + "') ";
                }

                // Name, Description
                if (tableName.equalsIgnoreCase(X_AD_Ref_List.Table_Name)){

                    action += "name, description) " +
                            " VALUES (" + recordID + ", 0,0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null" : "', '" + traduccion.getDescription() + "'") + ") ";

                }

                // Name, Description, Help
                if ((tableName.equalsIgnoreCase(X_AD_Window.Table_Name)) || (tableName.equalsIgnoreCase(X_AD_Field.Table_Name))
                        || (tableName.equalsIgnoreCase(X_AD_Process.Table_Name)) || (tableName.equalsIgnoreCase(X_AD_Process_Para.Table_Name))
                        || (tableName.equalsIgnoreCase(X_AD_Reference.Table_Name))){

                    action += "name, description, help) " +
                            " VALUES (" + recordID + ", 0,0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null, " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getHelp() == null) ? ", null" : ", '" + traduccion.getHelp() + "'") + ") ";
                }

                // Name, PrintName, Description, Help, PoNAme, PoDescription, PoHelp, PoPrintName
                if (tableName.equalsIgnoreCase(X_AD_Element.Table_Name)){

                    action += "name, description, help, printname, po_name, po_description, po_help, po_printname) " +
                            " VALUES (" + recordID + ", 0,0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null, " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getHelp() == null) ? ", null, " : ", '" + traduccion.getHelp() + "'") +
                            ((traduccion.getPrintName() == null) ? ", null, " : ", '" + traduccion.getPrintName() + "'") +
                            ((traduccion.getPoName() == null) ? ", null, " : ", '" + traduccion.getPoName() + "'") +
                            ((traduccion.getPoDescription() == null) ? ", null, " : ", '" + traduccion.getPoDescription() + "'") +
                            ((traduccion.getPoHelp() == null) ? ", null, " : ", '" + traduccion.getPoHelp() + "'") +
                            ((traduccion.getPoPrintName() == null) ? ", null, " : ", '" + traduccion.getPoPrintName() + "'") + ") ";
                }

                // Name, Description, PrintName
                if (tableName.equalsIgnoreCase(X_AD_ReportView.Table_Name)){

                    action += "name, description, printname) " +
                            " VALUES (" + recordID + ", 0, 0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null, " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getPrintName() == null) ? ", null" : ", '" + traduccion.getPrintName() + "'") + ") ";

                }

                // Name, Description, Help, CommitWarning
                if (tableName.equalsIgnoreCase(X_AD_Tab.Table_Name)){

                    action += "name, description, help, commitwarning) " +
                            " VALUES (" + recordID + ", 0, 0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null, " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getHelp() == null) ? ", null, " : ", '" + traduccion.getHelp() + "'") +
                            ((traduccion.getCommitWarning() == null) ? ", null, " : ", '" + traduccion.getCommitWarning() + "'") + ") ";

                }

                DB.executeUpdateEx(action, get_TrxName());

            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
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

                    List<Traduccion> traduccionList = this.getTraducciones(adTable.Table_Name, adTable.get_ID(), "es_MX");
                    adTable.setTraduccionList(traduccionList);

                    this.cabezalMigracion.getTableList().add(adTable);
                }

                // Recorro columnas de esta tabla para exportar
                List<MColumn> columnList = table.getColumnsAsList();
                for (MColumn column: columnList){

                    if ((this.isDictionary()) || (!column.getEntityType().equalsIgnoreCase(X_AD_Table.ENTITYTYPE_Dictionary))){
                        ADColumn adColumn = new ADColumn(getCtx(), column.get_ID(), null);

                        adColumn.setParentType(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA);
                        adColumn.setParentName(table.getName());
                        adColumn.setParentID(table.get_ID());

                        List<Traduccion> traduccionColList = this.getTraducciones(adColumn.Table_Name, adColumn.get_ID(), "es_MX");
                        adColumn.setTraduccionList(traduccionColList);

                        this.cabezalMigracion.getColumnList().add(adColumn);
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

                List<Traduccion> traduccionList = this.getTraducciones(adProcess.Table_Name, adProcess.get_ID(), "es_MX");
                adProcess.setTraduccionList(traduccionList);

                this.cabezalMigracion.getProcessList().add(adProcess);
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
     * Agrega Parametros de Proceso seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 9/2/19.
     */
    private void exportProcesosParam() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_process_para_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Process_Para.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADProcessPara adProcessPara = new ADProcessPara(getCtx(), rs.getInt("ad_process_para_id"), null);
                adProcessPara.setParentType(rs.getString("TipoSysMigraObjFrom"));
                adProcessPara.setParentName(rs.getString("parentName"));
                adProcessPara.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionParaList = this.getTraducciones(adProcessPara.Table_Name, adProcessPara.get_ID(), "es_MX");
                adProcessPara.setTraduccionList(traduccionParaList);

                this.cabezalMigracion.getProcessParaList().add(adProcessPara);
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

                List<Traduccion> traduccionList = this.getTraducciones(adWindow.Table_Name, adWindow.get_ID(), "es_MX");
                adWindow.setTraduccionList(traduccionList);

                this.cabezalMigracion.getWindowList().add(adWindow);

                // Recorro Tabs de este proceso para exportar
                MWindow window = new MWindow(getCtx(), rs.getInt("ad_window_id"), null);
                MTab[] tabList = window.getTabs(false, null);
                for (int i = 0; i < tabList.length; i++){
                    ADTab adTab = new ADTab(getCtx(), tabList[i].get_ID(), null);

                    List<Traduccion> traduccionTabList = this.getTraducciones(adTab.Table_Name, adTab.get_ID(), "es_MX");
                    adTab.setTraduccionList(traduccionTabList);

                    this.cabezalMigracion.getTabList().add(adTab);

                    // Recorro Fields de esta tab para exportar
                    MField[] fieldList = tabList[i].getFields(false, null);
                    for (int j = 0; j < fieldList.length; j++){
                        ADField adField = new ADField(getCtx(), fieldList[j].get_ID(), null);

                        List<Traduccion> traduccionFieldList = this.getTraducciones(adField.Table_Name, adField.get_ID(), "es_MX");
                        adField.setTraduccionList(traduccionFieldList);

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

    /***
     * Obtiene información desde archivo de interface y lo carga en ventana.
     * Xpande. Created by Gabriel Vila on 10/28/19.
     * @return
     */
    public String getDataFile() {

        String message = null;

        try{

            if ((this.getFilePathOrName() == null) || (this.getFilePathOrName().trim().equalsIgnoreCase(""))){
                return "Debe indicar archivo a procesar.";
            }

            // Elimino información anterior
            String action = " delete from z_sys_migracionlin where z_sys_migracion_id =" + this.get_ID();
            DB.executeUpdateEx(action, get_TrxName());

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

            // Obtener validaciones
            message = this.getValidacionesFile();
            if (message != null){
                return message;
            }

            // Obtener referencias de list
            message = this.getReferenciasListFile();
            if (message != null){
                return message;
            }

            // Obtener referencias de Tabla
            message = this.getReferenciasTableFile();
            if (message != null){
                return message;
            }

            // Obtener referencias
            message = this.getReferenciasFile();
            if (message != null){
                return message;
            }

            // Obtener elementos desde archivo
            message = this.getElementosFile();
            if (message != null){
                return message;
            }

            // Obtener parametros de procesos
            message = this.getProcesosParaFile();
            if (message != null){
                return message;
            }

            // Obtener Vistas de Informe
            message = this.getVistasInformeFile();
            if (message != null){
                return message;
            }

            // Obtener procesos
            message = this.getProcesosFile();
            if (message != null){
                return message;
            }

            // Obtener columnas
            message = this.getColumnasFile();
            if (message != null){
                return message;
            }

            // Obtener tablas
            message = this.getTablasFile();
            if (message != null){
                return message;
            }

            // Obtener fields
            message = this.getFieldsFile();
            if (message != null){
                return message;
            }

            // Obtener Pestañas
            message = this.getTabsFile();
            if (message != null){
                return message;
            }

            // Obtener ventanas
            message = this.getVentanasFile();
            if (message != null){
                return message;
            }


            // Verifico existencia de objetos en base destino
            this.setValidacionesDestino();
            this.setReferenciasDestino();
            this.setElementosDestino();
            this.setProcesosDestino();
            this.setTablasDestino();
            this.setColumnasDestino();
            this.setRefListaDestino();
            this.setRefTablaDestino();
            this.setProcesosParamsDestino();
            this.setVistasInformesDestino();
            this.setVentanasDestino();
            this.setTabsDestino();
            this.setFieldsDestino();
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
                sysMigracionLin.setName(adElement.getColumnName());
                sysMigracionLin.setAD_Table_ID(I_AD_Element.Table_ID);
                sysMigracionLin.setRecord_ID(adElement.get_ID());
                sysMigracionLin.setStartDate(adElement.getUpdated());
                sysMigracionLin.setVersionNo(adElement.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adElement.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adElement.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adElement.getParentType());
                if (adElement.getParentName() != null) sysMigracionLin.setParentName(adElement.getParentName());
                if (adElement.getParentID() > 0) sysMigracionLin.setParent_ID(adElement.getParentID());

                sysMigracionLin.saveEx();

                adElement.setSysMigraLinID(sysMigracionLin.get_ID());

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
                sysMigracionLin.setExisteItem(false);

                if (adProcess.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adProcess.getParentType());
                if (adProcess.getParentName() != null) sysMigracionLin.setParentName(adProcess.getParentName());
                if (adProcess.getParentID() > 0) sysMigracionLin.setParent_ID(adProcess.getParentID());

                sysMigracionLin.saveEx();

                adProcess.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo parametros de procesos del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getProcesosParaFile() {

        String message = null;

        try{
            for (ADProcessPara adProcessPara: this.cabezalMigracion.getProcessParaList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM);
                sysMigracionLin.setName(adProcessPara.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Process_Para.Table_ID);
                sysMigracionLin.setRecord_ID(adProcessPara.get_ID());
                sysMigracionLin.setStartDate(adProcessPara.getUpdated());
                sysMigracionLin.setVersionNo(adProcessPara.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adProcessPara.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adProcessPara.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adProcessPara.getParentType());
                if (adProcessPara.getParentName() != null) sysMigracionLin.setParentName(adProcessPara.getParentName());
                if (adProcessPara.getParentID() > 0) sysMigracionLin.setParent_ID(adProcessPara.getParentID());

                sysMigracionLin.saveEx();

                adProcessPara.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo pestañas del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getTabsFile() {

        String message = null;

        try{
            for (ADTab adTab: this.cabezalMigracion.getTabList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA);
                sysMigracionLin.setName(adTab.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Tab.Table_ID);
                sysMigracionLin.setRecord_ID(adTab.get_ID());
                sysMigracionLin.setStartDate(adTab.getUpdated());
                sysMigracionLin.setVersionNo(adTab.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adTab.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adTab.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adTab.getParentType());
                if (adTab.getParentName() != null) sysMigracionLin.setParentName(adTab.getParentName());
                if (adTab.getParentID() > 0) sysMigracionLin.setParent_ID(adTab.getParentID());

                sysMigracionLin.saveEx();

                adTab.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo fields del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getFieldsFile() {

        String message = null;

        try{
            for (ADField adField: this.cabezalMigracion.getFieldList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD);
                sysMigracionLin.setName(adField.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_Field.Table_ID);
                sysMigracionLin.setRecord_ID(adField.get_ID());
                sysMigracionLin.setStartDate(adField.getUpdated());
                sysMigracionLin.setVersionNo(adField.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adField.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adField.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adField.getParentType());
                if (adField.getParentName() != null) sysMigracionLin.setParentName(adField.getParentName());
                if (adField.getParentID() > 0) sysMigracionLin.setParent_ID(adField.getParentID());

                sysMigracionLin.saveEx();

                adField.setSysMigraLinID(sysMigracionLin.get_ID());
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
                sysMigracionLin.setExisteItem(false);

                if (adReference.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adReference.getParentType());
                if (adReference.getParentName() != null) sysMigracionLin.setParentName(adReference.getParentName());
                if (adReference.getParentID() > 0) sysMigracionLin.setParent_ID(adReference.getParentID());

                sysMigracionLin.saveEx();

                adReference.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo vistas de informe del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getVistasInformeFile() {

        String message = null;

        try{
            for (ADReportView adReportView: this.cabezalMigracion.getReportViewList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REPORTVIEW);
                sysMigracionLin.setName(adReportView.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_ReportView.Table_ID);
                sysMigracionLin.setRecord_ID(adReportView.get_ID());
                sysMigracionLin.setStartDate(adReportView.getUpdated());
                sysMigracionLin.setVersionNo(adReportView.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adReportView.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adReportView.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adReportView.getParentType());
                if (adReportView.getParentName() != null) sysMigracionLin.setParentName(adReportView.getParentName());
                if (adReportView.getParentID() > 0) sysMigracionLin.setParent_ID(adReportView.getParentID());

                sysMigracionLin.saveEx();

                adReportView.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo referencias de lista del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 10/28/19.
     * @return
     */
    private String getReferenciasListFile() {

        String message = null;

        try{
            for (ADRef_List adRefList: this.cabezalMigracion.getRefListList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REF_LISTA);
                sysMigracionLin.setName(adRefList.getValue());
                sysMigracionLin.setAD_Table_ID(I_AD_Ref_List.Table_ID);
                sysMigracionLin.setRecord_ID(adRefList.get_ID());
                sysMigracionLin.setStartDate(adRefList.getUpdated());
                sysMigracionLin.setVersionNo(adRefList.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adRefList.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adRefList.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adRefList.getParentType());
                if (adRefList.getParentName() != null) sysMigracionLin.setParentName(adRefList.getParentName());
                if (adRefList.getParentID() > 0) sysMigracionLin.setParent_ID(adRefList.getParentID());

                sysMigracionLin.saveEx();

                adRefList.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo referencias de tabla del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 10/28/19.
     * @return
     */
    private String getReferenciasTableFile() {

        String message = null;

        try{
            for (ADRef_Table adRefTable: this.cabezalMigracion.getRefTableList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REF_TABLA);
                sysMigracionLin.setName(String.valueOf(adRefTable.getAD_Table_ID()));
                sysMigracionLin.setAD_Table_ID(I_AD_Ref_Table.Table_ID);
                sysMigracionLin.setRecord_ID(adRefTable.get_ID());
                sysMigracionLin.setStartDate(adRefTable.getUpdated());
                sysMigracionLin.setVersionNo(adRefTable.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adRefTable.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adRefTable.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adRefTable.getParentType());
                if (adRefTable.getParentName() != null) sysMigracionLin.setParentName(adRefTable.getParentName());
                if (adRefTable.getParentID() > 0) sysMigracionLin.setParent_ID(adRefTable.getParentID());

                sysMigracionLin.saveEx();

                adRefTable.setSysMigraLinID(sysMigracionLin.get_ID());
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
                sysMigracionLin.setExisteItem(false);

                if (adTable.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adTable.getParentType());
                if (adTable.getParentName() != null) sysMigracionLin.setParentName(adTable.getParentName());
                if (adTable.getParentID() > 0) sysMigracionLin.setParent_ID(adTable.getParentID());

                sysMigracionLin.saveEx();

                adTable.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Cargo columnas del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 10/29/19.
     * @return
     */
    private String getColumnasFile() {

        String message = null;

        try{
            for (ADColumn adColumn: this.cabezalMigracion.getColumnList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA);
                sysMigracionLin.setName(adColumn.getColumnName());
                sysMigracionLin.setAD_Table_ID(I_AD_Column.Table_ID);
                sysMigracionLin.setRecord_ID(adColumn.get_ID());
                sysMigracionLin.setStartDate(adColumn.getUpdated());
                sysMigracionLin.setVersionNo(adColumn.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adColumn.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adColumn.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adColumn.getParentType());
                if (adColumn.getParentName() != null) sysMigracionLin.setParentName(adColumn.getParentName());
                if (adColumn.getParentID() > 0) sysMigracionLin.setParent_ID(adColumn.getParentID());

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
                sysMigracionLin.setExisteItem(false);

                if (adValRule.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adValRule.getParentType());
                if (adValRule.getParentName() != null) sysMigracionLin.setParentName(adValRule.getParentName());
                if (adValRule.getParentID() > 0) sysMigracionLin.setParent_ID(adValRule.getParentID());

                sysMigracionLin.saveEx();

                adValRule.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Verifico existencia de validaciones en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setValidacionesDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VALIDACION);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_Val_Rule.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_Val_Rule valRuleDB = new X_AD_Val_Rule(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(valRuleDB.get_ID());
                    sysMigracionLin.saveEx();
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de ventanas en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setVentanasDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_Window.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_Window windowDB = new X_AD_Window(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(windowDB.get_ID());
                    sysMigracionLin.saveEx();

                    // Actualizo ID destino padre para hijos de este elemento
                    String action = " update z_sys_migracionlin set ParentDestino_ID =" + windowDB.get_ID() +
                            " where z_sys_migracion_id =" + this.get_ID() +
                            " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_VENTANA + "' " +
                            " and parent_id =" + sysMigracionLin.getRecord_ID();
                    DB.executeUpdateEx(action, get_TrxName());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de vistas de informe en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setVistasInformesDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REPORTVIEW);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_ReportView.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_ReportView reportViewDB = new X_AD_ReportView(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(reportViewDB.get_ID());
                    sysMigracionLin.saveEx();
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de parametros de procesos en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setProcesosParamsDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Si el elemento padre de esta columna existe en destino, verifico si esta columna existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Process_Para.COLUMNNAME_AD_Process_ID + " =" + sysMigracionLin.getParentDestino_ID() +
                            " AND " + X_AD_Process_Para.COLUMNNAME_Name + " ='" + sysMigracionLin.getName() + "'";

                    int[] itemIDs = PO.getAllIDs(X_AD_Process_Para.Table_Name, whereClause, null);
                    if (itemIDs.length > 0){
                        X_AD_Process_Para processParaDB = new X_AD_Process_Para(getCtx(), itemIDs[0], null);
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.setDestino_ID(processParaDB.get_ID());
                        sysMigracionLin.saveEx();
                    }
                }

            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de referencias en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setReferenciasDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item con el mismo nombre en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_Reference.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_Reference referenceDB = new X_AD_Reference(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(referenceDB.get_ID());
                    sysMigracionLin.saveEx();

                    // Actualizo ID destino padre para hijos de esta referencia
                    String action = " update z_sys_migracionlin set ParentDestino_ID =" + referenceDB.get_ID() +
                            " where z_sys_migracion_id =" + this.get_ID() +
                            " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REFERENCIA + "' " +
                            " and parent_id =" + sysMigracionLin.getRecord_ID();
                    DB.executeUpdateEx(action, get_TrxName());
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de elementos en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setElementosDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item con el mismo nombre en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_Element.Table_Name, " ColumnName ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_Element elementDB = new X_AD_Element(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(elementDB.get_ID());
                    sysMigracionLin.saveEx();

                    // Actualizo ID destino padre para hijos de este elemento
                    String action = " update z_sys_migracionlin set ParentDestino_ID =" + elementDB.get_ID() +
                            " where z_sys_migracion_id =" + this.get_ID() +
                            " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_ELEMENTO + "' " +
                            " and parent_id =" + sysMigracionLin.getRecord_ID();
                    DB.executeUpdateEx(action, get_TrxName());
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de columnas en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setColumnasDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Si el elemento padre de esta columna existe en destino, verifico si esta columna existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Column.COLUMNNAME_AD_Table_ID + " =" + sysMigracionLin.getParentDestino_ID() +
                            " AND " + X_AD_Column.COLUMNNAME_ColumnName + " ='" + sysMigracionLin.getName() + "'";

                    int[] itemIDs = PO.getAllIDs(X_AD_Column.Table_Name, whereClause, null);
                    if (itemIDs.length > 0){
                        X_AD_Column columnDB = new X_AD_Column(getCtx(), itemIDs[0], null);
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.setDestino_ID(columnDB.get_ID());
                        sysMigracionLin.saveEx();

                        // Actualizo ID destino padre para hijos de esta columna
                        String action = " update z_sys_migracionlin set ParentDestino_ID =" + columnDB.get_ID() +
                                " where z_sys_migracion_id =" + this.get_ID() +
                                " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_COLUMNA + "' " +
                                " and parent_id =" + sysMigracionLin.getRecord_ID();
                        DB.executeUpdateEx(action, get_TrxName());

                    }
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de pestañas en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setTabsDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Si el elemento padre de esta tab existe en destino, verifico si esta tab existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Tab.COLUMNNAME_AD_Window_ID + " =" + sysMigracionLin.getParentDestino_ID() +
                            " AND " + X_AD_Tab.COLUMNNAME_Name + " ='" + sysMigracionLin.getName() + "'";

                    int[] itemIDs = PO.getAllIDs(X_AD_Tab.Table_Name, whereClause, null);
                    if (itemIDs.length > 0){
                        X_AD_Tab tabDB = new X_AD_Tab(getCtx(), itemIDs[0], null);
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.setDestino_ID(tabDB.get_ID());
                        sysMigracionLin.saveEx();

                        // Actualizo ID destino padre para hijos de este objeto
                        String action = " update z_sys_migracionlin set ParentDestino_ID =" + tabDB.get_ID() +
                                " where z_sys_migracion_id =" + this.get_ID() +
                                " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PESTANIA + "' " +
                                " and parent_id =" + sysMigracionLin.getRecord_ID();
                        DB.executeUpdateEx(action, get_TrxName());
                    }
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de fields en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setFieldsDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Si el elemento padre de esta tab existe en destino, verifico si esta tab existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Field.COLUMNNAME_AD_Tab_ID + " =" + sysMigracionLin.getParentDestino_ID() +
                            " AND " + X_AD_Field.COLUMNNAME_Name + " ='" + sysMigracionLin.getName() + "'";

                    int[] itemIDs = PO.getAllIDs(X_AD_Field.Table_Name, whereClause, null);
                    if (itemIDs.length > 0){
                        X_AD_Field fieldDB = new X_AD_Field(getCtx(), itemIDs[0], null);
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.setDestino_ID(fieldDB.get_ID());
                        sysMigracionLin.saveEx();

                        // Actualizo ID destino padre para hijos de este objeto
                        String action = " update z_sys_migracionlin set ParentDestino_ID =" + fieldDB.get_ID() +
                                " where z_sys_migracion_id =" + this.get_ID() +
                                " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD + "' " +
                                " and parent_id =" + sysMigracionLin.getRecord_ID();
                        DB.executeUpdateEx(action, get_TrxName());
                    }
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de referencias de lista en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setRefListaDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REF_LISTA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Si el elemento padre de esta ref lista existe en destino, verifico si esta ref lista existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Ref_List.COLUMNNAME_AD_Reference_ID + " =" + sysMigracionLin.getParentDestino_ID() +
                            " AND " + X_AD_Ref_List.COLUMNNAME_Value + " ='" + sysMigracionLin.getName() + "'";

                    // Verifico si existe item en la base destino
                    int[] itemIDs = PO.getAllIDs(X_AD_Ref_List.Table_Name, whereClause, null);
                    if (itemIDs.length > 0){
                        X_AD_Ref_List referenceDB = new X_AD_Ref_List(getCtx(), itemIDs[0], null);
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.setDestino_ID(referenceDB.get_ID());
                        sysMigracionLin.saveEx();
                    }
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de referencias de tabla en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setRefTablaDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_REF_TABLA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Si el elemento padre de esta ref lista existe en destino, verifico si esta ref lista existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Ref_Table.COLUMNNAME_AD_Reference_ID + " =" + sysMigracionLin.getParentDestino_ID() +
                            " AND " + X_AD_Ref_Table.COLUMNNAME_AD_Table_ID + " =" + sysMigracionLin.getName();


                    // Verifico si existe item con el mismo nombre en la base destino
                    // La tabla de AD_Ref_Table no tiene ID propio, por lo tanto lo busco con una sql
                    String sql = " select ad_table_id from ad_ref_table where " + whereClause;
                    int idAux = DB.getSQLValueEx(get_TrxName(), sql);
                    if (idAux > 0){
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.saveEx();
                    }
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de procesos en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setProcesosDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item con el mismo nombre en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_Process.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    MProcess processDB = new MProcess(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(processDB.get_ID());
                    sysMigracionLin.saveEx();

                    // Actualizo ID destino padre para hijos de este proceso
                    String action = " update z_sys_migracionlin set ParentDestino_ID =" + processDB.get_ID() +
                            " where z_sys_migracion_id =" + this.get_ID() +
                            " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO + "' " +
                            " and parent_id =" + sysMigracionLin.getRecord_ID();
                    DB.executeUpdateEx(action, get_TrxName());
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Verifico existencia de tablas en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setTablasDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item con el mismo nombre en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_Table.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_Table tableDB = new X_AD_Table(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(tableDB.get_ID());
                    sysMigracionLin.saveEx();

                    // Actualizo ID destino padre para hijos de esta tabla
                    String action = " update z_sys_migracionlin set ParentDestino_ID =" + tableDB.get_ID() +
                            " where z_sys_migracion_id =" + this.get_ID() +
                            " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_TABLA + "' " +
                            " and parent_id =" + sysMigracionLin.getRecord_ID();
                    DB.executeUpdateEx(action, get_TrxName());

                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
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
                sysMigracionLin.setExisteItem(false);

                sysMigracionLin.saveEx();

                adWindow.setSysMigraLinID(sysMigracionLin.get_ID());
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }


    /***
     * Obtiene y retorna lineas segun tipo de objeto.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @param tipoMigraObj
     * @return
     */
    private List<MZSysMigracionLin> getLinesByTipoMigraObj(String tipoMigraObj){

        String whereClause = X_Z_Sys_MigracionLin.COLUMNNAME_Z_Sys_Migracion_ID + " =" + this.get_ID() +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_TipoSysMigraObj + " ='" + tipoMigraObj + "'";

        List<MZSysMigracionLin> lines = new Query(getCtx(), I_Z_Sys_MigracionLin.Table_Name, whereClause, get_TrxName()).list();

        return lines;
    }


    /***
     * Importa objetos del diccionario seleccionados en base destino
     * Xpande. Created by Gabriel Vila on 10/31/19.
     * @return
     */
    public String importData(){

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


            // Importo Validaciones
            this.importValidaciones();

            // Importo Referencias
            this.importReferencias();

            // Importo Referencias Lista
            this.importReferenciasLista();

            // Importo Referencias Tabla
            this.importReferenciasTabla();

            // Exporto Elementos
            //this.exportElementos();

            // Exporto Tablas
            //this.exportTablas();

            // Exporto Columnas
            //this.exportColumnas();

            // Exporto Procesos
            //this.exportProcesos();

            // Exporto Parametros de Procesos
            //this.exportProcesosParam();

            // Export vistas de informes
            //this.exportVistaInformes();

            // Exporto Ventanas
            //this.exportVentanas();

            // Exporto Pestañás
            //this.exportTabs();

            // Exporto Fields
            //this.exportFields();

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return message;
    }

    /***
     * Importo validaciones en base destino.
     * Xpande. Created by Gabriel Vila on 10/31/19.
     */
    private void importValidaciones() {

        try{

            this.hashValidaciones = new HashMap<Integer, Integer>();

            for (ADVal_Rule adValRule: this.cabezalMigracion.getValRuleList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Val_Rule.Table_ID, adValRule.get_ID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        return;
                    }
                    else {
                        // Solamente sobrescribir traducciones de este objeto
                        importObject = false;
                        importTraduccion = true;
                    }
                }
                else {
                    importObject = true;
                    importTraduccion = true;
                }

                MValRule model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new MValRule(getCtx(), 0, get_TrxName());
                    model.setAD_Org_ID(0);
                    model.setCode(adValRule.getCode());
                    model.setName(adValRule.getName());
                    model.setDescription(adValRule.getDescription());
                    model.setType(adValRule.getType());
                    model.setEntityType(adValRule.getEntityType());
                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.saveEx();

                    // Agrego asociación de ID origen con ID destino
                    this.hashValidaciones.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new MValRule(getCtx(), sysMigracionLin.getDestino_ID(), get_TrxName());
                }

                if ((model == null) || (model.get_ID() <= 0)){
                    throw new AdempiereException("No se pudo importar : " + X_AD_Val_Rule.Table_Name + " - " + adValRule.get_ID());
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Referencias en base destino.
     * Xpande. Created by Gabriel Vila on 10/31/19.
     */
    private void importReferencias() {

        try{

            this.hashReferencias = new HashMap<Integer, Integer>();

            for (ADReference adReference: this.cabezalMigracion.getReferenceList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Reference.Table_ID, adReference.get_ID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        return;
                    }
                    else {
                        // Solamente sobrescribir traducciones de este objeto
                        importObject = false;
                        importTraduccion = true;
                    }
                }
                else {
                    importObject = true;
                    importTraduccion = true;
                }

                X_AD_Reference model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new X_AD_Reference(getCtx(), 0, get_TrxName());
                    model.setAD_Org_ID(0);
                    model.setName(adReference.getName());
                    model.setDescription(adReference.getDescription());
                    model.setHelp(adReference.getHelp());
                    model.setValidationType(adReference.getValidationType());
                    model.setVFormat(adReference.getVFormat());
                    model.setEntityType(adReference.getEntityType());
                    model.setIsOrderByValue(adReference.isOrderByValue());
                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.saveEx();

                    // Agrego asociación de ID origen con ID destino
                    this.hashReferencias.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new X_AD_Reference(getCtx(), sysMigracionLin.getDestino_ID(), get_TrxName());
                }

                if ((model == null) || (model.get_ID() <= 0)){
                    throw new AdempiereException("No se pudo importar : " + X_AD_Reference.Table_Name + " - " + adReference.get_ID());
                }

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Reference.Table_Name, model.get_ID(), adReference.getTraduccionList());
                }
            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Referencias de Lista en base destino.
     * Xpande. Created by Gabriel Vila on 11/3/19.
     */
    private void importReferenciasLista() {

        try{

            for (ADRef_List adRefList: this.cabezalMigracion.getRefListList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Ref_List.Table_ID, adRefList.get_ID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        return;
                    }
                    else {
                        // Solamente sobrescribir traducciones de este objeto
                        importObject = false;
                        importTraduccion = true;
                    }
                }
                else {
                    importObject = true;
                    importTraduccion = true;
                }

                X_AD_Ref_List model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Si no encuentro ID del padre en hash, salgo con error.
                    if (!this.hashReferencias.containsKey(adRefList.getParentID())){
                        throw new AdempiereException("No se pudo obtener padre desde hash para : " + X_AD_Ref_List.Table_Name + " - " + adRefList.get_ID());
                    }

                    // Creo nuevo modelo de objeto
                    model = new X_AD_Ref_List(getCtx(), 0, get_TrxName());
                    model.setAD_Org_ID(0);
                    model.setAD_Reference_ID(this.hashReferencias.get(adRefList.getParentID()));
                    model.setValue(adRefList.getValue());
                    model.setName(adRefList.getName());
                    model.setDescription(adRefList.getDescription());
                    model.setValidFrom(adRefList.getValidFrom());
                    model.setValidTo(adRefList.getValidTo());
                    model.setEntityType(adRefList.getEntityType());
                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.saveEx();
                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new X_AD_Ref_List(getCtx(), sysMigracionLin.getDestino_ID(), get_TrxName());
                }

                if ((model == null) || (model.get_ID() <= 0)){
                    throw new AdempiereException("No se pudo importar : " + X_AD_Ref_List.Table_Name + " - " + adRefList.get_ID());
                }

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Ref_List.Table_Name, model.get_ID(), adRefList.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Referencias de Tabla en base destino.
     * Xpande. Created by Gabriel Vila on 11/3/19.
     */
    private void importReferenciasTabla() {

        try{

            for (ADRef_Table adRefTable: this.cabezalMigracion.getRefTableList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecordParent(X_AD_Ref_Table.Table_ID, adRefTable.get_ID(), adRefTable.getParentID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        return;
                    }
                    else {
                        // Solamente sobrescribir traducciones de este objeto
                        importObject = false;
                        importTraduccion = true;
                    }
                }
                else {
                    importObject = true;
                    importTraduccion = true;
                }

                X_AD_Ref_Table model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Si no encuentro ID del padre en hash, salgo con error.
                    if (!this.hashReferencias.containsKey(adRefTable.getParentID())){
                        throw new AdempiereException("No se pudo obtener padre desde hash para : " + X_AD_Ref_Table.Table_Name + " - " + adRefTable.get_ID());
                    }

                    // Creo nuevo modelo de objeto
                    model = new X_AD_Ref_Table(getCtx(), 0, get_TrxName());
                    model.setAD_Org_ID(0);
                    model.setAD_Reference_ID(this.hashReferencias.get(adRefTable.getParentID()));
                    model.setAD_Table_ID(adRefTable.getAD_Table_ID());
                    model.setAD_Key(adRefTable.getAD_Key());
                    model.setAD_Display(adRefTable.getAD_Display());
                    model.setIsValueDisplayed(adRefTable.isValueDisplayed());
                    model.setWhereClause(adRefTable.getWhereClause());
                    model.setOrderByClause(adRefTable.getOrderByClause());
                    model.setEntityType(adRefTable.getEntityType());
                    model.setAD_Window_ID(adRefTable.getAD_Window_ID());
                    model.setIsAlert(adRefTable.isAlert());
                    model.setDisplaySQL(adRefTable.getDisplaySQL());
                    model.setIsDisplayIdentifier(adRefTable.isDisplayIdentifier());
                    model.saveEx();
                }

                if ((model == null) || (model.get_ID() <= 0)){
                    throw new AdempiereException("No se pudo importar : " + X_AD_Ref_Table.Table_Name + " - " + adRefTable.get_ID());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Obtiene y retorna linea segun tabla y registro.
     * Xpande. Created by Gabriel Vila on 10/31/19.
     * @param adTableID
     * @param recordID
     * @return
     */
    private MZSysMigracionLin getLineByTableRecord(int adTableID, int recordID){

        String whereClause = X_Z_Sys_MigracionLin.COLUMNNAME_Z_Sys_Migracion_ID + " =" + this.get_ID() +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_AD_Table_ID + " =" + adTableID +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_Record_ID + " =" + recordID;

        MZSysMigracionLin model = new Query(getCtx(), I_Z_Sys_MigracionLin.Table_Name, whereClause, get_TrxName()).first();

        return model;
    }

    /***
     * Obtiene y retorna linea segun tabla, registro e id del padre.
     * Xpande. Created by Gabriel Vila on 10/31/19.
     * @param adTableID
     * @param recordID
     * @param parentID
     * @return
     */
    private MZSysMigracionLin getLineByTableRecordParent(int adTableID, int recordID, int parentID){

        String whereClause = X_Z_Sys_MigracionLin.COLUMNNAME_Z_Sys_Migracion_ID + " =" + this.get_ID() +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_AD_Table_ID + " =" + adTableID +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_Record_ID + " =" + recordID +
                " AND " + X_Z_Sys_MigracionLin.COLUMNNAME_Parent_ID + " =" + parentID;

        MZSysMigracionLin model = new Query(getCtx(), I_Z_Sys_MigracionLin.Table_Name, whereClause, get_TrxName()).first();

        return model;
    }

}
