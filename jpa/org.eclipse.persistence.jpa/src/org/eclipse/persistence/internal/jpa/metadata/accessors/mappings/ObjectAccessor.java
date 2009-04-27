/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 *     05/16/2008-1.0M8 Guy Pelletier 
 *       - 218084: Implement metadata merging functionality between mapping files
 *     09/23/2008-1.1 Guy Pelletier 
 *       - 241651: JPA 2.0 Access Type support
 *     02/06/2009-2.0 Guy Pelletier 
 *       - 248293: JPA 2.0 Element Collections (part 2)
 *     02/25/2009-2.0 Guy Pelletier 
 *       - 265359: JPA 2.0 Element Collections - Metadata processing portions
 *     03/27/2009-2.0 Guy Pelletier 
 *       - 241413: JPA 2.0 Add EclipseLink support for Map type attributes
 *     04/24/2009-2.0 Guy Pelletier 
 *       - 270011: JPA 2.0 MappedById support
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.metadata.accessors.mappings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MappedById;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;

import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.internal.jpa.metadata.accessors.classes.ClassAccessor;
import org.eclipse.persistence.internal.jpa.metadata.accessors.objects.MetadataAccessibleObject;

import org.eclipse.persistence.internal.jpa.metadata.columns.PrimaryKeyJoinColumnMetadata;
import org.eclipse.persistence.internal.jpa.metadata.columns.PrimaryKeyJoinColumnsMetadata;

import org.eclipse.persistence.internal.jpa.metadata.MetadataDescriptor;
import org.eclipse.persistence.internal.jpa.metadata.MetadataLogger;

import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.indirection.WeavedObjectBasicIndirectionPolicy;

import org.eclipse.persistence.mappings.ObjectReferenceMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

