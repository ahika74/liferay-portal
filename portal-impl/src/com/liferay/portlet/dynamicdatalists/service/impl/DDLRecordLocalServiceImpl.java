/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.dynamicdatalists.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.portlet.dynamicdatalists.model.DDLRecordSet;
import com.liferay.portlet.dynamicdatalists.service.base.DDLRecordLocalServiceBaseImpl;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.storage.StorageEngineUtil;

import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Marcellus Tavares
 * @author Eduardo Lundgren
 */
public class DDLRecordLocalServiceImpl
	extends DDLRecordLocalServiceBaseImpl {

	public DDLRecord addRecord(
			long recordSetId, Fields fields, int displayIndex,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// Record

		DDLRecordSet recordSet = ddlRecordSetPersistence.findByPrimaryKey(
			recordSetId);

		long recordId = counterLocalService.increment();

		DDLRecord record = ddlRecordPersistence.create(recordId);

		DDMStructure ddmStructure = recordSet.getDDMStructure();

		record.setClassNameId(ddmStructure.getClassNameId());

		long classPK = StorageEngineUtil.create(
			recordSet.getCompanyId(), recordSet.getDDMStructureId(), fields,
			serviceContext);

		record.setClassPK(classPK);

		record.setDisplayIndex(displayIndex);

		record.setRecordSetId(recordSetId);

		ddlRecordPersistence.update(record, false);

		// Dynamic data mapping structure link

		long classNameId = PortalUtil.getClassNameId(DDLRecord.class);

		ddmStructureLinkLocalService.addStructureLink(
			classNameId, recordId, recordSet.getDDMStructureId(),
			serviceContext);

		return record;
	}

	public DDLRecord addRecord(
			long recordSetId, Map<String, Serializable> fieldsMap,
			int displayIndex, ServiceContext serviceContext)
		throws PortalException, SystemException {

		Fields fields = _toFields(fieldsMap);

		return addRecord(recordSetId, fields, displayIndex, serviceContext);
	}

	public void deleteRecord(DDLRecord record)
		throws PortalException, SystemException {

		// RecordSet item

		ddlRecordPersistence.remove(record);

		// Dynamic data mapping storage

		StorageEngineUtil.deleteByClass(record.getClassPK());

		// Dynamic data mapping structure link

		ddmStructureLinkLocalService.deleteClassStructureLink(
			record.getRecordId());
	}

	public void deleteRecord(long recordId)
		throws PortalException, SystemException {

		DDLRecord record = ddlRecordPersistence.findByPrimaryKey(
			recordId);

		deleteRecord(record);
	}

	public void deleteRecords(long recordSetId)
		throws PortalException, SystemException {

		List<DDLRecord> records = ddlRecordPersistence.findByRecordSetId(
			recordSetId);

		for (DDLRecord record : records) {
			deleteRecord(record);
		}
	}

	public DDLRecord getRecord(long recordId)
		throws PortalException, SystemException {

		return ddlRecordPersistence.findByPrimaryKey(recordId);
	}

	public List<DDLRecord> getRecords(long recordSetId)
		throws SystemException {

		return ddlRecordPersistence.findByRecordSetId(recordSetId);
	}

	public List<DDLRecord> getRecords(
			long recordSetId, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		return ddlRecordPersistence.findByRecordSetId(
			recordSetId, start, end, orderByComparator);
	}

	public int getRecordsCount(long recordSetId) throws SystemException {
		return ddlRecordPersistence.countByRecordSetId(recordSetId);
	}

	public DDLRecord updateRecord(
			long recordId, Fields fields, int displayIndex, boolean merge,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// Record

		DDLRecord record = ddlRecordPersistence.findByPrimaryKey(recordId);

		record.setDisplayIndex(displayIndex);

		ddlRecordPersistence.update(record, false);

		// Dynamic data mapping storage

		StorageEngineUtil.update(
			record.getClassPK(), fields, serviceContext, merge);

		return record;
	}

	public DDLRecord updateRecord(
			long recordId, Map<String, Serializable> fieldsMap,
			int displayIndex, boolean merge, ServiceContext serviceContext)
		throws PortalException, SystemException {

		Fields fields = _toFields(fieldsMap);

		return updateRecord(
			recordId, fields, displayIndex, merge, serviceContext);
	}

	private Fields _toFields(Map<String, Serializable> fieldsMap) {
		Fields fields = new Fields();

		Iterator<String> itr = fieldsMap.keySet().iterator();

		while (itr.hasNext()) {
			String fieldName = itr.next();
			String value = String.valueOf(fieldsMap.get(fieldName));

			fields.put(new Field(fieldName, value));
		}

		return fields;
	}

}