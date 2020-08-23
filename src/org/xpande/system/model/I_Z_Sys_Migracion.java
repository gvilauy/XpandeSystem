/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/
package org.xpande.system.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for Z_Sys_Migracion
 *  @author Adempiere (generated) 
 *  @version Release 3.9.0
 */
public interface I_Z_Sys_Migracion 
{

    /** TableName=Z_Sys_Migracion */
    public static final String Table_Name = "Z_Sys_Migracion";

    /** AD_Table_ID=1000226 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 4 - System 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(4);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name DateDoc */
    public static final String COLUMNNAME_DateDoc = "DateDoc";

	/** Set Document Date.
	  * Date of the Document
	  */
	public void setDateDoc (Timestamp DateDoc);

	/** Get Document Date.
	  * Date of the Document
	  */
	public Timestamp getDateDoc();

    /** Column name EntityType */
    public static final String COLUMNNAME_EntityType = "EntityType";

	/** Set Entity Type.
	  * Dictionary Entity Type;
 Determines ownership and synchronization
	  */
	public void setEntityType (String EntityType);

	/** Get Entity Type.
	  * Dictionary Entity Type;
 Determines ownership and synchronization
	  */
	public String getEntityType();

    /** Column name FilePathOrName */
    public static final String COLUMNNAME_FilePathOrName = "FilePathOrName";

	/** Set File Path or Name.
	  * Path of directory or name of the local file or URL
	  */
	public void setFilePathOrName (String FilePathOrName);

	/** Get File Path or Name.
	  * Path of directory or name of the local file or URL
	  */
	public String getFilePathOrName();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name IsDictionary */
    public static final String COLUMNNAME_IsDictionary = "IsDictionary";

	/** Set IsDictionary.
	  * Si es o no un elemento del diccionario
	  */
	public void setIsDictionary (boolean IsDictionary);

	/** Get IsDictionary.
	  * Si es o no un elemento del diccionario
	  */
	public boolean isDictionary();

    /** Column name IsTranslated */
    public static final String COLUMNNAME_IsTranslated = "IsTranslated";

	/** Set Translated.
	  * This column is translated
	  */
	public void setIsTranslated (boolean IsTranslated);

	/** Get Translated.
	  * This column is translated
	  */
	public boolean isTranslated();

    /** Column name MarcarRecursivo */
    public static final String COLUMNNAME_MarcarRecursivo = "MarcarRecursivo";

	/** Set MarcarRecursivo.
	  * Si se marca o no de manera recursiva
	  */
	public void setMarcarRecursivo (boolean MarcarRecursivo);

	/** Get MarcarRecursivo.
	  * Si se marca o no de manera recursiva
	  */
	public boolean isMarcarRecursivo();

    /** Column name MigElemento */
    public static final String COLUMNNAME_MigElemento = "MigElemento";

	/** Set MigElemento.
	  * Si se desea migrar o no elementos del diccionario
	  */
	public void setMigElemento (boolean MigElemento);

	/** Get MigElemento.
	  * Si se desea migrar o no elementos del diccionario
	  */
	public boolean isMigElemento();

    /** Column name MigProceso */
    public static final String COLUMNNAME_MigProceso = "MigProceso";

	/** Set MigProceso.
	  * Si se desea o no migrar procesos
	  */
	public void setMigProceso (boolean MigProceso);

	/** Get MigProceso.
	  * Si se desea o no migrar procesos
	  */
	public boolean isMigProceso();

    /** Column name MigReferencia */
    public static final String COLUMNNAME_MigReferencia = "MigReferencia";

	/** Set MigReferencia.
	  * Si se desea o no migrar referencias
	  */
	public void setMigReferencia (boolean MigReferencia);

	/** Get MigReferencia.
	  * Si se desea o no migrar referencias
	  */
	public boolean isMigReferencia();

    /** Column name MigTabla */
    public static final String COLUMNNAME_MigTabla = "MigTabla";

	/** Set MigTabla.
	  * Si se desea o no migrar Tablas
	  */
	public void setMigTabla (boolean MigTabla);

	/** Get MigTabla.
	  * Si se desea o no migrar Tablas
	  */
	public boolean isMigTabla();

