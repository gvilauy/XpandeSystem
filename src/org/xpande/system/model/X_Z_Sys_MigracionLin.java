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

/** Generated Model for Z_Sys_MigracionLin
 *  @author Adempiere (generated) 
 *  @version Release 3.9.0 - $Id$ */
public class X_Z_Sys_MigracionLin extends PO implements I_Z_Sys_MigracionLin, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190823L;

    /** Standard Constructor */
    public X_Z_Sys_MigracionLin (Properties ctx, int Z_Sys_MigracionLin_ID, String trxName)
    {
      super (ctx, Z_Sys_MigracionLin_ID, trxName);
      /** if (Z_Sys_MigracionLin_ID == 0)
        {
			setAD_Table_ID (0);
			setIsSelected (false);
// N
			setName (null);
			setRecord_ID (0);
			setTipoSysMigraObj (null);
			setZ_Sys_Migracion_ID (0);
			setZ_Sys_MigracionLin_ID (0);
        } */
    }

    /** Load Constructor */
    public X_Z_Sys_MigracionLin (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_Z_Sys_MigracionLin[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_AD_Table getAD_Table() throws RuntimeException
    {
		return (I_AD_Table)MTable.get(getCtx(), I_AD_Table.Table_Name)
			.getPO(getAD_Table_ID(), get_TrxName());	}

	/** Set Table.
		@param AD_Table_ID 
		Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1) 
			set_Value (COLUMNNAME_AD_Table_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get Table.
		@return Database Table information
	  */
	public int getAD_Table_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set Selected.
		@param IsSelected Selected	  */
	public void setIsSelected (boolean IsSelected)
	{
		set_Value (COLUMNNAME_IsSelected, Boolean.valueOf(IsSelected));
	}

	/** Get Selected.
		@return Selected	  */
	public boolean isSelected () 
	{
		Object oo = get_Value(COLUMNNAME_IsSelected);
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

	/** Set Parent.
		@param Parent_ID 
		Parent of Entity
	  */
	public void setParent_ID (int Parent_ID)
	{
		if (Parent_ID < 1) 
			set_Value (COLUMNNAME_Parent_ID, null);
		else 
			set_Value (COLUMNNAME_Parent_ID, Integer.valueOf(Parent_ID));
	}

	/** Get Parent.
		@return Parent of Entity
	  */
	public int getParent_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Parent_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ParentName.
		@param ParentName 
		Nombre del padre
	  */
	public void setParentName (String ParentName)
	{
		set_Value (COLUMNNAME_ParentName, ParentName);
	}

	/** Get ParentName.
		@return Nombre del padre
	  */
	public String getParentName () 
	{
		return (String)get_Value(COLUMNNAME_ParentName);
	}

	/** Set Record ID.
		@param Record_ID 
		Direct internal record ID
	  */
	public void setRecord_ID (int Record_ID)
	{
		if (Record_ID < 0) 
			set_Value (COLUMNNAME_Record_ID, null);
		else 
			set_Value (COLUMNNAME_Record_ID, Integer.valueOf(Record_ID));
	}

	/** Get Record ID.
		@return Direct internal record ID
	  */
	public int getRecord_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** TipoSysMigraObj AD_Reference_ID=1000057 */
	public static final int TIPOSYSMIGRAOBJ_AD_Reference_ID=1000057;
	/** VENTANA = VENTANA */
	public static final String TIPOSYSMIGRAOBJ_VENTANA = "VENTANA";
	/** TABLA = TABLA */
	public static final String TIPOSYSMIGRAOBJ_TABLA = "TABLA";
	/** PROCESO = PROCESO */
	public static final String TIPOSYSMIGRAOBJ_PROCESO = "PROCESO";
	/** REFERENCIA = REFERENCIA */
	public static final String TIPOSYSMIGRAOBJ_REFERENCIA = "REFERENCIA";
	/** VALIDACION = VALIDACION */
	public static final String TIPOSYSMIGRAOBJ_VALIDACION = "VALIDACION";
	/** ELEMENTO = ELEMENTO */
	public static final String TIPOSYSMIGRAOBJ_ELEMENTO = "ELEMENTO";
	/** Set TipoSysMigraObj.
		@param TipoSysMigraObj 
		Tipo de objeto de diccionario de datos en proceso de migración
	  */
	public void setTipoSysMigraObj (String TipoSysMigraObj)
	{

		set_Value (COLUMNNAME_TipoSysMigraObj, TipoSysMigraObj);
	}

	/** Get TipoSysMigraObj.
		@return Tipo de objeto de diccionario de datos en proceso de migración
	  */
	public String getTipoSysMigraObj () 
	{
		return (String)get_Value(COLUMNNAME_TipoSysMigraObj);
	}

	/** TipoSysMigraObjFrom AD_Reference_ID=1000057 */
	public static final int TIPOSYSMIGRAOBJFROM_AD_Reference_ID=1000057;
	/** VENTANA = VENTANA */
	public static final String TIPOSYSMIGRAOBJFROM_VENTANA = "VENTANA";
	/** TABLA = TABLA */
	public static final String TIPOSYSMIGRAOBJFROM_TABLA = "TABLA";
	/** PROCESO = PROCESO */
	public static final String TIPOSYSMIGRAOBJFROM_PROCESO = "PROCESO";
	/** REFERENCIA = REFERENCIA */
	public static final String TIPOSYSMIGRAOBJFROM_REFERENCIA = "REFERENCIA";
	/** VALIDACION = VALIDACION */
	public static final String TIPOSYSMIGRAOBJFROM_VALIDACION = "VALIDACION";
	/** ELEMENTO = ELEMENTO */
	public static final String TIPOSYSMIGRAOBJFROM_ELEMENTO = "ELEMENTO";
	/** Set TipoSysMigraObjFrom.
		@param TipoSysMigraObjFrom 
		Tipo de objeto (fuente) de diccionario de datos en proceso de migración
	  */
	public void setTipoSysMigraObjFrom (String TipoSysMigraObjFrom)
	{

		set_Value (COLUMNNAME_TipoSysMigraObjFrom, TipoSysMigraObjFrom);
	}

	/** Get TipoSysMigraObjFrom.
		@return Tipo de objeto (fuente) de diccionario de datos en proceso de migración
	  */
	public String getTipoSysMigraObjFrom () 
	{
		return (String)get_Value(COLUMNNAME_TipoSysMigraObjFrom);
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

	public I_Z_Sys_Migracion getZ_Sys_Migracion() throws RuntimeException
    {
		return (I_Z_Sys_Migracion)MTable.get(getCtx(), I_Z_Sys_Migracion.Table_Name)
			.getPO(getZ_Sys_Migracion_ID(), get_TrxName());	}

	/** Set Z_Sys_Migracion ID.
		@param Z_Sys_Migracion_ID Z_Sys_Migracion ID	  */
	public void setZ_Sys_Migracion_ID (int Z_Sys_Migracion_ID)
	{
		if (Z_Sys_Migracion_ID < 1) 
			set_Value (COLUMNNAME_Z_Sys_Migracion_ID, null);
		else 
			set_Value (COLUMNNAME_Z_Sys_Migracion_ID, Integer.valueOf(Z_Sys_Migracion_ID));
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

	/** Set Z_Sys_MigracionLin ID.
		@param Z_Sys_MigracionLin_ID Z_Sys_MigracionLin ID	  */
	public void setZ_Sys_MigracionLin_ID (int Z_Sys_MigracionLin_ID)
	{
		if (Z_Sys_MigracionLin_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_Z_Sys_MigracionLin_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_Z_Sys_MigracionLin_ID, Integer.valueOf(Z_Sys_MigracionLin_ID));
	}

	/** Get Z_Sys_MigracionLin ID.
		@return Z_Sys_MigracionLin ID	  */
	public int getZ_Sys_MigracionLin_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Z_Sys_MigracionLin_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}