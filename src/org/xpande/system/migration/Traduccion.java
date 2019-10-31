package org.xpande.system.migration;

import org.compiere.model.I_Persistent;

import java.sql.Timestamp;

/**
 * Clase para manejo de traducciones en migración de diccionario.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 9/9/19.
 */
public class Traduccion {

    private String language = null;
    private String name = null;
    private String description =  null;
    private String help = null;
    private String commitWarning = null;
    private Timestamp created = null;
    private Timestamp updated = null;
    private int createdBy = -1;
    private int updatedBy = -1;
    private String isTranslated = "N";
    private String printName = null;
    private String poName = null;
    private String poPrintName = null;
    private String poDescription = null;
    private String poHelp = null;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getIsTranslated() {
        return isTranslated;
    }

    public void setIsTranslated(String isTranslated) {
        this.isTranslated = isTranslated;
    }

    public String getPrintName() {
        return printName;
    }

    public void setPrintName(String printName) {
        this.printName = printName;
    }

    public String getPoName() {
        return poName;
    }

    public void setPoName(String poName) {
        this.poName = poName;
    }

    public String getPoPrintName() {
        return poPrintName;
    }

    public void setPoPrintName(String poPrintName) {
        this.poPrintName = poPrintName;
    }

    public String getPoDescription() {
        return poDescription;
    }

    public void setPoDescription(String poDescription) {
        this.poDescription = poDescription;
    }

    public String getPoHelp() {
        return poHelp;
    }

    public void setPoHelp(String poHelp) {
        this.poHelp = poHelp;
    }

    public String getCommitWarning() {
        return commitWarning;
    }

    public void setCommitWarning(String commitWarning) {
        this.commitWarning = commitWarning;
    }
}
