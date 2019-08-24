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
/** Generated Model - DO NOT CHANGE */
package org.xpande.system.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for Z_Sys_Migracion
 *  @author Adempiere (generated) 
 *  @version Release 3.9.0 - $Id$ */
public class X_Z_Sys_Migracion extends PO implements I_Z_Sys_Migracion, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190823L;

    /** Standard Constructor */
    public X_Z_Sys_Migracion (Properties ctx, int Z_Sys_Migracion_ID, String trxName)
    {
      super (ctx, Z_Sys_Migracion_ID, trxName);
      /** if (Z_Sys_Migracion_ID == 0)
        {
			setDateDoc (new Timestamp( System.currentTimeMillis() ));
// @#Date@
			setMigElemento (false);
// N
			setMigProceso (false);
// N
			setMigReferencia (false);
// N
			setMigTabla (false);
// N
			setMigValidacion (false);
// N
			setMigVentana (false);
// N
			setTipoSysMigra (null);
// EXPORTAR
			setZ_Sys_Migracion_ID (0);
        } */
    }

    /** Load Constructor */
    public X_Z_Sys_Migracion (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_Z_Sys_Migracion[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Document Date.
		@param DateDoc 
		Date of the Document
	  */
	public void setDateDoc (Timestamp DateDoc)
	{
		set_Value (COLUMNNAME_DateDoc, DateDoc);
	}

	/** Get Document Date.
		@return Date of the Document
	  */
	public Timestamp getDateDoc () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateDoc);
	}

	/** EntityType AD_Reference_ID=389 */
	public static final int ENTITYTYPE_AD_Reference_ID=389;
	/** Set Entity Type.
		@param EntityType 
		Dictionary Entity Type; Determines ownership and synchronization
	  */
	public void setEntityType (String EntityType)
	{

		set_Value (COLUMNNAME_EntityType, EntityType);
	}

	/** Get Entity Type.
		@return Dictionary Entity Type; Determines ownership and synchronization
	  */
	public String getEntityType () 
	{
		return (String)get_Value(COLUMNNAME_EntityType);
	}

	/** Set MigElemento.
		@param MigElemento 
		Si se desea migrar o no elementos del diccionario
	  */
	public void setMigElemento (boolean MigElemento)
	{
		set_Value (COLUMNNAME_MigElemento, Boolean.valueOf(MigElemento));
	}

	/** Get MigElemento.
		@return Si se desea migrar o no elementos del diccionario
	  */
	public boolean isMigElemento () 
	{
		Object oo = get_Value(COLUMNNAME_MigElemento);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set MigProceso.
		@param MigProceso 
		Si se desea o no migrar procesos
	  */
	public void setMigProceso (boolean MigProceso)
	{
		set_Value (COLUMNNAME_MigProceso, Boolean.valueOf(MigProceso));
	}

	/** Get MigProceso.
		@return Si se desea o no migrar procesos
	  */
	public boolean isMigProceso () 
	{
		Object oo = get_Value(COLUMNNAME_MigProceso);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set MigReferencia.
		@param MigReferencia 
		Si se desea o no migrar referencias
	  */
	public void setMigReferencia (boolean MigReferencia)
	{
		set_Value (COLUMNNAME_MigReferencia, Boolean.valueOf(MigReferencia));
	}

	/** Get MigReferencia.
		@return Si se desea o no migrar referencias
	  */
	public boolean isMigReferencia () 
	{
		Object oo = get_Value(COLUMNNAME_MigReferencia);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set MigTabla.
		@param MigTabla 
		Si se desea o no migrar Tablas
	  */
	public void setMigTabla (boolean MigTabla)
	{
		set_Value (COLUMNNAME_MigTabla, Boolean.valueOf(MigTabla));
	}

	/** Get MigTabla.
		@return Si se desea o no migrar Tablas
	  */
	public boolean isMigTabla () 
	{
		Object oo = get_Value(COLUMNNAME_MigTabla);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set MigValidacion.
		@param MigValidacion 
		Si se desea o no migrar validaciones
	  */
	public void setMigValidacion (boolean MigValidacion)
	{
		set_Value (COLUMNNAME_MigValidacion, Boolean.valueOf(MigValidacion));
	}

	/** Get MigValidacion.
		@return Si se desea o no migrar validaciones
	  */
	public boolean isMigValidacion () 
	{
		Object oo = get_Value(COLUMNNAME_MigValidacion);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set MigVentana.
		@param MigVentana 
		Si se desea o no migrar ventanas
	  */
	public void setMigVentana (boolean MigVentana)
	{
		set_Value (COLUMNNAME_MigVentana, Boolean.valueOf(MigVentana));
	}

	/** Get MigVentana.
		@return Si se desea o no migrar ventanas
	  */
	public boolean isMigVentana () 
	{
		Object oo = get_Value(COLUMNNAME_MigVentana);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	/** Set ProcessButton.
		@param ProcessButton ProcessButton	  */
	public void setProcessButton (String ProcessButton)
	{
		set_Value (COLUMNNAME_ProcessButton, ProcessButton);
	}

	/** Get ProcessButton.
		@return ProcessButton	  */
	public String getProcessButton () 
	{
		return (String)get_Value(COLUMNNAME_ProcessButton);
	}

	/** Set ProcessButton2.
		@param ProcessButton2 
		Botón de Proceso
	  */
	public void setProcessButton2 (String ProcessButton2)
	{
		set_Value (COLUMNNAME_ProcessButton2, ProcessButton2);
	}

	/** Get ProcessButton2.
		@return Botón de Proceso
	  */
	public String getProcessButton2 () 
	{
		return (String)get_Value(COLUMNNAME_ProcessButton2);
	}

	/** Set Start Date.
		@param StartDate 
		First effective day (inclusive)
	  */
	public void setStartDate (Timestamp StartDate)
	{
		set_Value (COLUMNNAME_StartDate, StartDate);
	}

	/** Get Start Date.
		@return First effective day (inclusive)
	  */
	public Timestamp getStartDate () 
	{
		return (Timestamp)get_Value(COLUMNNAME_StartDate);
	}

	/** Set TextoFiltro.
		@param TextoFiltro 
		Texto genérico para filtro de valores
	  */
	public void setTextoFiltro (String TextoFiltro)
	{
		set_Value (COLUMNNAME_TextoFiltro, TextoFiltro);
	}

	/** Get TextoFiltro.
		@return Texto genérico para filtro de valores
	  */
	public String getTextoFiltro () 
	{
		return (String)get_Value(COLUMNNAME_TextoFiltro);
	}

	/** TipoSysMigra AD_Reference_ID=1000056 */
	public static final int TIPOSYSMIGRA_AD_Reference_ID=1000056;
	/** EXPORTAR = EXPORTAR */
	public static final String TIPOSYSMIGRA_EXPORTAR = "EXPORTAR";
	/** IMPORTAR = IMPORTAR */
	public static final String TIPOSYSMIGRA_IMPORTAR = "IMPORTAR";
	/** Set TipoSysMigra.
		@param TipoSysMigra 
		Tipo de acción en proceso de Migración de Diccionario
	  */
	public void setTipoSysMigra (String TipoSysMigra)
	{

		set_Value (COLUMNNAME_TipoSysMigra, TipoSysMigra);
	}

	/** Get TipoSysMigra.
		@return Tipo de acción en proceso de Migración de Diccionario
	  */
	public String getTipoSysMigra () 
	{
		return (String)get_Value(COLUMNNAME_TipoSysMigra);
	}

	/** Set Immutable Universally Unique Identifier.
		@param UUID 
		Immutable Universally Unique Identifier
	  */
	public void setUUID (String UUID)
	{
		set_Value (COLUMNNAME_UUID, UUID);
	}

	/** Get Immutable Universally Unique Identifier.
		@return Immutable Universally Unique Identifier
	  */
	public String getUUID () 
	{
		return (String)get_Value(COLUMNNAME_UUID);
	}

	/** Set Version No.
		@param VersionNo 
		Version Number
	  */
	public void setVersionNo (String VersionNo)
	{
		set_Value (COLUMNNAME_VersionNo, VersionNo);
	}

	/** Get Version No.
		@return Version Number
	  */
	public String getVersionNo () 
	{
		return (String)get_Value(COLUMNNAME_VersionNo);
	}

	/** Set Z_Sys_Migracion ID.
		@param Z_Sys_Migracion_ID Z_Sys_Migracion ID	  */
	public void setZ_Sys_Migracion_ID (int Z_Sys_Migracion_ID)
	{
		if (Z_Sys_Migracion_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_Z_Sys_Migracion_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Z_Sys_Migracion_ID, Integer.valueOf(Z_Sys_Migracion_ID));
	}

	/** Get Z_Sys_Migracion ID.
		@return Z_Sys_Migracion ID	  */
	public int getZ_Sys_Migracion_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_Sys_Migracion_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}