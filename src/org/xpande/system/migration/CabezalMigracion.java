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
}