/**
 * INTERNAL:
 * A single object relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class ObjectAccessor extends RelationshipAccessor {
    private Boolean m_id;
    private Boolean m_isOptional;
    private List<PrimaryKeyJoinColumnMetadata> m_primaryKeyJoinColumns = new ArrayList<PrimaryKeyJoinColumnMetadata>();
    private String m_mappedById;
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    protected ObjectAccessor(String xmlElement) {
        super(xmlElement);
    }
    
    /**
     * INTERNAL:
     */
    protected ObjectAccessor(Annotation annotation, MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(annotation, accessibleObject, classAccessor);
        
        m_isOptional = (annotation == null) ? true : (Boolean) MetadataHelper.invokeMethod("optional", annotation);
        
        // Set the primary key join columns if some are present.
        // Process all the primary key join columns first.
        if (isAnnotationPresent(PrimaryKeyJoinColumns.class)) {
            for (Annotation primaryKeyJoinColumn : (Annotation[]) MetadataHelper.invokeMethod("value", getAnnotation(PrimaryKeyJoinColumns.class))) { 
                m_primaryKeyJoinColumns.add(new PrimaryKeyJoinColumnMetadata(primaryKeyJoinColumn, accessibleObject));
            }
        }
        
        // Process the single primary key join column second.
        if (isAnnotationPresent(PrimaryKeyJoinColumn.class)) {
            m_primaryKeyJoinColumns.add(new PrimaryKeyJoinColumnMetadata(getAnnotation(PrimaryKeyJoinColumn.class), accessibleObject));
        }
        
        // Set the mapped by id if one is present.
        if (isAnnotationPresent(MappedById.class)) {
            m_mappedById = (String) MetadataHelper.invokeMethod("value", getAnnotation(MappedById.class));
        }
        
        // Set the derived id if one is specified.
        m_id = isAnnotationPresent(Id.class);
    }
    
    /**
     * INTERNAL:
     * Return the default fetch type for an object mapping.
     */
    public Enum getDefaultFetchType() {
        return FetchType.valueOf("EAGER");
    }
    
    /**
     * INTERNAL:
     */
    public Boolean getId(){
        return m_id;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public String getMappedById(){
        return m_mappedById;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public Boolean getOptional() {
        return m_isOptional;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */    
    public List<PrimaryKeyJoinColumnMetadata> getPrimaryKeyJoinColumns() {
        return m_primaryKeyJoinColumns;
    }
    
    /**
     * INTERNAL:
     * If a target entity is specified in metadata, it will be set as the 
     * reference class, otherwise we will use the raw class.
     */
    @Override
    public Class getReferenceClass() {
        if (m_referenceClass == null) {
            m_referenceClass = getTargetEntity();
        
            if (m_referenceClass == void.class) {
                // Get the reference class from the accessible object and
                // log the defaulting contextual reference class.
                m_referenceClass = super.getReferenceClass();
                getLogger().logConfigMessage(getLoggingContext(), getAnnotatedElement(), m_referenceClass);
            } 
        }
        
        return m_referenceClass;
    }
    
    /**
     * INTERNAL:
     * Used to process primary keys and DerivedIds.
     */
    protected Class getSimplePKType(){
        MetadataDescriptor referenceDescriptor = getReferenceDescriptor();
        ClassAccessor referenceAccessor = referenceDescriptor.getClassAccessor();
        
        if (referenceAccessor.hasDerivedId()) {
            // Referenced object has a derived ID and must be a simple pk type.  
            // Recurse through to get the simple type.
            return ((ObjectAccessor) referenceDescriptor.getAccessorFor(referenceDescriptor.getIdAttributeName())).getSimplePKType();
        } else {
            // Validate on their basic mapping.
            return referenceDescriptor.getAccessorFor(referenceDescriptor.getIdAttributeName()).getRawClass();
        }
    }
    
    /**
     * INTERNAL:
     */
    protected boolean hasMappedById() {
        return m_mappedById != null;
    }
    
    /**
     * INTERNAL:
     * Initialize a OneToOneMapping.
     */
    protected OneToOneMapping initOneToOneMapping() {
        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setIsReadOnly(false);
        mapping.setIsPrivateOwned(isPrivateOwned());
        mapping.setJoinFetch(getMappingJoinFetchType(getJoinFetch()));
        mapping.setIsOptional(isOptional());
        mapping.setAttributeName(getAttributeName());
        mapping.setReferenceClassName(getReferenceClassName());
        mapping.setIsDerivedIdMapping(isDerivedId());
        
        // Process the indirection.
        processIndirection(mapping);
        
        // Set the getter and setter methods if access is PROPERTY.
        setAccessorMethods(mapping);
        
        // Process the cascade types.
        processCascadeTypes(mapping);
        
        // Process a @ReturnInsert and @ReturnUpdate (to log a warning message)
        processReturnInsertAndUpdate();
        
        return mapping;
    }
    
    /**
     * INTERNAL:
     */
    @Override
    public void initXMLObject(MetadataAccessibleObject accessibleObject) {
        super.initXMLObject(accessibleObject);
    
        // Initialize lists of ORMetadata objects.
        initXMLObjects(m_primaryKeyJoinColumns, accessibleObject);
    }
    
    /**
     * INTERNAL:
     * Return true is this accessor is a derived id accessor.
     */
    @Override
    public boolean isDerivedId() {
        return m_id != null && m_id;
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents a 1-1 primary key relationship.
     */
    public boolean isOneToOnePrimaryKeyRelationship() {
        return isOneToOne() && ! m_primaryKeyJoinColumns.isEmpty();
    }
    
    /**
     * INTERNAL:
     */
    public boolean isOptional() {
        return m_isOptional != null && m_isOptional;
    }
    
    /**
     * INTERNAL:
     * Process the indirection (aka fetch type)
     */
    protected void processIndirection(ObjectReferenceMapping mapping) {
        boolean usesIndirection = usesIndirection();
        
        // If weaving was disabled, and the class was not static weaved,
        // then disable indirection.
        if (usesIndirection && (!getProject().isWeavingEnabled()) && (!ClassConstants.PersistenceWeavedLazy_Class.isAssignableFrom(getDescriptor().getJavaClass()))) {
            usesIndirection = false;
        }
        
        if (usesIndirection && usesPropertyAccess(getDescriptor())) {
            mapping.setIndirectionPolicy(new WeavedObjectBasicIndirectionPolicy(getSetMethodName()));
        } else {
            mapping.setUsesIndirection(usesIndirection);
        }
        
        mapping.setIsLazy(isLazy());
    }
    
    /**
     * INTERNAL:
     * Used to process primary keys and DerivedIds.
     */
    public void processKey(HashSet<ClassAccessor> processing, HashSet<ClassAccessor> processed){
        MetadataDescriptor referenceDescriptor = getReferenceDescriptor();
        ClassAccessor referenceAccessor = referenceDescriptor.getClassAccessor();
        
        if (!processed.contains(referenceAccessor)){
            referenceAccessor.processDerivedIDs(processing, processed);
        }

        processRelationship();
        String attributeName = getAttributeName();

        // If this entity has a pk class, we need to validate our ids. 
        String keyname = referenceDescriptor.getPKClassName();

        if (keyname != null) {
            // They have a pk class
            String ourpkname = this.getDescriptor().getPKClassName();
            if (ourpkname == null){
                throw ValidationException.invalidCompositePKSpecification(getJavaClass(), ourpkname);
            }
            
            if (! ourpkname.equals(keyname)){
                // Validate our pk contains their pk.
                getOwningDescriptor().validatePKClassId(attributeName, referenceDescriptor.getPKClass());
            } else {
                // This pk is the reference pk, so all pk attributes are accounted through this relationship
                getOwningDescriptor().getPKClassIDs().clear();
            }
        } else {
            Type type = null;
            if (referenceAccessor.hasDerivedId()){
                // Referenced object has a derived ID but no PK class defined,
                // so it must be a simple pk type. Recurse through to get the 
                // simple type
                type = ((ObjectAccessor) referenceDescriptor.getAccessorFor(referenceDescriptor.getIdAttributeName())).getSimplePKType();
            } else {
                // Validate on their basic mapping.
                type = referenceDescriptor.getAccessorFor(referenceDescriptor.getIdAttributeName()).getRawClass();
            }
            
            getOwningDescriptor().validatePKClassId(attributeName, type);
        }

        // Store the Id attribute name. Used with validation and OrderBy.
        getOwningDescriptor().addIdAttributeName(attributeName);

        // Add the primary key fields to the descriptor.  
        ObjectReferenceMapping mapping = (ObjectReferenceMapping)this.getMapping();
        for (DatabaseField pkField : mapping.getForeignKeyFields()){
            getOwningDescriptor().addPrimaryKeyField(pkField);
        }
    }
    
    /**
     * INTERNAL:
     * Process the mapping keys from the mapped by id field.
     */
    protected void processMappedByIdKeys(OneToOneMapping mapping) {
        if (m_mappedById.equals("")) {
            if (getReferenceDescriptor().hasCompositePrimaryKey()) {
                // We must have an embeddedid mapping that maps to the parents 
                // idclass or embedded id class directly.
                // Case 5: parent uses id class but dependant uses embeddedid
                // Case 6: both use embeddedid
                getDescriptor().getEmbeddedIdAccessor().processDerivedIdFields(mapping, getReferenceDescriptor());
            } else {
                // case 4: simple id association.
                DatabaseField dependentField = getDescriptor().getPrimaryKeyField();
                DatabaseField parentField = getReferenceDescriptor().getPrimaryKeyField();
                mapping.addForeignKeyField(dependentField, parentField);
            }
        } else {
            MappingAccessor mappingAccessor = getDescriptor().getAccessorFor(m_mappedById);
        
            if (mappingAccessor == null) {
                throw ValidationException.invalidMappedByIdValue(m_mappedById, getAnnotatedElementName(), getDescriptor().getEmbeddedIdAccessor().getReferenceClass());
            } else if (mappingAccessor.isBasic()) {
                // Case 1: basic mapping from embedded id to parent entity.
                DatabaseField dependentField = mappingAccessor.getMapping().getField();
                DatabaseField parentField = getReferenceDescriptor().getPrimaryKeyField();
                mapping.addForeignKeyField(dependentField, parentField);
            } else if (mappingAccessor.isDerivedIdClass()) {
                // Case 2 and case 3 (@IdClass or @EmbeddedId used as the derived id)
                ((DerivedIdClassAccessor) mappingAccessor).processDerivedIdFields(mapping, getReferenceDescriptor());
            }
            
            // This will also set the isDerivedIdMapping flag to true.
            mapping.setMappedByIdValue(m_mappedById);
        }
        
        
        mapping.setIsReadOnly(true);
    }
    
    /**
     * INTERNAL:
     * Process the primary key join columns for the owning side of a one to one 
     * mapping. The default pk and pk field names are used only with single 
     * primary key entities. The processor should never get as far as to use 
     * them with entities that have a composite primary key (validation 
     * exception will be thrown).
     */
    protected void processOneToOnePrimaryKeyRelationship(OneToOneMapping mapping) {
        MetadataDescriptor referenceDescriptor = getReferenceDescriptor();
        List<PrimaryKeyJoinColumnMetadata> pkJoinColumns = processPrimaryKeyJoinColumns(new PrimaryKeyJoinColumnsMetadata(getPrimaryKeyJoinColumns()));

        // Add the source foreign key fields to the mapping.
        for (PrimaryKeyJoinColumnMetadata primaryKeyJoinColumn : pkJoinColumns) {
            // The default primary key name is the primary key field name of the
            // referenced entity.
            DatabaseField pkField = primaryKeyJoinColumn.getPrimaryKeyField();
            pkField.setName(getName(pkField, referenceDescriptor.getPrimaryKeyFieldName(), MetadataLogger.PK_COLUMN));
            pkField.setTable(referenceDescriptor.getPrimaryTable());
            
            // The default foreign key name is the primary key of the
            // referencing entity.
            DatabaseField fkField = primaryKeyJoinColumn.getForeignKeyField();
            fkField.setName(getName(fkField, getDescriptor().getPrimaryKeyFieldName(), MetadataLogger.FK_COLUMN));
            fkField.setTable(getDescriptor().getPrimaryTable());
            
            // Add a source foreign key to the mapping.
            mapping.addForeignKeyField(fkField, pkField);
            
            // Mark the mapping read only
            mapping.setIsReadOnly(true);
        }
    }
    
    /**
     * INTERNAL:
     * Process the the correct metadata join column for the owning side of a 
     * one to one mapping.
     */
    protected void processOwningMappingKeys(OneToOneMapping mapping) {
        if (isOneToOnePrimaryKeyRelationship()) {
            processOneToOnePrimaryKeyRelationship(mapping);
        } else {
            // If the pk field (referencedColumnName) is not specified, it 
            // defaults to the primary key of the referenced table.
            String defaultPKFieldName = getReferenceDescriptor().getPrimaryKeyFieldName();
            
            // If the fk field (name) is not specified, it defaults to the 
            // concatenation of the following: the name of the referencing 
            // relationship property or field of the referencing entity or
            // embeddable class; "_"; the name of the referenced primary key 
            // column.
            String defaultFKFieldName = getUpperCaseAttributeName() + "_" + defaultPKFieldName;
            
            processOneToOneForeignKeyRelationship(mapping, getJoinColumns(getJoinColumns(), getReferenceDescriptor()), defaultPKFieldName, getReferenceDatabaseTable(), defaultFKFieldName, getDescriptor().getPrimaryTable());
        }
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setId(Boolean id){
        m_id = id;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setMappedById(String mappedById){
        m_mappedById = mappedById;
    }
    
    /**
     * INTERNAL:
     * Used for OX mapping.
     */
    public void setOptional(Boolean isOptional) {
        m_isOptional = isOptional;
    }
    
    /**
     * INTERNAL: 
     * Used for OX mapping.
     */
    public void setPrimaryKeyJoinColumns(List<PrimaryKeyJoinColumnMetadata> primaryKeyJoinColumns) {
        m_primaryKeyJoinColumns = primaryKeyJoinColumns;
    }
}
