package org.xpande.system.migration;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase con toda la información de un proceso de migración para serializar en archivo.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/24/19.
 */
public class CabezalMigracion {

    private List<ADElement> elementList = new ArrayList<ADElement>();
    private List<ADReference> referenceList = new ArrayList<ADReference>();
    private List<ADVal_Rule> valRuleList = new ArrayList<ADVal_Rule>();
    private List<ADReportView> reportViewList = new ArrayList<ADReportView>();
    private List<ADProcess> processList = new ArrayList<ADProcess>();
    private List<ADTable> tableList = new ArrayList<ADTable>();
    private List<ADWindow> windowList = new ArrayList<ADWindow>();
    private List<ADRef_List> refListList = new ArrayList<ADRef_List>();
    private List<ADRef_Table> refTableList = new ArrayList<ADRef_Table>();
    private List<ADColumn> columnList = new ArrayList<ADColumn>();
    private List<ADProcessPara> processParaList = new ArrayList<ADProcessPara>();
    private List<ADTab> tabList = new ArrayList<ADTab>();
    private List<ADField> fieldList = new ArrayList<ADField>();

    public List<ADRef_Table> getRefTableList() {
        return refTableList;
    }

    public void setRefTableList(List<ADRef_Table> refTableList) {
        this.refTableList = refTableList;
    }

    public List<ADRef_List> getRefListList() {
        return refListList;
    }

    public void setRefListList(List<ADRef_List> refListList) {
        this.refListList = refListList;
    }

    public List<ADElement> getElementList() {
        return elementList;
    }

    public void setElementList(List<ADElement> elementList) {
        this.elementList = elementList;
    }

    public List<ADReference> getReferenceList() {
        return referenceList;
    }

    public void setReferenceList(List<ADReference> referenceList) {
        this.referenceList = referenceList;
    }

    public List<ADVal_Rule> getValRuleList() {
        return valRuleList;
    }

    public void setValRuleList(List<ADVal_Rule> valRuleList) {
        this.valRuleList = valRuleList;
    }

    public List<ADReportView> getReportViewList() {
        return reportViewList;
    }

    public void setReportViewList(List<ADReportView> reportViewList) {
        this.reportViewList = reportViewList;
    }

    public List<ADProcess> getProcessList() {
        return processList;
    }

    public void setProcessList(List<ADProcess> processList) {
        this.processList = processList;
    }

    public List<ADTable> getTableList() {
        return tableList;
    }

    public void setTableList(List<ADTable> tableList) {
        this.tableList = tableList;
    }

    public List<ADWindow> getWindowList() {
        return windowList;
    }

    public void setWindowList(List<ADWindow> windowList) {
        this.windowList = windowList;
    }

    public List<ADColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ADColumn> columnList) {
        this.columnList = columnList;
    }

    public List<ADProcessPara> getProcessParaList() {
        return processParaList;
    }

    public void setProcessParaList(List<ADProcessPara> processParaList) {
        this.processParaList = processParaList;
    }

    public List<ADTab> getTabList() {
        return tabList;
    }

    public void setTabList(List<ADTab> tabList) {
        this.tabList = tabList;
    }

    public List<ADField> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<ADField> fieldList) {
        this.fieldList = fieldList;
    }
}
