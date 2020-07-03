package org.xpande.system.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.util.DB;
import org.compiere.util.ValueNamePair;
import org.xpande.system.migration.*;
import org.xpande.system.utils.SystemUtils;

import javax.el.MapELResolver;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
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
    private HashMap<Integer, Integer> hashElementos = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashTablas = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashColumnas = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashProcesos = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashVistaInf = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashVentanas = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashPestanias = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> hashFieldGroups = new HashMap<Integer, Integer>();

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

            // Si tengo grupo de field asociado, lo proceso
            if (field.getAD_FieldGroup_ID() > 0){
                X_AD_FieldGroup fieldGroup = new X_AD_FieldGroup(getCtx(), field.getAD_FieldGroup_ID(), null);
                this.setFieldGroupLin(fieldGroup, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELD, field.getName(), field.get_ID());
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
     * Setea linea de este documento con datos de una field group del diccionario.
     * Xpande. Created by Gabriel Vila on 11/12/19.
     * @param fieldGroup
     * @param parentType
     * @param parentName
     * @param parentID
     */
    private void setFieldGroupLin(X_AD_FieldGroup fieldGroup, String parentType, String parentName, int parentID){

        try{
            if (!this.isDictionary()){
                if (fieldGroup.getEntityType().equalsIgnoreCase(X_AD_FieldGroup.ENTITYTYPE_Dictionary)){
                    return;
                }
            }

            if (this.existeTablaRecord(I_AD_FieldGroup.Table_ID, fieldGroup.get_ID())){
                return;
            }

            MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
            sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
            sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELDGROUP);
            sysMigracionLin.setName(fieldGroup.getName());
            sysMigracionLin.setAD_Table_ID(I_AD_FieldGroup.Table_ID);
            sysMigracionLin.setRecord_ID(fieldGroup.get_ID());
            sysMigracionLin.setStartDate(fieldGroup.getUpdated());
            sysMigracionLin.setVersionNo(fieldGroup.get_ValueAsString("VersionNo"));
            sysMigracionLin.setEntityType(fieldGroup.getEntityType());
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
            sysMigracionLin.setName(parentName);
            sysMigracionLin.setAD_Table_ID(I_AD_Ref_Table.Table_ID);
            sysMigracionLin.setRecord_ID(parentID);
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

            whereClause = whereClause.replaceAll("a.name", "a.tablename");

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

            // Proceso elemento de este parametro de proceso
            X_AD_Element element = (X_AD_Element) processPara.getAD_Element();
            if ((element != null) && (element.get_ID() > 0)){
                this.setElementoLin(element, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM, processPara.getName(), processPara.get_ID());
            }

            // Si tengo referencia asociada la proceso
            if (processPara.getAD_Reference_Value_ID() > 0){
                X_AD_Reference reference = new X_AD_Reference(getCtx(), processPara.getAD_Reference_Value_ID(), null);
                this.setReferenciaLin(reference, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM, processPara.getName(), processPara.get_ID());
            }

            // Si tengo validacion asociada a este field la proceso
            if (processPara.getAD_Val_Rule_ID() > 0){
                X_AD_Val_Rule valRule = new X_AD_Val_Rule(getCtx(), processPara.getAD_Val_Rule_ID(), null);
                this.setValidacionLin(valRule, X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM, processPara.getName(), processPara.get_ID());
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

            // Exporto Field Groups
            this.exportFieldGroups();

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
     * Agrega field groups seleccionadas para exportar, en este modelo.
     * Xpande. Created by Gabriel Vila on 11/12/19.
     */
    private void exportFieldGroups() {

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select record_id as ad_fieldgroup_id, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_FieldGroup.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){
                ADFieldGroup fieldGroup = new ADFieldGroup(getCtx(), rs.getInt("ad_fieldgroup_id"), null);
                fieldGroup.setParentType(rs.getString("TipoSysMigraObjFrom"));
                fieldGroup.setParentName(rs.getString("parentName"));
                fieldGroup.setParentID(rs.getInt("parent_ID"));

                List<Traduccion> traduccionList = this.getTraducciones(fieldGroup.Table_Name, fieldGroup.get_ID(), "es_MX");
                fieldGroup.setTraduccionList(traduccionList);

                this.cabezalMigracion.getFieldGroupList().add(fieldGroup);
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

                // Me aseguro columna UUID que no sea de solo lectura
                if (column.getColumnName().equalsIgnoreCase("UUID")){
                    column.setIsMandatory(false);
                }

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
            sql = " select record_id as ad_reference_id, name, TipoSysMigraObjFrom, parentName, parent_ID " +
                    " from z_sys_migracionlin " +
                    " where z_sys_migracion_id = " + this.get_ID() +
                    " and ad_table_id =" + I_AD_Ref_Table.Table_ID +
                    " and isselected ='Y'" +
                    " order by created ";

            pstmt = DB.prepareStatement(sql, get_TrxName());
            rs = pstmt.executeQuery();

            while(rs.next()){

                // Las referencia de tabla, tienen como clave primaria la columna: AD_Reference_ID. Por lo tanto para obtener el modelo
                // no puedo usar el new.
                ADRef_Table adRefTable = SystemUtils.getRefTableByReferenceID(getCtx(), rs.getInt("ad_reference_id"), null);
                adRefTable.setNombreReferencia(rs.getString("name"));
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
                DB.executeUpdateEx(action, null);

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
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getHelp() == null) ? ", null" : ", '" + traduccion.getHelp() + "'") + ") ";
                }

                // Name, PrintName, Description, Help, PoNAme, PoDescription, PoHelp, PoPrintName
                if (tableName.equalsIgnoreCase(X_AD_Element.Table_Name)){

                    action += "name, description, help, printname, po_name, po_description, po_help, po_printname) " +
                            " VALUES (" + recordID + ", 0,0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getHelp() == null) ? ", null " : ", '" + traduccion.getHelp() + "'") +
                            ((traduccion.getPrintName() == null) ? ", null " : ", '" + traduccion.getPrintName() + "'") +
                            ((traduccion.getPoName() == null) ? ", null " : ", '" + traduccion.getPoName() + "'") +
                            ((traduccion.getPoDescription() == null) ? ", null " : ", '" + traduccion.getPoDescription() + "'") +
                            ((traduccion.getPoHelp() == null) ? ", null " : ", '" + traduccion.getPoHelp() + "'") +
                            ((traduccion.getPoPrintName() == null) ? ", null " : ", '" + traduccion.getPoPrintName() + "'") + ") ";
                }

                // Name, Description, PrintName
                if (tableName.equalsIgnoreCase(X_AD_ReportView.Table_Name)){

                    action += "name, description, printname) " +
                            " VALUES (" + recordID + ", 0, 0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getPrintName() == null) ? ", null" : ", '" + traduccion.getPrintName() + "'") + ") ";

                }

                // Name, Description, Help, CommitWarning
                if (tableName.equalsIgnoreCase(X_AD_Tab.Table_Name)){

                    action += "name, description, help, commitwarning) " +
                            " VALUES (" + recordID + ", 0, 0,'Y','" + traduccion.getCreated() + "', " + traduccion.getCreatedBy() + ", '" + traduccion.getUpdated() + "', " +
                            traduccion.getUpdatedBy() + ", '" + traduccion.getIsTranslated() + "', '" + traduccion.getLanguage() + "', '" +
                            traduccion.getName() + ((traduccion.getDescription() == null) ? "', null " : "', '" + traduccion.getDescription() + "'") +
                            ((traduccion.getHelp() == null) ? ", null " : ", '" + traduccion.getHelp() + "'") +
                            ((traduccion.getCommitWarning() == null) ? ", null " : ", '" + traduccion.getCommitWarning() + "'") + ") ";

                }

                DB.executeUpdateEx(action, null);

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

                /*
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

                 */
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

                /*
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
                */

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

            // Obtener field groups
            message = this.getFieldGroupsFile();
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
            this.setFieldGroupsDestino();

            // Por ahora siempre creo una nueva ventana y no hago modificaciones en las ya existentes. Prefiero una copia a romper la que existe.
            /*
            this.setVentanasDestino();
            this.setTabsDestino();
            this.setFieldsDestino();
            */

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
                sysMigracionLin.setName(adRefTable.getNombreReferencia());
                sysMigracionLin.setAD_Table_ID(I_AD_Ref_Table.Table_ID);
                sysMigracionLin.setRecord_ID(adRefTable.getAD_Reference_ID());
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
     * Cargo field groups del diccionario leídos previamente desde archivo de interface, en lineas de este proceso.
     * Xpande. Created by Gabriel Vila on 9/8/19.
     * @return
     */
    private String getFieldGroupsFile() {

        String message = null;

        try{
            for (ADFieldGroup adFieldGroup: this.cabezalMigracion.getFieldGroupList()){

                MZSysMigracionLin sysMigracionLin = new MZSysMigracionLin(getCtx(), 0, get_TrxName());
                sysMigracionLin.setZ_Sys_Migracion_ID(this.get_ID());
                sysMigracionLin.setTipoSysMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELDGROUP);
                sysMigracionLin.setName(adFieldGroup.getName());
                sysMigracionLin.setAD_Table_ID(I_AD_FieldGroup.Table_ID);
                sysMigracionLin.setRecord_ID(adFieldGroup.get_ID());
                sysMigracionLin.setStartDate(adFieldGroup.getUpdated());
                sysMigracionLin.setVersionNo(adFieldGroup.get_ValueAsString("VersionNo"));
                sysMigracionLin.setEntityType(adFieldGroup.getEntityType());
                sysMigracionLin.setIsSelected(true);
                sysMigracionLin.setExisteItem(false);

                if (adFieldGroup.getParentType() != null) sysMigracionLin.setTipoSysMigraObjFrom(adFieldGroup.getParentType());
                if (adFieldGroup.getParentName() != null) sysMigracionLin.setParentName(adFieldGroup.getParentName());
                if (adFieldGroup.getParentID() > 0) sysMigracionLin.setParent_ID(adFieldGroup.getParentID());

                sysMigracionLin.saveEx();

                adFieldGroup.setSysMigraLinID(sysMigracionLin.get_ID());
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
     * Verifico existencia de field groups en base destino.
     * Xpande. Created by Gabriel Vila on 10/30/19.
     * @return
     */
    private void setFieldGroupsDestino(){

        try{

            List<MZSysMigracionLin> sysMigracionLinList = this.getLinesByTipoMigraObj(X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_FIELDGROUP);

            for (MZSysMigracionLin sysMigracionLin: sysMigracionLinList){

                // Verifico si existe item en la base destino
                int[] itemIDs = PO.getAllIDs(X_AD_FieldGroup.Table_Name, " Name ='" + sysMigracionLin.getName() + "'", null);
                if (itemIDs.length > 0){
                    X_AD_FieldGroup fieldGroupDB = new X_AD_FieldGroup(getCtx(), itemIDs[0], null);
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setDestino_ID(fieldGroupDB.get_ID());
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

                        // Actualizo ID destino padre para hijos de este objeto
                        String action = " update z_sys_migracionlin set ParentDestino_ID =" + processParaDB.get_ID() +
                                " where z_sys_migracion_id =" + this.get_ID() +
                                " and tiposysmigraobjfrom ='" + X_Z_Sys_MigracionLin.TIPOSYSMIGRAOBJ_PROCESO_PARAM + "' " +
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

                // Si el elemento padre de esta ref tabla existe en destino, verifico si esta ref tabla existe en base destino
                if (sysMigracionLin.getParentDestino_ID() > 0){

                    // Verifico si existe item en la base destino
                    String whereClause = X_AD_Ref_Table.COLUMNNAME_AD_Reference_ID + " =" + sysMigracionLin.getParentDestino_ID();

                    // Verifico si existe item en la base destino
                    // La tabla de AD_Ref_Table no tiene ID propio, por lo tanto lo busco con una sql
                    String sql = " select ad_reference_id from ad_ref_table where " + whereClause;
                    int idAux = DB.getSQLValueEx(get_TrxName(), sql);
                    if (idAux > 0){
                        sysMigracionLin.setExisteItem(true);
                        sysMigracionLin.setDestino_ID(sysMigracionLin.getParentDestino_ID());
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

            // Importo Elementos
            this.importElementos();

            // Importo Tablas
            this.importTablas();

            // Importo vistas de informes
            this.importVistaInformes();

            // Importo Procesos
            this.importProcesos();

            // Importo Parametros de Procesos
            this.importProcesosParam();

            // Importo Columnas
            this.importColumnas();

            // Importo Ventanas
            this.importVentanas();

            // Importo Pestañás
            this.importTabs();

            // Importo Field Groups
            this.importFieldGroups();

            // Importo Fields
            this.importFields();

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
                        // Agrego asociación de ID origen con ID destino
                        this.hashValidaciones.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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
                    model = new MValRule(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setCode(adValRule.getCode());
                    model.setName(adValRule.getName());
                    model.setDescription(adValRule.getDescription());
                    model.setType(adValRule.getType());
                    model.setEntityType(adValRule.getEntityType());
                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new MValRule(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Val_Rule.Table_Name + " - " + adValRule.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    // Modifico atributos
                    model.setCode(adValRule.getCode());
                    model.setDescription(adValRule.getDescription());
                    model.setType(adValRule.getType());
                    model.setEntityType(adValRule.getEntityType());
                    model.saveEx();

                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashValidaciones.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo field groups en base destino.
     * Xpande. Created by Gabriel Vila on 10/31/19.
     */
    private void importFieldGroups() {

        try{

            this.hashFieldGroups = new HashMap<Integer, Integer>();

            for (ADFieldGroup adFieldGroup: this.cabezalMigracion.getFieldGroupList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_FieldGroup.Table_ID, adFieldGroup.get_ID());
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
                        continue;
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

                X_AD_FieldGroup model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new X_AD_FieldGroup(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(adFieldGroup.getName());
                    model.setEntityType(adFieldGroup.getEntityType());
                    model.setFieldGroupType(adFieldGroup.getFieldGroupType());
                    model.setIsCollapsedByDefault(adFieldGroup.getIsCollapsedByDefault());

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new X_AD_FieldGroup(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_FieldGroup.Table_Name + " - " + adFieldGroup.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    // Modifico atributos
                    model.setEntityType(adFieldGroup.getEntityType());
                    model.setFieldGroupType(adFieldGroup.getFieldGroupType());
                    model.setIsCollapsedByDefault(adFieldGroup.getIsCollapsedByDefault());

                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashFieldGroups.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_FieldGroup.Table_Name, model.get_ID(), adFieldGroup.getTraduccionList());
                }

            }

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }


    /***
     * Importo vistas de informe en base destino.
     * Xpande. Created by Gabriel Vila on 11/7/19.
     */
    private void importVistaInformes(){

        try{

            this.hashVistaInf = new HashMap<Integer, Integer>();

            for (ADReportView adReportView: this.cabezalMigracion.getReportViewList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_ReportView.Table_ID, adReportView.get_ID());
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
                        // Agrego asociación de ID origen con ID destino
                        this.hashVistaInf.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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

                X_AD_ReportView model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new X_AD_ReportView(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(adReportView.getName());
                    model.setDescription(adReportView.getDescription());
                    model.setWhereClause(adReportView.getWhereClause());
                    model.setOrderByClause(adReportView.getOrderByClause());
                    model.setEntityType(adReportView.getEntityType());
                    model.setPrintName(adReportView.getPrintName());
                    model.setIsCentrallyMaintained(adReportView.getIsCentrallyMaintained());

                    if (this.hashTablas.containsKey(adReportView.getAD_Table_ID())){
                        model.setAD_Table_ID(this.hashReferencias.get(adReportView.getAD_Table_ID()));
                    }
                    else {
                        model.setAD_Table_ID(adReportView.getAD_Table_ID());
                    }

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new X_AD_ReportView(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_ReportView.Table_Name + " - " + adReportView.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    // Modifico atributos
                    model.setDescription(adReportView.getDescription());
                    model.setWhereClause(adReportView.getWhereClause());
                    model.setOrderByClause(adReportView.getOrderByClause());
                    model.setEntityType(adReportView.getEntityType());
                    model.setPrintName(adReportView.getPrintName());
                    model.setIsCentrallyMaintained(adReportView.getIsCentrallyMaintained());

                    if (this.hashTablas.containsKey(adReportView.getAD_Table_ID())){
                        model.setAD_Table_ID(this.hashReferencias.get(adReportView.getAD_Table_ID()));
                    }
                    else {
                        model.setAD_Table_ID(adReportView.getAD_Table_ID());
                    }

                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashVistaInf.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_ReportView.Table_Name, model.get_ID(), adReportView.getTraduccionList());
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
                        // Agrego asociación de ID origen con ID destino
                        this.hashReferencias.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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
                    model = new X_AD_Reference(getCtx(), 0, null);
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
                    sysMigracionLin.setExisteItem(true);

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new X_AD_Reference(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Reference.Table_Name + " - " + adReference.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.setDescription(adReference.getDescription());
                    model.setHelp(adReference.getHelp());
                    model.setValidationType(adReference.getValidationType());
                    model.setVFormat(adReference.getVFormat());
                    model.setEntityType(adReference.getEntityType());
                    model.setIsOrderByValue(adReference.isOrderByValue());
                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashReferencias.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

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
     * Importo Elementos en base destino.
     * Xpande. Created by Gabriel Vila on 11/4/19.
     */
    private void importElementos() {

        try{

            this.hashElementos = new HashMap<Integer, Integer>();

            for (ADElement adElement: this.cabezalMigracion.getElementList()){

                System.out.println(adElement.getName());

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Element.Table_ID, adElement.get_ID());
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
                        // Agrego asociación de ID origen con ID destino
                        this.hashElementos.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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

                M_Element model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new M_Element(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setColumnName(adElement.getColumnName());
                    model.setEntityType(adElement.getEntityType());
                    model.setName(adElement.getName());
                    model.setPrintName(adElement.getPrintName());
                    model.setDescription(adElement.getDescription());
                    model.setHelp(adElement.getHelp());
                    model.setPO_Name(adElement.getPO_Name());
                    model.setPO_PrintName(adElement.getPO_PrintName());
                    model.setPO_Description(adElement.getPO_Description());
                    model.setPO_Help(adElement.getPO_Help());
                    model.setFieldLength(adElement.getFieldLength());
                    model.setAD_Reference_ID(adElement.getAD_Reference_ID());

                    if (adElement.getAD_Reference_Value_ID() > 0){
                        if (this.hashReferencias.containsKey(adElement.getAD_Reference_Value_ID())){
                            model.setAD_Reference_Value_ID(this.hashReferencias.get(adElement.getAD_Reference_Value_ID()).intValue());
                        }
                        else {
                            model.setAD_Reference_Value_ID(adElement.getAD_Reference_Value_ID());
                        }
                    }

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new M_Element(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Element.Table_Name + " - " + adElement.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.setEntityType(adElement.getEntityType());
                    model.setName(adElement.getName());
                    model.setPrintName(adElement.getPrintName());
                    model.setDescription(adElement.getDescription());
                    model.setHelp(adElement.getHelp());
                    model.setPO_Name(adElement.getPO_Name());
                    model.setPO_PrintName(adElement.getPO_PrintName());
                    model.setPO_Description(adElement.getPO_Description());
                    model.setPO_Help(adElement.getPO_Help());
                    model.setFieldLength(adElement.getFieldLength());
                    model.setAD_Reference_ID(adElement.getAD_Reference_ID());

                    if (adElement.getAD_Reference_Value_ID() > 0){
                        if (this.hashReferencias.containsKey(adElement.getAD_Reference_Value_ID())){
                            model.setAD_Reference_Value_ID(this.hashReferencias.get(adElement.getAD_Reference_Value_ID()).intValue());
                        }
                        else {
                            model.setAD_Reference_Value_ID(adElement.getAD_Reference_Value_ID());
                        }
                    }

                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashElementos.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Element.Table_Name, model.get_ID(), adElement.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Tablas en base destino.
     * Xpande. Created by Gabriel Vila on 11/4/19.
     */
    private void importTablas() {

        try{

            this.hashTablas = new HashMap<Integer, Integer>();

            for (ADTable adTable: this.cabezalMigracion.getTableList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Table.Table_ID, adTable.get_ID());
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
                        // Agrego asociación de ID origen con ID destino
                        this.hashTablas.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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

                MTable model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new MTable(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(adTable.getName());
                    model.setDescription(adTable.getDescription());
                    model.setHelp(adTable.getHelp());
                    model.setTableName(adTable.getTableName());
                    model.setIsView(adTable.getIsView());
                    model.setAccessLevel(adTable.getAccessLevel());
                    model.setEntityType(adTable.getEntityType());
                    model.setIsSecurityEnabled(adTable.getIsSecurityEnabled());
                    model.setIsDeleteable(adTable.getIsDeleteable());
                    model.setIsHighVolume(adTable.getIsHighVolume());
                    model.setIsChangeLog(adTable.getIsChangeLog());
                    model.setReplicationType(adTable.getReplicationType());
                    model.setIsCentrallyMaintained(adTable.getIsCentrallyMaintained());
                    model.setIsDocument(adTable.getIsDocument());

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);
                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new MTable(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Table.Table_Name + " - " + adTable.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.setName(adTable.getName());
                    model.setDescription(adTable.getDescription());
                    model.setHelp(adTable.getHelp());
                    model.setTableName(adTable.getTableName());
                    model.setIsView(adTable.getIsView());
                    model.setAccessLevel(adTable.getAccessLevel());
                    model.setEntityType(adTable.getEntityType());
                    model.setIsSecurityEnabled(adTable.getIsSecurityEnabled());
                    model.setIsDeleteable(adTable.getIsDeleteable());
                    model.setIsHighVolume(adTable.getIsHighVolume());
                    model.setIsChangeLog(adTable.getIsChangeLog());
                    model.setReplicationType(adTable.getReplicationType());
                    model.setIsCentrallyMaintained(adTable.getIsCentrallyMaintained());
                    model.setIsDocument(adTable.getIsDocument());

                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashTablas.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Table.Table_Name, model.get_ID(), adTable.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Procesos en base destino.
     * Xpande. Created by Gabriel Vila on 11/4/19.
     */
    private void importProcesos() {

        try{

            this.hashProcesos = new HashMap<Integer, Integer>();

            for (ADProcess adProcess: this.cabezalMigracion.getProcessList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Process.Table_ID, adProcess.get_ID());
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
                        // Agrego asociación de ID origen con ID destino
                        this.hashProcesos.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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

                MProcess model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new MProcess(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setValue(adProcess.getValue());
                    model.setName(adProcess.getName());
                    model.setDescription(adProcess.getDescription());
                    model.setHelp(adProcess.getHelp());
                    model.setAccessLevel(adProcess.getAccessLevel());
                    model.setEntityType(adProcess.getEntityType());
                    model.setProcedureName(adProcess.getProcedureName());
                    model.setIsReport(adProcess.getIsReport());
                    model.setIsDirectPrint(adProcess.getIsDirectPrint());
                    model.setClassname(adProcess.getClassname());
                    model.setWorkflowValue(adProcess.getWorkflowValue());
                    model.setIsBetaFunctionality(adProcess.getIsBetaFunctionality());
                    model.setIsServerProcess(adProcess.getIsServerProcess());
                    model.setShowHelp(adProcess.getShowHelp());
                    model.setJasperReport(adProcess.getJasperReport());

                    if (adProcess.getAD_ReportView_ID() > 0){
                        if (this.hashVistaInf.containsKey(adProcess.getAD_ReportView_ID())){
                            model.setAD_ReportView_ID(this.hashVistaInf.get(adProcess.getAD_ReportView_ID()));
                        }
                        else {
                            model.setAD_ReportView_ID(adProcess.getAD_ReportView_ID());
                        }
                    }

                    if (adProcess.getAD_Browse_ID() > 0){
                        model.setAD_Browse_ID(adProcess.getAD_Browse_ID());
                    }

                    if (adProcess.getAD_Form_ID() > 0){
                        model.setAD_Form_ID(adProcess.getAD_Form_ID());
                    }

                    if (adProcess.getAD_Workflow_ID() > 0){
                        model.setAD_Workflow_ID(adProcess.getAD_Workflow_ID());
                    }

                    if (adProcess.getAD_PrintFormat_ID() > 0){
                        model.setAD_PrintFormat_ID(adProcess.getAD_PrintFormat_ID());
                    }

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);
                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new MProcess(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Process.Table_Name + " - " + adProcess.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.setDescription(adProcess.getDescription());
                    model.setHelp(adProcess.getHelp());
                    model.setAccessLevel(adProcess.getAccessLevel());
                    model.setEntityType(adProcess.getEntityType());
                    model.setProcedureName(adProcess.getProcedureName());
                    model.setIsReport(adProcess.getIsReport());
                    model.setIsDirectPrint(adProcess.getIsDirectPrint());
                    model.setClassname(adProcess.getClassname());
                    model.setWorkflowValue(adProcess.getWorkflowValue());
                    model.setIsBetaFunctionality(adProcess.getIsBetaFunctionality());
                    model.setIsServerProcess(adProcess.getIsServerProcess());
                    model.setShowHelp(adProcess.getShowHelp());
                    model.setJasperReport(adProcess.getJasperReport());

                    if (adProcess.getAD_ReportView_ID() > 0){
                        if (this.hashVistaInf.containsKey(adProcess.getAD_ReportView_ID())){
                            model.setAD_ReportView_ID(this.hashVistaInf.get(adProcess.getAD_ReportView_ID()));
                        }
                        else {
                            model.setAD_ReportView_ID(adProcess.getAD_ReportView_ID());
                        }
                    }

                    if (adProcess.getAD_Browse_ID() > 0){
                        model.setAD_Browse_ID(adProcess.getAD_Browse_ID());
                    }

                    if (adProcess.getAD_Form_ID() > 0){
                        model.setAD_Form_ID(adProcess.getAD_Form_ID());
                    }

                    if (adProcess.getAD_Workflow_ID() > 0){
                        model.setAD_Workflow_ID(adProcess.getAD_Workflow_ID());
                    }

                    if (adProcess.getAD_PrintFormat_ID() > 0){
                        model.setAD_PrintFormat_ID(adProcess.getAD_PrintFormat_ID());
                    }
                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashProcesos.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Process.Table_Name, model.get_ID(), adProcess.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Ventanas en base destino.
     * Xpande. Created by Gabriel Vila on 11/11/19.
     */
    private void importVentanas() {

        try{

            this.hashVentanas = new HashMap<Integer, Integer>();

            for (ADWindow adWindow: this.cabezalMigracion.getWindowList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Window.Table_ID, adWindow.get_ID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                /*
                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        continue;
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
                */

                boolean importTraduccion = true;
                boolean importObject = true;


                MWindow model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Nombre de la ventana para que no duplique si es que existe. Siempre la ventana es nueva.
                    String nombre = "IMP_" + new Timestamp(System.currentTimeMillis()).toString() + "_" + adWindow.getName();

                    // Creo nuevo modelo de objeto
                    model = new MWindow(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(nombre);
                    model.setDescription(adWindow.getDescription());
                    model.setHelp(adWindow.getHelp());
                    model.setWindowType(adWindow.getWindowType());
                    model.setIsSOTrx(adWindow.getIsSOTrx());
                    model.setEntityType(adWindow.getEntityType());
                    model.setProcessing(false);
                    model.setIsDefault(adWindow.getIsDefault());
                    model.setWinHeight(adWindow.getWinHeight());
                    model.setWinWidth(adWindow.getWinWidth());
                    model.setIsBetaFunctionality(adWindow.getIsBetaFunctionality());

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);
                }

                sysMigracionLin.setMessage("OK - " + model.getName());
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashVentanas.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Window.Table_Name, model.get_ID(), adWindow.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Pestañas en base destino.
     * Xpande. Created by Gabriel Vila on 11/11/19.
     */
    private void importTabs() {

        try{

            this.hashPestanias = new HashMap<Integer, Integer>();

            for (ADTab adTab: this.cabezalMigracion.getTabList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Tab.Table_ID, adTab.get_ID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                /*
                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        continue;
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
                */

                boolean importTraduccion = true;
                boolean importObject = true;

                MTab model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new MTab(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(adTab.getName());
                    model.setDescription(adTab.getDescription());
                    model.setHelp(adTab.getHelp());
                    model.setSeqNo(adTab.getSeqNo());
                    model.setTabLevel(adTab.getTabLevel());
                    model.setIsSingleRow(adTab.getIsSingleRow());
                    model.setIsInfoTab(adTab.getIsInfoTab());
                    model.setIsTranslationTab(adTab.getIsTranslationTab());
                    model.setIsReadOnly(adTab.getIsReadOnly());
                    model.setHasTree(adTab.getIsHasTree());
                    model.setWhereClause(adTab.getWhereClause());
                    model.setOrderByClause(adTab.getOrderByClause());
                    model.setCommitWarning(adTab.getCommitWarning());
                    model.setIsSortTab(adTab.getIsSortTab());
                    model.setEntityType(adTab.getEntityType());
                    model.setReadOnlyLogic(adTab.getReadOnlyLogic());
                    model.setDisplayLogic(adTab.getDisplayLogic());
                    model.setIsInsertRecord(adTab.getIsInsertRecord());
                    model.setIsAdvancedTab(adTab.getIsAdvancedTab());

                    // Included_Tab_ID No por ahora, no hace falta.

                    if (adTab.getAD_Process_ID() > 0){
                        if (this.hashProcesos.containsKey(adTab.getAD_Process_ID())){
                            model.setAD_Process_ID(this.hashProcesos.get(adTab.getAD_Process_ID()).intValue());
                        }
                        else {
                            model.setAD_Process_ID(adTab.getAD_Process_ID());
                        }
                    }

                    if (this.hashTablas.containsKey(adTab.getAD_Table_ID())){
                        model.setAD_Table_ID(this.hashTablas.get(adTab.getAD_Table_ID()).intValue());
                    }
                    else {
                        model.setAD_Table_ID(adTab.getAD_Table_ID());
                    }

                    if (adTab.getAD_Column_ID() > 0){
                        if (this.hashColumnas.containsKey(adTab.getAD_Column_ID())){
                            model.setAD_Column_ID(this.hashColumnas.get(adTab.getAD_Column_ID()).intValue());
                        }
                        else {
                            model.setAD_Column_ID(adTab.getAD_Column_ID());
                        }
                    }

                    if (adTab.getAD_ColumnSortOrder_ID() > 0){
                        if (this.hashColumnas.containsKey(adTab.getAD_ColumnSortOrder_ID())){
                            model.setAD_ColumnSortOrder_ID(this.hashColumnas.get(adTab.getAD_ColumnSortOrder_ID()).intValue());
                        }
                        else {
                            model.setAD_ColumnSortOrder_ID(adTab.getAD_ColumnSortOrder_ID());
                        }
                    }

                    if (adTab.getAD_ColumnSortYesNo_ID() > 0){
                        if (this.hashColumnas.containsKey(adTab.getAD_ColumnSortYesNo_ID())){
                            model.setAD_ColumnSortYesNo_ID(this.hashColumnas.get(adTab.getAD_ColumnSortYesNo_ID()).intValue());
                        }
                        else {
                            model.setAD_ColumnSortYesNo_ID(adTab.getAD_ColumnSortYesNo_ID());
                        }
                    }

                    if (adTab.getParent_Column_ID() > 0){
                        if (this.hashColumnas.containsKey(adTab.getParent_Column_ID())){
                            model.setParent_Column_ID(this.hashColumnas.get(adTab.getParent_Column_ID()).intValue());
                        }
                        else {
                            model.setParent_Column_ID(adTab.getParent_Column_ID());
                        }
                    }

                    if (this.hashVentanas.containsKey(adTab.getAD_Window_ID())){
                        model.setAD_Window_ID(this.hashVentanas.get(adTab.getAD_Window_ID()).intValue());
                    }
                    else {
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Tab.Table_Name + " - " + adTab.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashPestanias.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Tab.Table_Name, model.get_ID(), adTab.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Fields en base destino.
     * Xpande. Created by Gabriel Vila on 11/11/19.
     */
    private void importFields() {

        try{

            for (ADField adField: this.cabezalMigracion.getFieldList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Field.Table_ID, adField.get_ID());
                if ((sysMigracionLin == null) || (sysMigracionLin.get_ID() <= 0)){
                    continue;
                }

                if (!sysMigracionLin.isSelected()){
                    continue;
                }

                /*
                boolean importTraduccion = false;
                boolean importObject = false;

                // Si este objeto ya existe en la base destino
                if (sysMigracionLin.isExisteItem()){
                    // Si no esta seleccionado para sobreescribir traduccion, entonces salgo
                    if (!this.isTranslated()){
                        continue;
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
                */

                boolean importTraduccion = true;
                boolean importObject = true;

                MField model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new MField(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(adField.getName());
                    model.setDescription(adField.getDescription());
                    model.setHelp(adField.getHelp());
                    model.setIsCentrallyMaintained(adField.getIsCentrallyMaintained());
                    model.setIsDisplayed(adField.getIsDisplayed());
                    model.setDisplayLogic(adField.getDisplayLogic());
                    model.setDisplayLength(adField.getDisplayLength());
                    model.setIsReadOnly(adField.getIsReadOnly());
                    model.setSeqNo(adField.getSeqNo());
                    model.setSortNo(adField.getSortNo());
                    model.setIsSameLine(adField.getIsSameLine());
                    model.setIsHeading(adField.getIsHeading());
                    model.setIsFieldOnly(adField.getIsFieldOnly());
                    model.setIsEncrypted(adField.getIsEncrypted());
                    model.setEntityType(adField.getEntityType());
                    model.setObscureType(adField.getObscureType());
                    model.setIsMandatory(adField.getIsMandatory());
                    model.setDefaultValue(adField.getDefaultValue());
                    model.setInfoFactoryClass(adField.getInfoFactoryClass());
                    model.setPreferredWidth(adField.getPreferredWidth());
                    model.setIsDisplayedGrid(adField.getIsDisplayedGrid());
                    model.setSeqNoGrid(adField.getSeqNoGrid());
                    model.setIsAllowCopy(adField.getIsAllowCopy());

                    if (adField.getAD_Reference_ID() > 0){
                        model.setAD_Reference_ID(adField.getAD_Reference_ID());
                    }

                    // Included_Tab_ID No por ahora, no hace falta.

                    if (adField.getAD_FieldGroup_ID() > 0){
                        if (this.hashFieldGroups.containsKey(adField.getAD_FieldGroup_ID())){
                            model.setAD_FieldGroup_ID(this.hashFieldGroups.get(adField.getAD_FieldGroup_ID()).intValue());
                        }
                        else {
                            model.setAD_FieldGroup_ID(adField.getAD_FieldGroup_ID());
                        }
                    }

                    if (adField.getAD_Reference_Value_ID() > 0){
                        if (this.hashReferencias.containsKey(adField.getAD_Reference_Value_ID())){
                            model.setAD_Reference_Value_ID(this.hashReferencias.get(adField.getAD_Reference_Value_ID()).intValue());
                        }
                        else {
                            model.setAD_Reference_Value_ID(adField.getAD_Reference_Value_ID());
                        }
                    }

                    if (adField.getAD_Val_Rule_ID() > 0){
                        if (this.hashValidaciones.containsKey(adField.getAD_Val_Rule_ID())){
                            model.setAD_Val_Rule_ID(this.hashValidaciones.get(adField.getAD_Val_Rule_ID()).intValue());
                        }
                        else {
                            model.setAD_Val_Rule_ID(adField.getAD_Val_Rule_ID());
                        }
                    }

                    if (this.hashColumnas.containsKey(adField.getAD_Column_ID())){
                        model.setAD_Column_ID(this.hashColumnas.get(adField.getAD_Column_ID()).intValue());
                    }
                    else {
                        // Por las dudas verifico que esta columna exista en la base de datos.
                        // Sino existe no agrego este field.
                        MColumn column = (MColumn) adField.getAD_Column();
                        if ((column == null) || (column.get_ID() <= 0)){
                            continue;
                        }
                        model.setAD_Column_ID(adField.getAD_Column_ID());
                    }

                    if (this.hashPestanias.containsKey(adField.getAD_Tab_ID())){
                        model.setAD_Tab_ID(this.hashPestanias.get(adField.getAD_Tab_ID()).intValue());
                    }
                    else {
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Field.Table_Name + " - " + adField.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Field.Table_Name, model.get_ID(), adField.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }

    /***
     * Importo Parametros de Procesos en base destino.
     * Xpande. Created by Gabriel Vila on 11/7/19.
     */
    private void importProcesosParam() {

        try{

            for (ADProcessPara adProcessPara: this.cabezalMigracion.getProcessParaList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Process_Para.Table_ID, adProcessPara.get_ID());
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
                        continue;
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

                MProcessPara model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Creo nuevo modelo de objeto
                    model = new MProcessPara(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setName(adProcessPara.getName());
                    model.setDescription(adProcessPara.getDescription());
                    model.setHelp(adProcessPara.getHelp());
                    model.setSeqNo(adProcessPara.getSeqNo());
                    model.setAD_Reference_ID(adProcessPara.getAD_Reference_ID());
                    model.setColumnName(adProcessPara.getColumnName());
                    model.setIsCentrallyMaintained(adProcessPara.getIsCentrallyMaintained());
                    model.setFieldLength(adProcessPara.getFieldLength());
                    model.setIsMandatory(adProcessPara.getIsMandatory());
                    model.setIsRange(adProcessPara.getIsRange());
                    model.setDefaultValue(adProcessPara.getDefaultValue());
                    model.setDefaultValue2(adProcessPara.getDefaultValue2());
                    model.setVFormat(adProcessPara.getVFormat());
                    model.setValueMin(adProcessPara.getValueMin());
                    model.setValueMax(adProcessPara.getValueMax());
                    model.setEntityType(adProcessPara.getEntityType());
                    model.setReadOnlyLogic(adProcessPara.getReadOnlyLogic());
                    model.setDisplayLogic(adProcessPara.getDisplayLogic());
                    model.setIsInfoOnly(adProcessPara.getIsInfoOnly());

                    if (this.hashElementos.containsKey(adProcessPara.getAD_Element_ID())){
                        model.setAD_Element_ID(this.hashElementos.get(adProcessPara.getAD_Element_ID()).intValue());
                    }
                    else {
                        model.setAD_Element_ID(adProcessPara.getAD_Element_ID());
                    }

                    if (adProcessPara.getAD_Reference_Value_ID() > 0){
                        if (this.hashReferencias.containsKey(adProcessPara.getAD_Reference_Value_ID())){
                            model.setAD_Reference_Value_ID(this.hashReferencias.get(adProcessPara.getAD_Reference_Value_ID()).intValue());
                        }
                        else {
                            model.setAD_Reference_Value_ID(adProcessPara.getAD_Reference_Value_ID());
                        }
                    }

                    if (adProcessPara.getAD_Val_Rule_ID() > 0){
                        if (this.hashValidaciones.containsKey(adProcessPara.getAD_Val_Rule_ID())){
                            model.setAD_Val_Rule_ID(this.hashValidaciones.get(adProcessPara.getAD_Val_Rule_ID()).intValue());
                        }
                        else {
                            model.setAD_Val_Rule_ID(adProcessPara.getAD_Val_Rule_ID());
                        }
                    }

                    if (this.hashProcesos.containsKey(adProcessPara.getAD_Process_ID())){
                        model.setAD_Process_ID(this.hashProcesos.get(adProcessPara.getAD_Process_ID()));
                    }
                    else {
                        model.setAD_Process_ID(adProcessPara.getAD_Process_ID());
                    }

                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.get_ID());
                    sysMigracionLin.setExisteItem(true);
                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new MProcessPara(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Process_Para.Table_Name + " - " + adProcessPara.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.setDescription(adProcessPara.getDescription());
                    model.setHelp(adProcessPara.getHelp());
                    model.setSeqNo(adProcessPara.getSeqNo());
                    model.setAD_Reference_ID(adProcessPara.getAD_Reference_ID());
                    model.setColumnName(adProcessPara.getColumnName());
                    model.setIsCentrallyMaintained(adProcessPara.getIsCentrallyMaintained());
                    model.setFieldLength(adProcessPara.getFieldLength());
                    model.setIsMandatory(adProcessPara.getIsMandatory());
                    model.setIsRange(adProcessPara.getIsRange());
                    model.setDefaultValue(adProcessPara.getDefaultValue());
                    model.setDefaultValue2(adProcessPara.getDefaultValue2());
                    model.setVFormat(adProcessPara.getVFormat());
                    model.setValueMin(adProcessPara.getValueMin());
                    model.setValueMax(adProcessPara.getValueMax());
                    model.setEntityType(adProcessPara.getEntityType());
                    model.setReadOnlyLogic(adProcessPara.getReadOnlyLogic());
                    model.setDisplayLogic(adProcessPara.getDisplayLogic());
                    model.setIsInfoOnly(adProcessPara.getIsInfoOnly());

                    if (this.hashElementos.containsKey(adProcessPara.getAD_Element_ID())){
                        model.setAD_Element_ID(this.hashElementos.get(adProcessPara.getAD_Element_ID()).intValue());
                    }
                    else {
                        model.setAD_Element_ID(adProcessPara.getAD_Element_ID());
                    }

                    if (adProcessPara.getAD_Reference_Value_ID() > 0){
                        if (this.hashReferencias.containsKey(adProcessPara.getAD_Reference_Value_ID())){
                            model.setAD_Reference_Value_ID(this.hashReferencias.get(adProcessPara.getAD_Reference_Value_ID()).intValue());
                        }
                        else {
                            model.setAD_Reference_Value_ID(adProcessPara.getAD_Reference_Value_ID());
                        }
                    }

                    if (adProcessPara.getAD_Val_Rule_ID() > 0){
                        if (this.hashValidaciones.containsKey(adProcessPara.getAD_Val_Rule_ID())){
                            model.setAD_Val_Rule_ID(this.hashValidaciones.get(adProcessPara.getAD_Val_Rule_ID()).intValue());
                        }
                        else {
                            model.setAD_Val_Rule_ID(adProcessPara.getAD_Val_Rule_ID());
                        }
                    }

                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Process_Para.Table_Name, model.get_ID(), adProcessPara.getTraduccionList());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
    }


    /***
     * Importo Columnas de tablas en base destino.
     * Proceso sensible.
     * Xpande. Created by Gabriel Vila on 11/4/19.
     */
    private void importColumnas() {

        String sql = null;
        try{

            this.hashColumnas = new HashMap<Integer, Integer>();

            for (ADColumn adColumn: this.cabezalMigracion.getColumnList()){

                System.out.println(adColumn.getColumnName());

                /*
                // Evito conflictos entre bases que no tienen UUID
                if (adColumn.getColumnName().equalsIgnoreCase("UUID")){
                    continue;
                }
                */

                    MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Column.Table_ID, adColumn.get_ID());
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
                        // Agrego asociación de ID origen con ID destino
                        this.hashColumnas.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());
                        continue;
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

                MColumn model = null;

                // Si debo importar este objeto
                if (importObject){

                    // Verifico nuevamente si columna no existe para tabla ya que se puede haber creado la tabla en este mismo proceso y esta ser una columna
                    // de auditoria o de documento (y por lo tanto ya se agrego automaticamente en el aftersave de la tabla).
                    int adTableIDAux = adColumn.getParentID();
                    if (this.hashTablas.containsKey(adColumn.getParentID())){
                        adTableIDAux = this.hashTablas.get(adColumn.getParentID()).intValue();
                    }
                    sql = " select ad_column_id from ad_column where ad_table_id =" + adTableIDAux + " and lower(columnname) ='" + adColumn.getColumnName().toLowerCase() + "'";
                    int adColumnIDAux = DB.getSQLValueEx(null, sql);

                    // Si columna no existe
                    if (adColumnIDAux <= 0){
                        // Creo nuevo modelo de objeto
                        model = new MColumn(getCtx(), 0, null);
                        model.setAD_Org_ID(0);
                        model.setName(adColumn.getName());
                        model.setDescription(adColumn.getDescription());
                        model.setHelp(adColumn.getHelp());
                        model.setEntityType(adColumn.getEntityType());
                        model.setColumnName(adColumn.getColumnName());
                        model.setAD_Table_ID(adTableIDAux);
                        model.setAD_Reference_ID(adColumn.getAD_Reference_ID());
                        model.setFieldLength(adColumn.getFieldLength());
                        model.setDefaultValue(adColumn.getDefaultValue());
                        model.setIsKey(adColumn.getIsKey());
                        model.setIsParent(adColumn.getIsParent());
                        model.setIsMandatory(adColumn.getIsMandatory());
                        model.setIsUpdateable(adColumn.getIsUpdateable());
                        model.setReadOnlyLogic(adColumn.getReadOnlyLogic());
                        model.setIsIdentifier(adColumn.getIsIdentifier());
                        model.setIsTranslated(adColumn.getIsTranslated());
                        model.setIsEncrypted(adColumn.getIsEncrypted());
                        model.setCallout(adColumn.getCallout());
                        model.setVFormat(adColumn.getVFormat());
                        model.setValueMin(adColumn.getValueMin());
                        model.setValueMax(adColumn.getValueMax());
                        model.setIsSelectionColumn(adColumn.getIsSelectionColumn());
                        model.setIsAlwaysUpdateable(adColumn.getIsAlwaysUpdateable());
                        model.setColumnSQL(adColumn.getColumnSQL());
                        model.setMandatoryLogic(adColumn.getMandatoryLogic());
                        model.setInfoFactoryClass(adColumn.getInfoFactoryClass());
                        model.setIsAutocomplete(adColumn.getIsAutocomplete());
                        model.setIsAllowLogging(adColumn.getIsAllowLogging());
                        model.setFormatPattern(adColumn.getFormatPattern());
                        model.setIsRange(adColumn.getIsRange());
                        model.setIsAllowCopy(adColumn.getIsAllowCopy());
                        model.setSeqNo(adColumn.getSeqNo());

                        if (adColumn.getAD_Reference_Value_ID() > 0){
                            if (this.hashReferencias.containsKey(adColumn.getAD_Reference_Value_ID())){
                                model.setAD_Reference_Value_ID(this.hashReferencias.get(adColumn.getAD_Reference_Value_ID()));
                            }
                            else {
                                model.setAD_Reference_Value_ID(adColumn.getAD_Reference_Value_ID());
                            }
                        }

                        if (adColumn.getAD_Val_Rule_ID() > 0){
                            if (this.hashValidaciones.containsKey(adColumn.getAD_Val_Rule_ID())){
                                model.setAD_Val_Rule_ID(this.hashValidaciones.get(adColumn.getAD_Val_Rule_ID()));
                            }
                            else {
                                model.setAD_Val_Rule_ID(adColumn.getAD_Val_Rule_ID());
                            }
                        }

                        if (!adColumn.getColumnName().equalsIgnoreCase("DocAction")){
                            if (adColumn.getAD_Process_ID() > 0){
                                if (this.hashProcesos.containsKey(adColumn.getAD_Process_ID())){
                                    model.setAD_Process_ID(this.hashProcesos.get(adColumn.getAD_Process_ID()));
                                }
                                else {
                                    model.setAD_Process_ID(adColumn.getAD_Process_ID());
                                }
                            }
                        }

                        if (this.hashElementos.containsKey(adColumn.getAD_Element_ID())){
                            model.setAD_Element_ID(this.hashElementos.get(adColumn.getAD_Element_ID()).intValue());
                        }
                        else {
                            model.setAD_Element_ID(adColumn.getAD_Element_ID());
                        }

                        // Me aseguro columna UUID que no sea de solo lectura
                        if (model.getColumnName().equalsIgnoreCase("UUID")){
                            model.setIsMandatory(false);
                        }

                        model.saveEx();

                        // Sincronizo columna con DB
                        model.syncDatabase();

                        model.saveEx();

                        // Guardo ID del objeto creado en linea de migración.
                        sysMigracionLin.setDestino_ID(model.get_ID());
                        sysMigracionLin.setExisteItem(true);
                    }
                    else {

                        // Obtengo modelo existente en base destino según ID destino
                        model = new MColumn(getCtx(), adColumnIDAux, null);

                        // Modifico atributos
                        model.setName(adColumn.getName());
                        model.setDescription(adColumn.getDescription());
                        model.setHelp(adColumn.getHelp());
                        model.setEntityType(adColumn.getEntityType());
                        model.setAD_Reference_ID(adColumn.getAD_Reference_ID());
                        model.setIsKey(adColumn.getIsKey());
                        model.setIsMandatory(adColumn.getIsMandatory());
                        model.setIsUpdateable(adColumn.getIsUpdateable());
                        model.setIsAlwaysUpdateable(adColumn.getIsAlwaysUpdateable());
                        model.setFieldLength(adColumn.getFieldLength());
                        model.setDefaultValue(adColumn.getDefaultValue());
                        model.setIsParent(adColumn.getIsParent());
                        model.setReadOnlyLogic(adColumn.getReadOnlyLogic());
                        model.setIsIdentifier(adColumn.getIsIdentifier());
                        model.setIsTranslated(adColumn.getIsTranslated());
                        model.setIsEncrypted(adColumn.getIsEncrypted());
                        model.setCallout(adColumn.getCallout());
                        model.setVFormat(adColumn.getVFormat());
                        model.setValueMin(adColumn.getValueMin());
                        model.setValueMax(adColumn.getValueMax());
                        model.setIsSelectionColumn(adColumn.getIsSelectionColumn());
                        model.setColumnSQL(adColumn.getColumnSQL());
                        model.setMandatoryLogic(adColumn.getMandatoryLogic());
                        model.setInfoFactoryClass(adColumn.getInfoFactoryClass());
                        model.setIsAutocomplete(adColumn.getIsAutocomplete());
                        model.setIsAllowLogging(adColumn.getIsAllowLogging());
                        model.setFormatPattern(adColumn.getFormatPattern());
                        model.setIsRange(adColumn.getIsRange());
                        model.setIsAllowCopy(adColumn.getIsAllowCopy());

                        if (adColumn.getAD_Reference_Value_ID() > 0){
                            if (this.hashReferencias.containsKey(adColumn.getAD_Reference_Value_ID())){
                                model.setAD_Reference_Value_ID(this.hashReferencias.get(adColumn.getAD_Reference_Value_ID()));
                            }
                            else {
                                model.setAD_Reference_Value_ID(adColumn.getAD_Reference_Value_ID());
                            }
                        }

                        if (adColumn.getAD_Val_Rule_ID() > 0){
                            if (this.hashValidaciones.containsKey(adColumn.getAD_Val_Rule_ID())){
                                model.setAD_Val_Rule_ID(this.hashValidaciones.get(adColumn.getAD_Val_Rule_ID()));
                            }
                            else {
                                model.setAD_Val_Rule_ID(adColumn.getAD_Val_Rule_ID());
                            }
                        }

                        if (!adColumn.getColumnName().equalsIgnoreCase("DocAction")){
                            if (adColumn.getAD_Process_ID() > 0){
                                if (this.hashProcesos.containsKey(adColumn.getAD_Process_ID())){
                                    model.setAD_Process_ID(this.hashProcesos.get(adColumn.getAD_Process_ID()));
                                }
                                else {
                                    model.setAD_Process_ID(adColumn.getAD_Process_ID());
                                }
                            }
                        }

                        // Me aseguro columna UUID que no sea de solo lectura
                        if (model.getColumnName().equalsIgnoreCase("UUID")){
                            model.setIsMandatory(false);
                        }

                        model.saveEx();

                        // Sincronizo columna con DB
                        model.syncDatabase();

                        model.saveEx();

                        // Guardo ID del objeto creado en linea de migración.
                        sysMigracionLin.setDestino_ID(adColumnIDAux);
                        sysMigracionLin.setExisteItem(true);
                    }

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new MColumn(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Column.Table_Name + " - " + adColumn.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    // Modifico atributos
                    model.setName(adColumn.getName());
                    model.setDescription(adColumn.getDescription());
                    model.setHelp(adColumn.getHelp());
                    model.setEntityType(adColumn.getEntityType());
                    model.setAD_Reference_ID(adColumn.getAD_Reference_ID());
                    model.setFieldLength(adColumn.getFieldLength());
                    model.setDefaultValue(adColumn.getDefaultValue());
                    model.setIsKey(adColumn.getIsKey());
                    model.setIsMandatory(adColumn.getIsMandatory());
                    model.setIsUpdateable(adColumn.getIsUpdateable());
                    model.setIsAlwaysUpdateable(adColumn.getIsAlwaysUpdateable());
                    model.setIsParent(adColumn.getIsParent());
                    model.setReadOnlyLogic(adColumn.getReadOnlyLogic());
                    model.setIsIdentifier(adColumn.getIsIdentifier());
                    model.setIsTranslated(adColumn.getIsTranslated());
                    model.setIsEncrypted(adColumn.getIsEncrypted());
                    model.setCallout(adColumn.getCallout());
                    model.setVFormat(adColumn.getVFormat());
                    model.setValueMin(adColumn.getValueMin());
                    model.setValueMax(adColumn.getValueMax());
                    model.setIsSelectionColumn(adColumn.getIsSelectionColumn());
                    model.setColumnSQL(adColumn.getColumnSQL());
                    model.setMandatoryLogic(adColumn.getMandatoryLogic());
                    model.setInfoFactoryClass(adColumn.getInfoFactoryClass());
                    model.setIsAutocomplete(adColumn.getIsAutocomplete());
                    model.setIsAllowLogging(adColumn.getIsAllowLogging());
                    model.setFormatPattern(adColumn.getFormatPattern());
                    model.setIsRange(adColumn.getIsRange());
                    model.setIsAllowCopy(adColumn.getIsAllowCopy());

                    if (adColumn.getAD_Reference_Value_ID() > 0){
                        if (this.hashReferencias.containsKey(adColumn.getAD_Reference_Value_ID())){
                            model.setAD_Reference_Value_ID(this.hashReferencias.get(adColumn.getAD_Reference_Value_ID()));
                        }
                        else {
                            model.setAD_Reference_Value_ID(adColumn.getAD_Reference_Value_ID());
                        }
                    }

                    if (adColumn.getAD_Val_Rule_ID() > 0){
                        if (this.hashValidaciones.containsKey(adColumn.getAD_Val_Rule_ID())){
                            model.setAD_Val_Rule_ID(this.hashValidaciones.get(adColumn.getAD_Val_Rule_ID()));
                        }
                        else {
                            model.setAD_Val_Rule_ID(adColumn.getAD_Val_Rule_ID());
                        }
                    }

                    if (!adColumn.getColumnName().equalsIgnoreCase("DocAction")){
                        if (adColumn.getAD_Process_ID() > 0){
                            if (this.hashProcesos.containsKey(adColumn.getAD_Process_ID())){
                                model.setAD_Process_ID(this.hashProcesos.get(adColumn.getAD_Process_ID()));
                            }
                            else {
                                model.setAD_Process_ID(adColumn.getAD_Process_ID());
                            }
                        }
                    }

                    model.saveEx();

                    // Sincronizo columna con DB
                    model.syncDatabase();

                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

                // Agrego asociación de ID origen con ID destino
                this.hashColumnas.put(sysMigracionLin.getRecord_ID(), sysMigracionLin.getDestino_ID());

                // Si importa traduccion
                if (importTraduccion){
                    // Lo hago
                    this.importTraducciones(X_AD_Column.Table_Name, model.get_ID(), adColumn.getTraduccionList());
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
                        continue;
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
                    model = new X_AD_Ref_List(getCtx(), 0, null);
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
                    sysMigracionLin.setExisteItem(true);

                }
                else {
                    // Obtengo modelo existente en base destino según ID destino
                    model = new X_AD_Ref_List(getCtx(), sysMigracionLin.getDestino_ID(), null);

                    if ((model == null) || (model.get_ID() <= 0)){
                        sysMigracionLin.setMessage("No se pudo importar : " + X_AD_Ref_List.Table_Name + " - " + adRefList.get_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    model.setValue(adRefList.getValue());
                    model.setName(adRefList.getName());
                    model.setDescription(adRefList.getDescription());
                    model.setValidFrom(adRefList.getValidFrom());
                    model.setValidTo(adRefList.getValidTo());
                    model.setEntityType(adRefList.getEntityType());
                    model.saveEx();
                }

                sysMigracionLin.setMessage("OK");
                sysMigracionLin.saveEx();

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

        String sql = null;

        try{

            for (ADRef_Table adRefTable: this.cabezalMigracion.getRefTableList()){

                MZSysMigracionLin sysMigracionLin = this.getLineByTableRecord(X_AD_Ref_Table.Table_ID, adRefTable.getAD_Reference_ID());
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
                        continue;
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
                        sysMigracionLin.setMessage("No se pudo obtener padre desde hash para : " + X_AD_Ref_Table.Table_Name + " - " + adRefTable.getAD_Reference_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    // Busco tabla destino de esta referencia
                    sql = " select ad_table_id from ad_table where tablename='" + adRefTable.getNombreTabla() + "'";
                    int adTableIDAux = DB.getSQLValueEx(null, sql);
                    if (adTableIDAux <= 0){
                        sysMigracionLin.setMessage("No se pudo obtener Tabla para : " + X_AD_Ref_Table.Table_Name + " - " + adRefTable.getAD_Reference_ID());
                        sysMigracionLin.saveEx();
                        continue;
                    }

                    // Busco columna key destino de esta referencia
                    int adColKeyIDAux = 0;
                    if ((adRefTable.getNombreColKey() != null) && (!adRefTable.getNombreColKey().trim().equalsIgnoreCase(""))){
                        sql = " select ad_column_id from ad_column where ad_table_id =" + adTableIDAux + " and columnname='" + adRefTable.getNombreColKey() + "'";
                        adColKeyIDAux = DB.getSQLValueEx(null, sql);
                        if (adColKeyIDAux <= 0){
                            sysMigracionLin.setMessage("No se pudo obtener Columna Kay para : " + X_AD_Ref_Table.Table_Name + " - " + adRefTable.getAD_Reference_ID());
                            sysMigracionLin.saveEx();
                            continue;
                        }
                    }

                    // Busco columna display destino de esta referencia
                    int adColDisplayIDAux = 0;
                    if ((adRefTable.getNombreColDisplay() != null) && (!adRefTable.getNombreColDisplay().trim().equalsIgnoreCase(""))){
                        sql = " select ad_column_id from ad_column where ad_table_id =" + adTableIDAux + " and columnname='" + adRefTable.getNombreColDisplay() + "'";
                        adColDisplayIDAux = DB.getSQLValueEx(null, sql);
                        if (adColDisplayIDAux <= 0){
                            sysMigracionLin.setMessage("No se pudo obtener Columna Display para : " + X_AD_Ref_Table.Table_Name + " - " + adRefTable.getAD_Reference_ID());
                            sysMigracionLin.saveEx();
                            continue;
                        }
                    }

                    // Busco ventana destino de esta referencia
                    int adWindowIDAux = 0;
                    if ((adRefTable.getNombreVentana() != null) && (!adRefTable.getNombreVentana().trim().equalsIgnoreCase(""))){
                        sql = " select ad_window_id from ad_window where name='" + adRefTable.getNombreVentana() + "'";
                        adWindowIDAux = DB.getSQLValueEx(null, sql);
                    }

                    // Creo nuevo modelo de objeto
                    model = new X_AD_Ref_Table(getCtx(), 0, null);
                    model.setAD_Org_ID(0);
                    model.setAD_Reference_ID(this.hashReferencias.get(adRefTable.getParentID()));
                    model.setAD_Table_ID(adTableIDAux);

                    if (adColKeyIDAux > 0){
                        model.setAD_Key(adColKeyIDAux);
                    }

                    if (adColDisplayIDAux > 0){
                        model.setAD_Display(adColDisplayIDAux);
                    }

                    model.setIsValueDisplayed(adRefTable.getIsValueDisplayed());
                    model.setWhereClause(adRefTable.getWhereClause());
                    model.setOrderByClause(adRefTable.getOrderByClause());
                    model.setEntityType(adRefTable.getEntityType());

                    if (adWindowIDAux > 0){
                        model.setAD_Window_ID(adWindowIDAux);
                    }

                    model.setIsAlert(adRefTable.getIsAlert());
                    model.setDisplaySQL(adRefTable.getDisplaySQL());
                    model.setIsDisplayIdentifier(adRefTable.isDisplayIdentifier());
                    model.saveEx();

                    // Guardo ID del objeto creado en linea de migración.
                    sysMigracionLin.setDestino_ID(model.getAD_Reference_ID());
                    sysMigracionLin.setExisteItem(true);
                    sysMigracionLin.setMessage("OK");
                    sysMigracionLin.saveEx();
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

        MZSysMigracionLin model = new Query(getCtx(), I_Z_Sys_MigracionLin.Table_Name, whereClause, null).first();

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
