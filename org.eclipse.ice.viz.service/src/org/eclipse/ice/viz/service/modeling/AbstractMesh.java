/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.modeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ice.viz.service.datastructures.VizObject.IManagedVizUpdateable;
import org.eclipse.ice.viz.service.datastructures.VizObject.IManagedVizUpdateableListener;
import org.eclipse.ice.viz.service.datastructures.VizObject.UpdateableSubscriptionManager;
import org.eclipse.ice.viz.service.datastructures.VizObject.UpdateableSubscriptionType;

/**
 * A component of the model. All models are built from collections of components
 * in a hierarchical structure. A component represents some concrete entity
 * which can be displayed inside the graphics engine.
 * 
 * @author Robert Smith
 */
public class AbstractMesh
		implements IManagedVizUpdateableListener, IManagedVizUpdateable {

	/**
	 * The mesh's type, which defines how the part internally stores its data.
	 */
	protected MeshType type;

	/**
	 * A list of other mesh components which are connected to this one, such as
	 * children.
	 */
	protected Map<String, List<AbstractController>> entities;

	/**
	 * A map of properties for the component.
	 */
	protected Map<String, String> properties;

	/**
	 * The listeners registered for updates from this object.
	 */
	protected UpdateableSubscriptionManager updateManager;

	/**
	 * The controller which manages this component
	 */
	protected AbstractController controller;

	/**
	 * The default constructor
	 */
	public AbstractMesh() {
		// Instantiate the class variables
		entities = new HashMap<String, List<AbstractController>>();
		properties = new HashMap<String, String>();
		type = MeshType.SIMPLE;
		updateManager = new UpdateableSubscriptionManager(this);
	}

	/**
	 * A constructor allowing the user to specify the mesh's type.
	 * 
	 * @param type
	 *            The type of mesh this component models.
	 */
	public AbstractMesh(MeshType type) {

		// Instantiate the class variables
		this();
		this.type = type;
	}

	/**
	 * A constructor which provides a set of child entities for the
	 * MeshComponent.
	 * 
	 * @param entities
	 *            The list of initial entities.
	 */
	public AbstractMesh(List<AbstractController> entities) {
		// Create a map of entities
		this.entities = new HashMap<String, List<AbstractController>>();

		// Add the input into the map of entities
		for (AbstractController entity : entities) {
			addEntity(entity);
		}

		// Instantiate the class variables
		properties = new HashMap<String, String>();
		type = MeshType.SIMPLE;
		updateManager = new UpdateableSubscriptionManager(this);
	}

	/**
	 * A constructor which provides a set of child entities and a mesh type
	 * 
	 * @param entities
	 *            The list of initial entities
	 * @param type
	 *            The mesh's type
	 */
	public AbstractMesh(List<AbstractController> entities, MeshType type) {
		// Create a list of entities
		this.entities = new HashMap<String, List<AbstractController>>();

		this.entities.put("Default", entities);

		// Instantiate the class variables
		properties = new HashMap<String, String>();
		this.type = type;
		updateManager = new UpdateableSubscriptionManager(this);
	}

	/**
	 * A constructor taking a map of input to initialize the MeshComponent. If
	 * the type is custom part, then the constructor will take a map of initial
	 * properties in the form of a Map<String, String> keyed on property names.
	 * Otherwise, it will take a map of initial entities in the form of a
	 * Map<String, EList<VizObject>> keyed on category names.
	 * 
	 * @param input
	 *            The map representing the child entities and their categories
	 *            or properties, according to the component's type.
	 * @param type
	 *            The type of component the mesh represents.
	 */
	public AbstractMesh(Map<String, Object> input, MeshType type) {
		// Instantiate the class variables
		this();
		this.type = type;

		// If the type is a custom part, the input should be a properties map
		if (type == MeshType.CUSTOM_PART) {

			// For each property, create an entry and add it to the list
			for (String property : input.keySet()) {
				properties.put(property, (String) input.get(property));
			}
		}

		// Otherwise the input is a list of entities
		else {

			// For each category, create an entry and add it to the map
			for (String category : input.keySet()) {
				List<AbstractController> tempList = (List<AbstractController>) input
						.get(category);
				entities.put(category,
						(List<AbstractController>) input.get(category));
			}
		}

	}

	/**
	 * Getter method for type.
	 * 
	 * @return The mesh's type
	 */
	public MeshType getType() {
		return type;
	}

	/**
	 * Setter method for the mesh's type
	 */
	public void setType(MeshType type) {
		this.type = type;
		UpdateableSubscriptionType[] eventTypes = {
				UpdateableSubscriptionType.Property };
		updateManager.notifyListeners(eventTypes);
	}

	/**
	 * Returns a list of all related entities.
	 * 
	 * @return All related entities.
	 */
	public List<AbstractController> getEntities() {

		// A temporary list of entities
		List<AbstractController> entityList = new ArrayList<AbstractController>();

		// Add the entities of each category to the list
		for (String category : entities.keySet()) {
			entityList.addAll(entities.get(category));
		}

		return entityList;

	}

	/**
	 * Return all of the part's children entities of a given category.
	 * 
	 * @category The category of entities to return
	 */
	public List<AbstractController> getEntitiesByCategory(String category) {

		// Get the entities under the given category
		List<AbstractController> temp = entities.get(category);

		// If the list is null, return an empty list instead
		return (temp != null ? new ArrayList<AbstractController>(temp)
				: new ArrayList<AbstractController>());
	}

	/**
	 * Return the value of the given property
	 * 
	 * @property The property to return
	 */
	public String getProperty(String property) {
		return properties.get(property);
	}

	/**
	 * Set the given property, creating it in the map if it is not already
	 * present.
	 * 
	 * @generated NOT
	 * 
	 * @property The property to set
	 * @value The property's new value
	 */
	public void setProperty(String property, String value) {

		// Whether the property was actually changed
		boolean changed = true;

		if (property.equals(properties.get(property))) {
			changed = false;
		}

		properties.put(property, value);

		// If a change occurred, send an update
		if (changed) {

			UpdateableSubscriptionType[] eventTypes = new UpdateableSubscriptionType[1];

			// Check if the changed property was selection to send the proper
			// update event.
			if ("Selected".equals(property)) {
				eventTypes[0] = UpdateableSubscriptionType.Selection;
			} else {
				eventTypes[0] = UpdateableSubscriptionType.Property;
			}
			updateManager.notifyListeners(eventTypes);
		}
	}

	/**
	 * Add a new entity to the part. A convenience method which allows for the
	 * specification of a default behavior for new entities when no category is
	 * specified.
	 * 
	 * @generated NOT
	 * 
	 * @newEntity The child entity to add to the part.
	 */
	public void addEntity(AbstractController newEntity) {
		addEntityByCategory(newEntity, "Default");
	}

	/**
	 * Removes the given entity from the part's children
	 *
	 * @generated NOT
	 * 
	 * @entity The entity to be removed
	 */
	public void removeEntity(AbstractController entity) {

		// Do not try to add null objects to the map
		if (entity == null) {
			return;
		}

		// Whether or not the entity was found in the map
		boolean found = false;

		// If the map contains the given entity
		for (String category : entities.keySet()) {
			if (entities.get(category).contains(entity)) {

				// Remove all copies of the entity from the map
				entities.values().removeAll(Collections.singleton(entity));

				entities.get(category).remove(entity);

				// Unregister from the entity
				entity.unregister(this);

				found = true;
			}
		}

		if (found) {
			UpdateableSubscriptionType[] eventTypes = {
					UpdateableSubscriptionType.Child };
			updateManager.notifyListeners(eventTypes);
		}
	}

	/**
	 * Adds a new child entity under the given category.
	 *
	 * @generated NOT
	 * 
	 * @param newEntity
	 *            The new child entity to be added
	 * @param category
	 *            The new entity's category
	 */
	public void addEntityByCategory(AbstractController newEntity,
			String category) {

		// Get the entities for the given category
		List<AbstractController> catList = entities.get(category);

		// If the list is null, make an empty one
		if (catList == null) {
			catList = new ArrayList<AbstractController>();
		}

		// Prevent a part from being added multiple times
		else if (catList.contains(newEntity)) {
			return;
		}

		// If the entity is already present in this category, don't add a second
		// entry for it
		else
			for (AbstractController entity : catList) {
				if (entity == newEntity) {
					return;
				}
			}

		// Add the entity to the list and put it in the map
		catList.add(newEntity);
		entities.put(category, catList);

		// Register with the entity
		newEntity.register(this);

		UpdateableSubscriptionType[] eventTypes = {
				UpdateableSubscriptionType.Child };
		updateManager.notifyListeners(eventTypes);
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.eclipse.ice.viz.service.datastructures.VizObject.IVizUpdateable#
	// * register(org.eclipse.ice.viz.service.datastructures.VizObject.
	// * IVizUpdateableListener)
	// */
	// @Override
	// public void register(IVizUpdateableListener listener) {
	// ArrayList<UpdateableSubscriptionType> eventTypes = new
	// ArrayList<UpdateableSubscriptionType>();
	// eventTypes.add(UpdateableSubscriptionType.All);
	// updateManager.register(listener, eventTypes);
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.datastructures.VizObject.IVizUpdateable#
	 * unregister(org.eclipse.ice.viz.service.datastructures.VizObject.
	 * IVizUpdateableListener)
	 */
	@Override
	public void unregister(IManagedVizUpdateableListener listener) {
		updateManager.unregister(listener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.datastructures.VizObject.
	 * IManagedVizUpdateableListener#update(org.eclipse.ice.viz.service.
	 * datastructures.VizObject.IVizUpdateable,
	 * org.eclipse.ice.viz.service.datastructures.VizObject.
	 * UpdateableSubscription)
	 */
	@Override
	public void update(IManagedVizUpdateable component,
			UpdateableSubscriptionType[] type) {

		// Pass the update to own listeners
		updateManager.notifyListeners(type);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object otherObject) {

		// Check if the objects are the same
		if (this == otherObject) {
			return true;
		}

		// Check if the other object is an AbstractMeshComponent and cast it
		if (!(otherObject instanceof AbstractMesh)) {
			return false;
		}

		AbstractMesh castObject = (AbstractMesh) otherObject;

		// Check the types, properties, and entity category for equality
		if (type != castObject.type || !properties.equals(castObject.properties)
				|| !entities.keySet().equals(castObject.entities.keySet())) {
			return false;
		}

		// For each category, check that the two objects' lists of child
		// entities in that category are equal.
		for (String category : entities.keySet()) {
			if (!entities.get(category)
					.containsAll(castObject.entities.get(category))
					|| !castObject.entities.get(category)
							.containsAll(entities.get(category))) {
				return false;
			}
		}

		// All checks passed, so the objects are equal
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {

		// Create a new component, and make it a copy of this one.
		AbstractMesh clone = new AbstractMesh();
		clone.copy(this);
		return clone;
	}

	/**
	 * Deep copies the contents of another AbstractMeshComponent into this one.
	 * This does not copy the reference to any parent AbstractController, as an
	 * AbstractController should have exactly one AbstractMesh as a model.
	 * 
	 * @param otherObject
	 *            The object which will be copied into this.
	 */
	public void copy(AbstractMesh otherObject) {

		// Copy each of the other component's data members
		type = otherObject.type;
		properties = new HashMap<String, String>(otherObject.properties);

		// Clone each child entity
		for (String category : otherObject.entities.keySet()) {
			for (AbstractController entity : otherObject
					.getEntitiesByCategory(category)) {
				addEntityByCategory((AbstractController) entity.clone(),
						category);
			}
		}

		// Notify listeners of the change
		UpdateableSubscriptionType[] eventTypes = {
				UpdateableSubscriptionType.All };
		updateManager.notifyListeners(eventTypes);
	}

	/**
	 * Getter method for the controller.
	 * 
	 * @return The AbstractController which manages this component
	 */
	public AbstractController getController() {
		return controller;
	}

	/**
	 * Setter method for the controller.
	 * 
	 * @param controller
	 *            The AbstractController which manages this component
	 */
	public void setController(AbstractController controller) {
		this.controller = controller;

		// Set the manager's parent as well
		updateManager.setParent(controller.getUpdateManager());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.datastructures.VizObject.
	 * IManagedVizUpdateable#register(org.eclipse.ice.viz.service.datastructures
	 * .VizObject.IManagedVizUpdateableListener)
	 */
	@Override
	public void register(IManagedVizUpdateableListener listener) {
		updateManager.register(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.datastructures.VizObject.
	 * IManagedVizUpdateableListener#getSubscriptions(org.eclipse.ice.viz.
	 * service.datastructures.VizObject.IVizUpdateable)
	 */
	@Override
	public ArrayList<UpdateableSubscriptionType> getSubscriptions(
			IManagedVizUpdateable source) {
		ArrayList<UpdateableSubscriptionType> types = new ArrayList<UpdateableSubscriptionType>();
		types.add(UpdateableSubscriptionType.All);
		return types;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = type.hashCode();
		for (String category : entities.keySet()) {
			for (AbstractController entity : getEntitiesByCategory(category)) {
				hash += entity.hashCode();
			}
		}
		hash += properties.hashCode();
		return hash;
	}

}