    /** Column name MigValidacion */
    public static final String COLUMNNAME_MigValidacion = "MigValidacion";

	/** Set MigValidacion.
	  * Si se desea o no migrar validaciones
	  */
	public void setMigValidacion (boolean MigValidacion);

	/** Get MigValidacion.
	  * Si se desea o no migrar validaciones
	  */
	public boolean isMigValidacion();

    /** Column name MigVentana */
    public static final String COLUMNNAME_MigVentana = "MigVentana";

	/** Set MigVentana.
	  * Si se desea o no migrar ventanas
	  */
	public void setMigVentana (boolean MigVentana);

	/** Get MigVentana.
	  * Si se desea o no migrar ventanas
	  */
	public boolean isMigVentana();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name ProcessButton */
    public static final String COLUMNNAME_ProcessButton = "ProcessButton";

	/** Set ProcessButton	  */
	public void setProcessButton (String ProcessButton);

	/** Get ProcessButton	  */
	public String getProcessButton();

    /** Column name ProcessButton2 */
    public static final String COLUMNNAME_ProcessButton2 = "ProcessButton2";

	/** Set ProcessButton2.
	  * Botón de Proceso
	  */
	public void setProcessButton2 (String ProcessButton2);

	/** Get ProcessButton2.
	  * Botón de Proceso
	  */
	public String getProcessButton2();

    /** Column name ProcessButton3 */
    public static final String COLUMNNAME_ProcessButton3 = "ProcessButton3";

	/** Set ProcessButton3.
	  * Botón para proceso
	  */
	public void setProcessButton3 (String ProcessButton3);

	/** Get ProcessButton3.
	  * Botón para proceso
	  */
	public String getProcessButton3();

    /** Column name ProcessButton4 */
    public static final String COLUMNNAME_ProcessButton4 = "ProcessButton4";

	/** Set ProcessButton4.
	  * Botón de Proceso
	  */
	public void setProcessButton4 (String ProcessButton4);

	/** Get ProcessButton4.
	  * Botón de Proceso
	  */
	public String getProcessButton4();

    /** Column name StartDate */
    public static final String COLUMNNAME_StartDate = "StartDate";

	/** Set Start Date.
	  * First effective day (inclusive)
	  */
	public void setStartDate (Timestamp StartDate);

	/** Get Start Date.
	  * First effective day (inclusive)
	  */
	public Timestamp getStartDate();

    /** Column name TextoFiltro */
    public static final String COLUMNNAME_TextoFiltro = "TextoFiltro";

	/** Set TextoFiltro.
	  * Texto genérico para filtro de valores
	  */
	public void setTextoFiltro (String TextoFiltro);

	/** Get TextoFiltro.
	  * Texto genérico para filtro de valores
	  */
	public String getTextoFiltro();

    /** Column name TipoSysMigra */
    public static final String COLUMNNAME_TipoSysMigra = "TipoSysMigra";

	/** Set TipoSysMigra.
	  * Tipo de acción en proceso de Migración de Diccionario
	  */
	public void setTipoSysMigra (String TipoSysMigra);

	/** Get TipoSysMigra.
	  * Tipo de acción en proceso de Migración de Diccionario
	  */
	public String getTipoSysMigra();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name UUID */
    public static final String COLUMNNAME_UUID = "UUID";

	/** Set Immutable Universally Unique Identifier.
	  * Immutable Universally Unique Identifier
	  */
	public void setUUID (String UUID);

	/** Get Immutable Universally Unique Identifier.
	  * Immutable Universally Unique Identifier
	  */
	public String getUUID();

    /** Column name VersionNo */
    public static final String COLUMNNAME_VersionNo = "VersionNo";

	/** Set Version No.
	  * Version Number
	  */
	public void setVersionNo (String VersionNo);

	/** Get Version No.
	  * Version Number
	  */
	public String getVersionNo();

    /** Column name Z_Sys_Migracion_ID */
    public static final String COLUMNNAME_Z_Sys_Migracion_ID = "Z_Sys_Migracion_ID";

	/** Set Z_Sys_Migracion ID	  */
	public void setZ_Sys_Migracion_ID (int Z_Sys_Migracion_ID);

	/** Get Z_Sys_Migracion ID	  */
	public int getZ_Sys_Migracion_ID();
}
